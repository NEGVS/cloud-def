package xCloud.service.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import xCloud.config.KafkaConfig;
import xCloud.event.ResumeParsedEvent;
import xCloud.event.ResumeUploadEvent;
import xCloud.event.ResumeVectorizedEvent;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Kafka消息生产者服务
 * 用途：发送各种业务事件到Kafka
 *
 * ============开关感知============
 * 通过 ObjectProvider 按需获取 KafkaTemplate：
 * kafka.enabled=false 时 KafkaTemplate Bean 不存在，isEnabled() 返回 false，
 * 所有发送方法直接静默跳过（打一条 debug 日志），调用方无需判空，也不会抛异常、不会阻塞线程。
 *
 * @author Claude
 * @date 2026-08-18
 */
@Slf4j
@Service
public class KafkaProducerService {

    /** 延迟获取：KafkaTemplate 随 kafka.enabled 开关决定是否存在 */
    private final ObjectProvider<KafkaTemplate<String, Object>> kafkaTemplateProvider;

    public KafkaProducerService(ObjectProvider<KafkaTemplate<String, Object>> kafkaTemplateProvider) {
        this.kafkaTemplateProvider = kafkaTemplateProvider;
    }

    /**
     * Kafka 是否可用（Bean 存在即为可用）
     */
    public boolean isEnabled() {
        return kafkaTemplateProvider.getIfAvailable() != null;
    }

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
     * Kafka 未启用时直接跳过，保证业务主流程不受影响
     */
    private void sendMessage(String topic, String key, Object message) {
        KafkaTemplate<String, Object> kafkaTemplate = kafkaTemplateProvider.getIfAvailable();
        if (kafkaTemplate == null) {
            // 未启用 Kafka：静默降级，业务照常返回
            log.debug("Kafka未启用，跳过消息发送：topic={}, key={}", topic, key);
            return;
        }

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
