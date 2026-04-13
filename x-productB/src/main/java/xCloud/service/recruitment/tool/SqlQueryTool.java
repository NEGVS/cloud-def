package xCloud.service.recruitment.tool;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import xCloud.entity.recruitment.DocumentChunk;
import xCloud.mapper.DocumentChunkMapper;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据库查询工具（SQL Tool）
 *
 * 通过 MyBatis-Plus 安全查询招聘相关数据，
 * 避免直接拼接 SQL（防注入）。
 * 使用场景：查询候选人数量、岗位统计、招聘进度等结构化数据。
 */
@Slf4j
@Component
public class SqlQueryTool implements AgentTool {

    private final DocumentChunkMapper documentChunkMapper;

    public SqlQueryTool(DocumentChunkMapper documentChunkMapper) {
        this.documentChunkMapper = documentChunkMapper;
    }

    @Override
    public String getName() {
        return "database_query";
    }

    @Override
    public String getDescription() {
        return "查询招聘数据库中的结构化数据。当用户询问已上传了哪些文档、" +
               "某类型文档的数量、特定来源的文档内容时使用。" +
               "Input: 查询意图描述，如 '查询所有岗位信息文档' 或 '统计公司文档数量'。";
    }

    @Override
    public String execute(String input) {
        try {
            log.info("[SqlQueryTool] 执行查询: {}", input);

            String lowerInput = input.toLowerCase();

            // 统计文档数量
            if (lowerInput.contains("数量") || lowerInput.contains("统计") || lowerInput.contains("多少")) {
                return queryStats();
            }

            // 查询岗位信息
            if (lowerInput.contains("岗位") || lowerInput.contains("职位") || lowerInput.contains("job")) {
                return queryByDocType("job_info");
            }

            // 查询公司文档
            if (lowerInput.contains("公司") || lowerInput.contains("内部") || lowerInput.contains("company")) {
                return queryByDocType("company_doc");
            }

            // 默认：返回最近上传的文档摘要
            return queryRecent();

        } catch (Exception e) {
            log.error("[SqlQueryTool] 查询失败: {}", e.getMessage(), e);
            return "数据库查询失败: " + e.getMessage();
        }
    }

    private String queryStats() {
        long total = documentChunkMapper.selectCount(null);
        long jobCount = documentChunkMapper.selectCount(
                new QueryWrapper<DocumentChunk>().eq("doc_type", "job_info"));
        long companyCount = documentChunkMapper.selectCount(
                new QueryWrapper<DocumentChunk>().eq("doc_type", "company_doc"));
        return String.format("数据库统计：共 %d 个文档块，其中岗位信息 %d 块，公司文档 %d 块。",
                total, jobCount, companyCount);
    }

    private String queryByDocType(String docType) {
        List<DocumentChunk> chunks = documentChunkMapper.selectList(
                new QueryWrapper<DocumentChunk>()
                        .eq("doc_type", docType)
                        .select("source", "chunk_index", "content")
                        .last("LIMIT 5"));
        if (chunks.isEmpty()) return "未找到 " + docType + " 类型的文档。";

        return chunks.stream()
                .map(c -> "[" + c.getSource() + "] " + c.getContent())
                .collect(Collectors.joining("\n\n"));
    }

    private String queryRecent() {
        List<DocumentChunk> chunks = documentChunkMapper.selectList(
                new QueryWrapper<DocumentChunk>()
                        .orderByDesc("create_time")
                        .select("source", "doc_type", "create_time")
                        .last("LIMIT 10"));
        if (chunks.isEmpty()) return "数据库中暂无文档。";

        return "最近上传的文档：\n" + chunks.stream()
                .map(c -> c.getSource() + "（" + c.getDocType() + "）")
                .distinct()
                .collect(Collectors.joining("\n"));
    }
}
