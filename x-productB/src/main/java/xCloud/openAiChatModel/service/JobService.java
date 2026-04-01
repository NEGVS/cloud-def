package xCloud.openAiChatModel.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.request.InsertReq;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/9/5 15:14
 * @ClassName JobService
 */
@Service
public class JobService {

    @Autowired
    private MilvusClientV2 milvusClient;

    @Autowired
    private EmbeddingModel embeddingModel;

    public Object apply(long id, long cid) {
        return null;
    }

    public void importPdfToVector(String pdfPath, Integer categoryId) throws Exception {
        PDDocument doc = PDDocument.load(new File(pdfPath));
        String text = new PDFTextStripper().getText(doc);
        doc.close();

        String[] paragraphs = text.split("\n\n");
        List<Map<String, Object>> data = new ArrayList<>();

        for (String para : paragraphs) {
            if (para.trim().isEmpty()) continue;

            Embedding embedding = embeddingModel.embed(para).content();
            List<Float> vector = Arrays.stream(embedding.vector()).boxed().toList();

            Map<String, Object> row = new HashMap<>();
            row.put("job_desc", para);
            row.put("category_id", categoryId);
            row.put("vector", vector);
            data.add(row);
        }

        milvusClient.insert(InsertReq.builder()
            .collectionName("job_collection")
            .data(data)
            .build());
    }
}
