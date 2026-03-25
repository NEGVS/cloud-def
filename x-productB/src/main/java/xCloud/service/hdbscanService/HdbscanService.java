package xCloud.service.hdbscanService;

import io.milvus.v2.client.MilvusClientV2;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2026/3/22 23:57
 * @ClassName HdbscanService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HdbscanService {

    private final WebClient webClient;
    @Resource
    private MilvusClientV2 milvusClient;

    @Value("${hdbscan.service.url}")
    private String hdbscanUrl;

    /**
     * 调用 Flask 的 HDBSCAN 聚类服务
     *
     * @param vectors 向量列表 List<List<Float>>
     * @return 每个向量的 cluster_id 列表（-1 表示噪声）
     */
    public List<Integer> cluster(List<List<Float>> vectors) {
        String url = hdbscanUrl + "/cluster";

        return webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(vectors)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Integer>>() {
                })
                .doOnError(e -> log.error("调用 HDBSCAN 服务失败: {}", url, e))
                .block();  // 同步调用（生产可改异步返回 Mono）
    }

    /**
     * 聚类 + 插入 Milvus（示例）
     */
    public void clusterAndInsertToMilvus(List<List<Float>> vectors, String collectionName) {
        List<Integer> clusterIds = cluster(vectors);

        if (clusterIds == null || clusterIds.size() != vectors.size()) {
            throw new IllegalStateException("聚类结果数量不匹配");
        }

        // 假设你有 MilvusClient 封装类
//        milvusClient.insert(collectionName, vectors, clusterIds);  // 伪代码，实际用你的插入方法
        log.info("聚类完成并插入 Milvus，数量：{}", vectors.size());
    }


}
