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
import xCloud.entity.recruitment.DocumentChunk;
import xCloud.mapper.DocumentChunkMapper;
import xCloud.openAiChatModel.ali.embedding.AliEmbeddingUtil;
import xCloud.tools.CodeX;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * PDF 上传、智能切分、向量化存储
 *
 * 切分策略（Recursive Character Splitter）：
 *   1. 优先按段落（\n\n）切分，保留语义完整性
 *   2. 段落过长则按句子（。！？\n）再切
 *   3. 仍过长则按字符数硬切，保留 OVERLAP 字符重叠
 *   4. 过短的块与相邻块合并，避免碎片化
 */
@Slf4j
@Service
public class PdfChunkService {

    static final String COLLECTION = "recruitment_docs";
    private static final int CHUNK_SIZE   = 500;  // 每块最大字符数
    private static final int OVERLAP      = 100;  // 相邻块重叠字符数
    private static final int MIN_CHUNK    = 50;   // 最小块字符数（过短则合并）
    private static final int VECTOR_DIM   = 1024; // Ali text-embedding-v4 维度

    @Lazy
    @Resource
    private MilvusClientV2 milvusClientV2;

    @Resource
    private AliEmbeddingUtil aliEmbeddingUtil;

    @Resource
    private DocumentChunkMapper documentChunkMapper;

    @Resource
    @Qualifier("taskExecutor")
    private ThreadPoolExecutor taskExecutor;

    // ─────────────────────────────────────────────
    // 1. 上传入口
    // ─────────────────────────────────────────────

    /**
     * 上传 PDF，切分 → 向量化 → 存 Milvus + MySQL
     *
     * @param file    PDF 文件
     * @param docType 文档类型：company_doc（公司内部文档）/ job_info（岗位信息）
     * @return 入库块数
     */
    public int uploadPdf(MultipartFile file, String docType) throws Exception {
        ensureCollectionExists();

        String source = file.getOriginalFilename();
        String fullText = extractText(file.getInputStream());
        List<DocumentChunk> chunks = smartChunk(fullText, source, docType);

        if (chunks.isEmpty()) {
            log.warn("PDF [{}] 切分后无有效块", source);
            return 0;
        }

        storeChunks(chunks);
        log.info("PDF [{}] 入库完成，共 {} 块", source, chunks.size());
        return chunks.size();
    }

    // ─────────────────────────────────────────────
    // 2. PDF 文本提取
    // ─────────────────────────────────────────────

    private String extractText(InputStream is) throws Exception {
        try (PDDocument doc = PDDocument.load(is)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(doc);
        }
    }

    // ─────────────────────────────────────────────
    // 3. 智能切分（Recursive Character Splitter）
    // ─────────────────────────────────────────────

    List<DocumentChunk> smartChunk(String text, String source, String docType) {
        // Step 1: 按段落切分
        List<String> paragraphs = splitByDelimiters(text, new String[]{"\n\n", "\n", "。", "！", "？", "."});

        // Step 2: 合并过短块，切分过长块，加 overlap
        List<String> finalChunks = mergeAndSplit(paragraphs);

        // Step 3: 构建实体
        List<DocumentChunk> result = new ArrayList<>();
        for (int i = 0; i < finalChunks.size(); i++) {
            String content = finalChunks.get(i).trim();
            if (content.isEmpty()) continue;

            DocumentChunk chunk = new DocumentChunk();
            chunk.setId(CodeX.nextId());
            chunk.setContent(content);
            chunk.setSource(source);
            chunk.setDocType(docType);
            chunk.setChunkIndex(i);
            chunk.setPageNum(0); // PDFBox 按页提取可进一步细化
            chunk.setCreateTime(LocalDateTime.now());
            result.add(chunk);
        }
        return result;
    }

    private List<String> splitByDelimiters(String text, String[] delimiters) {
        List<String> chunks = new ArrayList<>();
        chunks.add(text);

        for (String delimiter : delimiters) {
            List<String> newChunks = new ArrayList<>();
            for (String chunk : chunks) {
                if (chunk.length() <= CHUNK_SIZE) {
                    newChunks.add(chunk);
                } else {
                    String[] parts = chunk.split(java.util.regex.Pattern.quote(delimiter));
                    for (String part : parts) {
                        if (!part.trim().isEmpty()) {
                            newChunks.add(part);
                        }
                    }
                }
            }
            chunks = newChunks;
        }
        return chunks;
    }

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
                    // overlap: 保留末尾 OVERLAP 字符作为下一块开头
                    String overlap = buffer.length() > OVERLAP
                            ? buffer.substring(buffer.length() - OVERLAP)
                            : buffer.toString();
                    buffer = new StringBuilder(overlap).append(" ").append(trimmed);
                } else {
                    buffer.append(" ").append(trimmed);
                }
            }
        }
        if (buffer.length() >= MIN_CHUNK) {
            result.add(buffer.toString());
        }
        return result;
    }

    // ─────────────────────────────────────────────
    // 4. 存储：Milvus（向量） + MySQL（文本）
    // ─────────────────────────────────────────────

    private void storeChunks(List<DocumentChunk> chunks) {
        List<JsonObject> milvusRows = new ArrayList<>();

        for (DocumentChunk chunk : chunks) {
            List<Double> vector = aliEmbeddingUtil.embeddingB(chunk.getContent());
            if (vector == null) {
                log.warn("Embedding 失败，跳过 chunk id={}", chunk.getId());
                continue;
            }

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
            milvusRows.add(row);
        }

        if (!milvusRows.isEmpty()) {
            milvusClientV2.insert(InsertReq.builder()
                    .collectionName(COLLECTION)
                    .data(milvusRows)
                    .build());
        }

        // 异步写 MySQL（关键词检索用）
        taskExecutor.execute(() -> {
            try {
                for (DocumentChunk chunk : chunks) {
                    documentChunkMapper.insert(chunk);
                }
                log.info("MySQL 批量写入 {} 块完成", chunks.size());
            } catch (Exception e) {
                log.error("MySQL 写入失败: {}", e.getMessage(), e);
            }
        });
    }

    // ─────────────────────────────────────────────
    // 5. Milvus Collection 初始化
    // ─────────────────────────────────────────────

    public void ensureCollectionExists() {
        boolean exists = milvusClientV2.hasCollection(
                HasCollectionReq.builder().collectionName(COLLECTION).build());
        if (exists) return;

        // 使用 builder() 直接构造 schema，不经过 deprecated 工厂方法
        CreateCollectionReq.CollectionSchema schema = CreateCollectionReq.CollectionSchema.builder().build();
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
                        .metricType(IndexParam.MetricType.IP) // 内积，适合归一化向量
                        .build()))
                .build());

        milvusClientV2.loadCollection(LoadCollectionReq.builder()
                .collectionName(COLLECTION).build());

        log.info("Milvus collection [{}] 创建完成", COLLECTION);
    }
}
