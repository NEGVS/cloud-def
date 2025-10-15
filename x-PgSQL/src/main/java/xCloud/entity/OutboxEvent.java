package xCloud.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.Instant;
import java.util.UUID;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/10/15 17:25
 * @ClassName OutboxEvent
 */
@Data
public class OutboxEvent {
    @Id
    private Long id;


    private UUID eventId;


    private String aggregateType;


    private String aggregateId;


    private String eventType;


    private String payload;


    private boolean published = false;


    private Instant createdAt = Instant.now();


    private Instant publishedAt;
}
