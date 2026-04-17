package xCloud.service.recruitment;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.DataType;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.request.AddFieldReq;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.collection.request.HasCollectionReq;
import io.milvus.v2.service.collection.request.LoadCollectionReq;
import io.milvus.v2.service.index.request.CreateIndexReq;
import io.milvus.v2.service.vector.request.InsertReq;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import xCloud.entity.TextVectorLog;
import xCloud.entity.recruitment.DocumentChunk;
import xCloud.mapper.DocumentChunkMapper;
import xCloud.openAiChatModel.ali.embedding.AliEmbeddingUtil;
import xCloud.service.VectorLogService;
import xCloud.tools.CodeX;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * PDF 上传、智能切分、向量化存储
 * <p>
 * 切分策略（Recursive Character Splitter）：
 * 1. 优先按段落（\n\n）切分，保留语义完整性
 * 2. 段落过长则按句子（。！？\n）再切
 * 3. 仍过长则按字符数硬切，保留 OVERLAP 字符重叠
 * 4. 过短的块与相邻块合并，避免碎片化
 * <p>
 * 高并发优化：
 * - Embedding 分批并行（每批 25 条，多批同时提交 taskExecutor）
 * - ensureCollectionExists 双重检查锁（DCL），避免并发重复创建
 * - MySQL 批量 insert（单条 SQL 多 VALUES），减少 N-1 次网络往返
 * - uploadPdfAsync 异步化，请求线程立即返回任务 Future
 *
 * @Author Andy Fan
 */
@Slf4j
@Service
public class PdfChunkService {

    static final String COLLECTION = "recruitment_docs";

    private static final int CHUNK_SIZE = 500;  // 每块最大字符数
    private static final int OVERLAP = 100;  // 相邻块重叠字符数
    private static final int MIN_CHUNK = 50;   // 最小块字符数（过短则合并）
    private static final int VECTOR_DIM = 1024; // Ali text-embedding-v4 输出维度
    private static final int EMBED_BATCH = 25;   // DashScope 单次批量上限

    /**
     * 分隔符优先级列表（从粗到细），静态块预编译，避免循环内重复编译
     */
    private static final Pattern[] DELIMITER_PATTERNS;

    static {
        String[] delimiters = {"\n\n", "\n", "。", "！", "？", "."};
        DELIMITER_PATTERNS = new Pattern[delimiters.length];
        for (int i = 0; i < delimiters.length; i++) {
            DELIMITER_PATTERNS[i] = Pattern.compile(Pattern.quote(delimiters[i]));
        }
    }

    /**
     * DCL 标志位：volatile 保证多线程可见性
     */
    private volatile boolean collectionReady = false;

    @Lazy
    @Resource
    private MilvusClientV2 milvusClientV2;

    @Resource
    private AliEmbeddingUtil aliEmbeddingUtil;

    @Resource
    private DocumentChunkMapper documentChunkMapper;

    @Resource
    private VectorLogService vectorLogService;

    @Resource
    @Qualifier("taskExecutor")
    private ThreadPoolExecutor taskExecutor;


    /**
     * 1. 上传入口 同步上传（阻塞直到完成），适合小文件或内部调用
     *
     * @param file    PDF 文件（仅支持 .pdf，不能为空）
     * @param docType 文档类型：company_doc / job_info
     * @return [成功入库块数, Embedding 失败跳过块数]
     */
    public int[] uploadPdf(MultipartFile file, String docType) throws Exception {
        return uploadPdfAsync(file, docType).get();
    }

    /**
     * 异步上传，请求线程立即返回 Future，适合高并发场景
     * <p>
     * PDF 解析在调用线程完成（避免 MultipartFile 流关闭问题），
     * 切分 + Embedding + 写库在 taskExecutor 中异步执行。
     *
     * @param file    PDF 文件（仅支持 .pdf，不能为空）
     * @param docType 文档类型：company_doc / job_info
     * @return Future，持有 [成功块数, 跳过块数]
     */
    public CompletableFuture<int[]> uploadPdfAsync(MultipartFile file, String docType) throws Exception {
        // ── 入参校验（同步，快速失败）──────────────────
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("仅支持 PDF 格式，当前文件: " + filename);
        }

        ensureCollectionExists();

        // PDF 解析在当前线程完成，避免 InputStream 被提前关闭
        String fullText = extractText(file.getInputStream());
        List<DocumentChunk> chunks = smartChunk(fullText, filename, docType);

        if (chunks.isEmpty()) {
            log.warn("PDF [{}] 切分后无有效块", filename);
            return CompletableFuture.completedFuture(new int[]{0, 0});
        }

        // 切分完成后，Embedding + 写库异步执行
        return CompletableFuture.supplyAsync(() -> {
            try {
                int[] result = storeChunks(chunks);
                log.info("PDF [{}] 入库完成，成功 {} 块，跳过 {} 块", filename, result[0], result[1]);
                return result;
            } catch (Exception e) {
                log.error("PDF [{}] 存储失败: {}", filename, e.getMessage(), e);
                throw new CompletionException(e);
            }
        }, taskExecutor);
    }


    /**
     * 2. PDF 文本提取 使用 PDFBox 提取 PDF 全文，按位置排序保证阅读顺序
     *
     * @param is PDF 输入流
     * @return 提取的纯文本内容
     */
    private String extractText(InputStream is) throws Exception {
        try (PDDocument doc = PDDocument.load(is)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(doc);
        }
    }

    /**
     * 3. 智能切分（Recursive Character Splitter） 递归字符切分：先按语义边界分割，再合并过短块、裁剪过长块，最终加 Overlap
     *
     * @param text    待切分全文
     * @param source  文件名（写入元数据）
     * @param docType 文档类型（写入元数据）
     * @return 切分后的文档块列表
     */
    List<DocumentChunk> smartChunk(String text, String source, String docType) {
        List<String> paragraphs = splitByDelimiters(text);
        List<String> finalChunks = mergeAndSplit(paragraphs);

        List<DocumentChunk> result = new ArrayList<>();
        int validIndex = 0;
        for (String raw : finalChunks) {
            String content = raw.trim();
            if (content.isEmpty()) continue;

            DocumentChunk chunk = new DocumentChunk();
            chunk.setId(CodeX.nextId());
            chunk.setContent(content);
            chunk.setSource(source);
            chunk.setDocType(docType);
            chunk.setChunkIndex(validIndex++); // 连续序号，不因空块跳号
            chunk.setPageNum(0);
            chunk.setCreateTime(LocalDateTime.now());
            result.add(chunk);
        }
        return result;
    }

    /**
     * 按预编译分隔符列表递归切分：仅对超过 CHUNK_SIZE 的块继续细分
     *
     * @param text 待切分文本
     * @return 切分后的文本片段列表
     */
    private List<String> splitByDelimiters(String text) {
        List<String> chunks = new ArrayList<>();
        chunks.add(text);

        for (Pattern pattern : DELIMITER_PATTERNS) {
            List<String> newChunks = new ArrayList<>();
            for (String chunk : chunks) {
                if (chunk.length() <= CHUNK_SIZE) {
                    newChunks.add(chunk);
                } else {
                    for (String part : pattern.split(chunk)) {
                        if (!part.trim().isEmpty()) newChunks.add(part);
                    }
                }
            }
            chunks = newChunks;
        }
        return chunks;
    }

    /**
     * 合并 + 切分 + Overlap：
     * - 将过短片段合并到前一块直至达到 CHUNK_SIZE
     * - 新块开头保留上一块末尾 OVERLAP 字符，保证上下文连贯
     *
     * @param raw 原始片段列表
     * @return 处理后的块列表
     */
    private List<String> mergeAndSplit(List<String> raw) {
        List<String> result = new ArrayList<>();
        StringBuilder buffer = new StringBuilder();

        for (String piece : raw) {
            String trimmed = piece.trim();
            if (trimmed.isEmpty()) continue;

            if (buffer.length() + trimmed.length() <= CHUNK_SIZE) {
                if (buffer.length() > 0) buffer.append(" ");
                buffer.append(trimmed);
            } else {
                if (buffer.length() >= MIN_CHUNK) {
                    result.add(buffer.toString());
                    // Overlap：保留末尾 OVERLAP 字符作为下一块前缀，保持语义连贯
                    String overlap = buffer.length() > OVERLAP
                            ? buffer.substring(buffer.length() - OVERLAP)
                            : buffer.toString();
                    buffer = new StringBuilder(overlap).append(" ").append(trimmed);
                } else {
                    buffer.append(" ").append(trimmed);
                }
            }
        }
        if (buffer.length() >= MIN_CHUNK) result.add(buffer.toString());
        return result;
    }


    /**
     * 4. 存储：Milvus（向量） + MySQL（文本） 并行批量 Embedding + 写 Milvus + 异步批量写 MySQL
     * <p>
     * 并行策略：将 chunks 按 EMBED_BATCH(25) 分组，每组提交一个 CompletableFuture 到
     * taskExecutor 并行调用 DashScope，allOf 等待全部完成后统一写 Milvus。
     * <p>
     * - Milvus：同步批量写入，保证向量可检索
     * - MySQL：异步批量 insert（单 SQL 多 VALUES），不阻塞主流程
     *
     * @param chunks 待存储的文档块列表
     * @return [成功写入 Milvus 块数, Embedding 失败跳过块数]
     */
    private int[] storeChunks(List<DocumentChunk> chunks) {
        int total = chunks.size();
        // 按 EMBED_BATCH 分组
        List<List<DocumentChunk>> batches = new ArrayList<>();
        for (int i = 0; i < total; i += EMBED_BATCH) {
            batches.add(chunks.subList(i, Math.min(i + EMBED_BATCH, total)));
        }

        // 每批并行提交 Embedding 任务
        List<CompletableFuture<BatchResult>> futures = new ArrayList<>();
        for (List<DocumentChunk> batch : batches) {
            futures.add(CompletableFuture.supplyAsync(
                    () -> embedBatch(batch), taskExecutor));
        }

        // 等待所有批次完成，汇总结果
        List<JsonObject> milvusRows = new ArrayList<>();
        List<DocumentChunk> mysqlRows = new ArrayList<>();
        AtomicInteger skipCount = new AtomicInteger(0);

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        for (CompletableFuture<BatchResult> f : futures) {
            BatchResult br = f.join();
            milvusRows.addAll(br.milvusRows);
            mysqlRows.addAll(br.mysqlRows);
            skipCount.addAndGet(br.skipCount);
        }

        // 批量写入 Milvus
        if (!milvusRows.isEmpty()) {
            milvusClientV2.insert(InsertReq.builder()
                    .collectionName(COLLECTION)
                    .data(milvusRows)
                    .build());
            log.info("Milvus 批量写入 {} 块完成", milvusRows.size());
        }

        // 异步批量写入 MySQL（单 SQL 多 VALUES，减少 N-1 次网络往返）
        if (!mysqlRows.isEmpty()) {
            final List<DocumentChunk> toSave = mysqlRows;
            taskExecutor.execute(() -> {
                try {
                    documentChunkMapper.insertBatch(toSave);
                    log.info("MySQL 批量写入 {} 块完成", toSave.size());
                } catch (Exception e) {
                    log.error("MySQL 批量写入失败，Milvus 已写入但 MySQL 缺失，需人工核查: {}",
                            e.getMessage(), e);
                }
            });
        }

        return new int[]{milvusRows.size(), skipCount.get()};
    }

    /**
     * 对一批 chunks 调用批量 Embedding API，构建 Milvus 行和 MySQL 行，并异步写向量日志
     *
     * @param batch 一批文档块（size ≤ EMBED_BATCH）
     * @return 该批次的处理结果
     */
    private BatchResult embedBatch(List<DocumentChunk> batch) {
        List<String> texts = new ArrayList<>();
        for (DocumentChunk c : batch) texts.add(c.getContent());

        List<List<Double>> vectors = aliEmbeddingUtil.embeddingBatch(texts);

        BatchResult result = new BatchResult();
        if (vectors == null) {
            log.warn("批量 Embedding 失败，跳过 {} 块，source={}",
                    batch.size(), batch.get(0).getSource());
            result.skipCount = batch.size();
            return result;
        }

        // 收集本批次的向量日志，embedBatch 结束后统一异步写入
        List<TextVectorLog> logBatch = new ArrayList<>(batch.size());

        for (int i = 0; i < batch.size(); i++) {
            DocumentChunk chunk = batch.get(i);
            List<Double> vector = (i < vectors.size()) ? vectors.get(i) : null;
            if (vector == null) {
                log.warn("Embedding 结果缺失，跳过 chunk id={}", chunk.getId());
                result.skipCount++;
                continue;
            }

            // ── 构建 Milvus 行 ──────────────────────────────────────
            JsonObject row = new JsonObject();
            row.addProperty("id", chunk.getId());
            JsonArray arr = new JsonArray();
            vector.forEach(arr::add);
            row.add("vector", arr);
            row.addProperty("content", chunk.getContent());
            row.addProperty("source", chunk.getSource());
            row.addProperty("doc_type", chunk.getDocType());
            row.addProperty("chunk_index", chunk.getChunkIndex());
            row.addProperty("page_num", chunk.getPageNum());
            result.milvusRows.add(row);
            result.mysqlRows.add(chunk);

            // ── 构建向量日志（与 Milvus id 对应，向量序列化为 JSON 字符串）──
            logBatch.add(VectorLogService.buildLog(
                    chunk.getId(),
                    chunk.getContent(),
                    arr.toString(),          // 向量 JSON，如 "[0.12, -0.34, ...]"
                    chunk.getSource(),
                    "docType=" + chunk.getDocType() + ",chunkIndex=" + chunk.getChunkIndex()
            ));
        }

        // 异步批量写入 text_vector_log，不阻塞 Embedding 主流程
        if (!logBatch.isEmpty()) {
            vectorLogService.asyncLogBatch(logBatch);
        }

        return result;
    }

    /**
     * 单批次 Embedding 结果载体
     */
    private static class BatchResult {
        final List<JsonObject> milvusRows = new ArrayList<>();
        final List<DocumentChunk> mysqlRows = new ArrayList<>();
        int skipCount = 0;
    }


    /**
     * 5. Milvus Collection 初始化（DCL） 检查并初始化 Milvus Collection（首次部署时调用一次即可）
     * <p>
     * 使用双重检查锁（DCL）保证高并发下只创建一次，避免并发重复创建报错。
     * <p>
     * Schema 字段：id / vector / content / source / doc_type / chunk_index / page_num
     * 索引：AUTOINDEX + 内积（IP）距离，适配 Ali Embedding 归一化向量
     */
    public void ensureCollectionExists() {
        if (collectionReady) return; // 第一次检查，无锁快速返回
        synchronized (this) {
            if (collectionReady) return; // 第二次检查，防止重复创建

            boolean exists = milvusClientV2.hasCollection(
                    HasCollectionReq.builder().collectionName(COLLECTION).build());
            if (!exists) {
                createCollection();
            }
            collectionReady = true;
        }
    }

    /**
     * 创建 Milvus Collection、索引并加载到内存
     */
    private void createCollection() {
        CreateCollectionReq.CollectionSchema schema =
                CreateCollectionReq.CollectionSchema.builder().build();
        schema.addField(AddFieldReq.builder().fieldName("id")
                .dataType(DataType.Int64).isPrimaryKey(true).autoID(false).build());
        schema.addField(AddFieldReq.builder().fieldName("vector")
                .dataType(DataType.FloatVector).dimension(VECTOR_DIM).build());
        schema.addField(AddFieldReq.builder().fieldName("content")
                .dataType(DataType.VarChar).maxLength(65535).build());
        schema.addField(AddFieldReq.builder().fieldName("source")
                .dataType(DataType.VarChar).maxLength(500).build());
        schema.addField(AddFieldReq.builder().fieldName("doc_type")
                .dataType(DataType.VarChar).maxLength(100).build());
        schema.addField(AddFieldReq.builder().fieldName("chunk_index")
                .dataType(DataType.Int64).build());
        schema.addField(AddFieldReq.builder().fieldName("page_num")
                .dataType(DataType.Int64).build());

        milvusClientV2.createCollection(CreateCollectionReq.builder()
                .collectionName(COLLECTION)
                .collectionSchema(schema)
                .build());

        milvusClientV2.createIndex(CreateIndexReq.builder()
                .collectionName(COLLECTION)
                .indexParams(Collections.singletonList(IndexParam.builder()
                        .fieldName("vector")
                        .indexType(IndexParam.IndexType.AUTOINDEX)
                        .metricType(IndexParam.MetricType.IP) // 内积，适配归一化向量
                        .build()))
                .build());

        milvusClientV2.loadCollection(
                LoadCollectionReq.builder().collectionName(COLLECTION).build());

        log.info("Milvus collection [{}] 创建完成", COLLECTION);
    }
}
