package xCloud.openAiChatModel.service;

import com.openai.models.vectorstores.VectorStore;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xCloud.entity.document.RetrievedDocument;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2026/3/17 00:13
 * @ClassName RecruitmentVectorService
 */
@Service
public class RecruitmentVectorService {

    @Autowired
    private VectorStore vectorStore;
    @Autowired
    private EmbeddingModel embeddingModel;

    public List<String> searchRecruitmentInfo(String query) {
        // 生成查询向量
        Embedding queryEmbedding = embeddingModel.embed(query).content();
        // todo andy 检索相似文档
//        List<RetrievedDocument> docs = vectorStore.search(
//                queryEmbedding.vector(),
//                SearchRequest.builder().topK(3).build()
//        );
        // todo andy 提取文档内容
//        return docs.stream()
//                .map(RetrievedDocument::content)
//                .collect(Collectors.toList());
        return null;
    }


}
