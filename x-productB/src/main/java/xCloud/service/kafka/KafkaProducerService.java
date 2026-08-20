package xCloud.service.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import xCloud.config.KafkaConfig;
import xCloud.event.ResumeParsedEvent;
import xCloud.event.ResumeUploadEvent;
import xCloud.event.ResumeVectorizedEvent;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Kafka消息生产者服务
 * 用途：发送各种业务事件到Kafka
 * @author Claude
 * @date 2026-08-18
 */
@Slf4j
@Service
public class KafkaProducerService {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * 发送简历上传事件
     */
    public void sendResumeUploadEvent(ResumeUploadEvent event) {
        event.setEventId(UUID.randomUUID().toString());
        sendMessage(KafkaConfig.Topics.RESUME_UPLOAD, event.getResumeId().toString(), event);
    }

    /**
     * 发送简历解析完成事件
     */
    public void sendResumeParsedEvent(ResumeParsedEvent event) {
        event.setEventId(UUID.randomUUID().toString());
        sendMessage(KafkaConfig.Topics.RESUME_PARSED, event.getResumeId().toString(), event);
    }

    /**
     * 发送简历向量化完成事件
     */
    public void sendResumeVectorizedEvent(ResumeVectorizedEvent event) {
        event.setEventId(UUID.randomUUID().toString());
        sendMessage(KafkaConfig.Topics.RESUME_VECTORIZED, event.getResumeId().toString(), event);
    }

    /**
     * 通用消息发送方法
     */
    private void sendMessage(String topic, String key, Object message) {
        try {
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, message);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("✅ Kafka消息发送成功：topic={}, key={}, partition={}, offset={}",
                            topic, key, result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                } else {
                    log.error("❌ Kafka消息发送失败：topic={}, key={}", topic, key, ex);
                }
            });

        } catch (Exception e) {
            log.error("❌ Kafka消息发送异常：topic={}, key={}", topic, key, e);
        }
    }
}
