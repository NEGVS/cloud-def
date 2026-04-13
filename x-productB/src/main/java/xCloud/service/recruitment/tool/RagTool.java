package xCloud.service.recruitment.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import xCloud.entity.recruitment.DocumentChunk;
import xCloud.service.recruitment.ContextCompressService;
import xCloud.service.recruitment.HybridRagService;
import xCloud.service.recruitment.RerankService;

import java.util.List;

/**
 * 文档检索工具（RAG Tool）
 *
 * 流程：多路召回 → Re-ranking → 上下文压缩 → 返回精简上下文
 * 使用场景：用户询问公司政策、岗位要求、内部文档等
 */
@Slf4j
@Component
public class RagTool implements AgentTool {

    private final HybridRagService hybridRagService;
    private final RerankService rerankService;
    private final ContextCompressService contextCompressService;

    public RagTool(HybridRagService hybridRagService,
                   RerankService rerankService,
                   ContextCompressService contextCompressService) {
        this.hybridRagService = hybridRagService;
        this.rerankService = rerankService;
        this.contextCompressService = contextCompressService;
    }

    @Override
    public String getName() {
        return "document_search";
    }

    @Override
    public String getDescription() {
        return "搜索公司内部文档和岗位信息。当用户询问岗位要求、薪资待遇、公司政策、" +
               "招聘流程、职位描述等问题时使用。Input: 搜索关键词或问题描述。";
    }

    @Override
    public String execute(String input) {
        try {
            log.info("[RagTool] 执行检索: {}", input);

            // 多路召回
            List<DocumentChunk> candidates = hybridRagService.hybridSearch(input, 10);
            if (candidates.isEmpty()) {
                return "未找到相关文档信息。";
            }

            // Re-ranking
            List<DocumentChunk> reranked = rerankService.rerank(input, candidates, 5);

            // 上下文压缩
            String compressed = contextCompressService.compress(input, reranked);

            log.info("[RagTool] 检索完成，压缩后上下文长度: {}", compressed.length());
            return compressed;

        } catch (Exception e) {
            log.error("[RagTool] 执行失败: {}", e.getMessage(), e);
            return "文档检索失败: " + e.getMessage();
        }
    }
}
