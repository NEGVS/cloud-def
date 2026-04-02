package xCloud.openAiChatModel.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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

@Service
public class JobService {

    @Autowired
    private MilvusClientV2 milvusClient;

    @Autowired
    private EmbeddingModel embeddingModel;

    public Object apply(long id, long cid) {
        return null;
    }

    public void importPdfToVector(String pdfPath, Integer categoryId, String jobTitle) throws Exception {
        PDDocument doc = PDDocument.load(new File(pdfPath));
        String text = new PDFTextStripper().getText(doc);
        doc.close();

        String[] paragraphs = text.split("\n\n");
//        List<Map<String, Object>> data = new ArrayList<>();
        List<JsonObject> data = new ArrayList<>(); // 这里改成 JsonObject
        for (String para : paragraphs) {
            if (para.trim().isEmpty()) continue;

            Embedding embedding = embeddingModel.embed(para).content();
            float[] vectorArray = embedding.vector();
            List<Float> vector = new ArrayList<>();
            for (float v : vectorArray) {
                vector.add(v);
            }

//            Map<String, Object> row = new HashMap<>();
            JsonObject row = new JsonObject();
            row.addProperty("job_title", jobTitle);
            row.addProperty("job_desc", para);
            row.addProperty("category_id", categoryId);
            // vector 是数组，用 add 不能用 addProperty
            JsonArray jsonArray = new JsonArray();
            for (Float f : vector) {
                jsonArray.add(f);
            }
            row.add("vector", jsonArray);
            data.add(row);
        }

        milvusClient.insert(InsertReq.builder()
                .collectionName("job_collection")
                .data(data)
                .build());
    }
}
