package xCloud.entity.dto;

import lombok.Data;

import java.util.List;

/**
 * @Description Milvus 集合结构对应的实体类
 * @Author Andy Fan
 * @Date 2025/10/10 17:18
 * @ClassName VectorEntity
 */
@Data
public class VectorDTO {
    /**
     * id
     */
    private String id;
    /**
     * 向量
     */
    private List<Double> vector;
}
