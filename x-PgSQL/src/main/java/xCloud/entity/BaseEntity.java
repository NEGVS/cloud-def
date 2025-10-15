package xCloud.entity;

import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Description 定义BaseEntity支持乐观锁。
 * @Author Andy Fan
 * @Date 2025/10/15 17:51
 * @ClassName BaseEntity
 */
@Data
public abstract class BaseEntity {
    @Version
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
