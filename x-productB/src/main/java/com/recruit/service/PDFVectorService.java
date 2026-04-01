package com.recruit.service;

import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.request.InsertReq;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.File;
import java.util.*;

@Service
public class PDFVectorService {

    @Autowired
    private MilvusClientV2 milvusClient;

    @Autowired
    private EmbeddingService embeddingService;

    public void processPDF(String pdfPath, Integer categoryId) throws Exception {
        PDDocument doc = PDDocument.load(new File(pdfPath));
        String text = new PDFTextStripper().getText(doc);
        doc.close();

        String[] paragraphs = text.split("\n\n");
        List<Map<String, Object>> data = new ArrayList<>();

        for (String para : paragraphs) {
            if (para.trim().isEmpty()) continue;

            Map<String, Object> row = new HashMap<>();
            row.put("job_desc", para);
            row.put("category_id", categoryId);
            row.put("vector", embeddingService.generateEmbedding(para));
            data.add(row);
        }

        InsertReq insertReq = InsertReq.builder()
            .collectionName("job_collection")
            .data(data)
            .build();

        milvusClient.insert(insertReq);
    }
}
