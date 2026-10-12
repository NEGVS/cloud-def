package xCloud.service;

import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.request.QueryReq;
import io.milvus.v2.service.vector.response.QueryResp;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import xCloud.dto.MilvusVectorPageRequest;
import xCloud.dto.MilvusVectorPageResponse;
import xCloud.dto.MilvusVectorVO;

import java.util.*;

/**
 * ============向量存储服务============
 * 基于 Milvus 2.x SDK 实现向量存储与检索
 *
 * 主要功能：
 * 1. 分页查询向量数据（支持文本超长截断）
 * 2. 向量数据导出与管理
 *
 * @author Claude
 * @date 2026-08-27
 */
@Slf4j
@Service
public class VectorStoreService {

    @Lazy
    @Resource
    private MilvusClientV2 milvusClientV2;

    @Value("${vector.collection:andy_vectorsV2}")
    private String defaultCollectionName;

    /**
     * ============分页查询向量数据============
     * 支持分页返回Milvus中的向量数据，包括ID、文本、向量
     *
     * @param request 分页请求参数
     * @return 分页响应数据
     */
    public MilvusVectorPageResponse pageQuery(MilvusVectorPageRequest request) {
        String targetCollection = (request.getCollectionName() != null && !request.getCollectionName().isEmpty()) ? request.getCollectionName() : defaultCollectionName;
        int pageNum = request.getPageNum() != null && request.getPageNum() > 0 ? request.getPageNum() : 1;
        int pageSize = request.getPageSize() != null && request.getPageSize() > 0 ? request.getPageSize() : 10;
        if (pageSize > 100) pageSize = 100; // ============限制最大页大小防止内存溢出============

        // ============执行查询============
        int offset = (pageNum - 1) * pageSize;
        QueryResp queryResp = milvusClientV2.query(QueryReq.builder()
                .collectionName(targetCollection)
                .filter("id >= 0") // 查询所有数据
                .outputFields(Arrays.asList("id", "text", "vector"))
                .limit((long) pageSize)
                .offset((long) offset)
                .build());

        // ============组装返回数据============
        // 注意：getQueryResults() 返回 List<QueryResp.QueryResult>，字段 Map 需通过 getEntity() 获取
        List<MilvusVectorVO> records = new ArrayList<>();
        queryResp.getQueryResults().forEach(result -> {
            Map<String, Object> entity = result.getEntity();
            if (entity == null) return;

            MilvusVectorVO vo = new MilvusVectorVO();
            Object idObj = entity.get("id");
            if (idObj != null) vo.setId(Long.valueOf(idObj.toString()));

            // ============处理文本（超过5000字符截断）============
            Object textObj = entity.get("text");
            String text = textObj != null ? textObj.toString() : "";
            vo.setOriginalTextLength(text.length());
            if (text.length() > 5000) {
                vo.setText(text.substring(0, 5000));
                vo.setTruncated(true);
            } else {
                vo.setText(text);
                vo.setTruncated(false);
            }

            // ============处理向量============
            Object vectorObj = entity.get("vector");
            if (vectorObj instanceof List) {
                vo.setVector((List<Float>) vectorObj);
            }

            records.add(vo);
        });

        // ============计算总数（使用简单估算）============
        // Milvus V2暂不支持高效count，使用offset+当前查询数量估算
        long estimatedTotal = offset + records.size() + (records.size() == pageSize ? pageSize : 0);

        // ============封装响应============
        MilvusVectorPageResponse response = new MilvusVectorPageResponse();
        response.setRecords(records);
        response.setPageNum(pageNum);
        response.setPageSize(pageSize);
        response.setTotal(estimatedTotal);
        response.setTotalPages((int) Math.ceil((double) estimatedTotal / pageSize));
        response.setCollectionName(targetCollection);

        log.info("分页查询完成，collection={}, pageNum={}, pageSize={}, 返回{}条记录", targetCollection, pageNum, pageSize, records.size());
        return response;
    }
}
