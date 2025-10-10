package xCloud.entity;

import lombok.Data;

/**
 * @Description Milvus 集合结构对应的实体类
 * @Author Andy Fan
 * @Date 2025/10/10 17:18
 * @ClassName VectorEntity
 */
@Data
public class VectorEntity {
    private Long id;
    private float[] vector;
}
