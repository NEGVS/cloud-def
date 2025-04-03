package xcloud.xproduct.config.elasticsearch;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import xcloud.xproduct.domain.XProducts;

import java.util.List;

public interface XProductRepository extends ElasticsearchRepository<XProducts, String> {


    /**
     * 根据名称查询
     *
     * @param name name
     * @return list
     */
    List<XProducts> findByName(String name);

    /**
     * 根据名称模糊查找（包含）
     *
     * @param name name
     * @return list
     */
    List<XProducts> findByNameContaining(String name);
}
