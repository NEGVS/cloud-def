package xCloud.service.kafka;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import xCloud.config.KafkaConfig;
import xCloud.entity.es.CandidateResumeDocument;
import xCloud.event.ResumeParsedEvent;
import xCloud.event.ResumeVectorizedEvent;
import xCloud.repository.CandidateResumeRepository;
import xCloud.service.vector.EmbeddingService;

import java.util.Date;
import java.util.List;

/**
 * Kafka消息消费者服务
 * 用途：消费Kafka消息并进行后续处理
 * @author Claude
 * @date 2026-08-18
 */
@Slf4j
@Service
public class KafkaConsumerService {

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    private CandidateResumeRepository candidateResumeRepository;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    /**
     * 消费简历解析完成事件 → 进行向量化
     */
    @KafkaListener(topics = KafkaConfig.Topics.RESUME_PARSED, groupId = "${spring.kafka.consumer.group-id}")
    public void consumeResumeParsed(@Payload String message,
                                    @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                    @Header(KafkaHeaders.OFFSET) long offset,
                                    Acknowledgment acknowledgment) {
        try {
            log.info("📥 接收到简历解析完成事件：partition={}, offset={}", partition, offset);

            ResumeParsedEvent event = JSON.parseObject(message, ResumeParsedEvent.class);

            // ============1-向量化简历内容============
            String textToVectorize = buildVectorizeText(event);
            List<Float> vector = embeddingService.embed(textToVectorize);

            // ============2-发送向量化完成事件============
            ResumeVectorizedEvent vectorizedEvent = new ResumeVectorizedEvent();
            vectorizedEvent.setResumeId(event.getResumeId());
            vectorizedEvent.setVector(vector);
            vectorizedEvent.setCollectionName("candidate_resume");
            vectorizedEvent.setEventTime(new Date());
            kafkaProducerService.sendResumeVectorizedEvent(vectorizedEvent);

            // ============3-手动提交offset============
            acknowledgment.acknowledge();
            log.info("✅ 简历向量化完成：resumeId={}, vectorDim={}", event.getResumeId(), vector.size());

        } catch (Exception e) {
            log.error("❌ 简历向量化失败：partition={}, offset={}", partition, offset, e);
            // 不提交offset，消息会重试
        }
    }

    /**
     * 消费简历向量化完成事件 → 写入ES
     */
    @KafkaListener(topics = KafkaConfig.Topics.RESUME_VECTORIZED, groupId = "${spring.kafka.consumer.group-id}")
    public void consumeResumeVectorized(@Payload String message,
                                        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                        @Header(KafkaHeaders.OFFSET) long offset,
                                        Acknowledgment acknowledgment) {
        try {
            log.info("📥 接收到简历向量化完成事件：partition={}, offset={}", partition, offset);

            ResumeVectorizedEvent event = JSON.parseObject(message, ResumeVectorizedEvent.class);

            // ============写入ES（增量索引）============
            // 注意：这里简化处理，实际需要从数据库加载完整的Resume数据
            CandidateResumeDocument doc = buildESDocument(event);
            candidateResumeRepository.save(doc);

            // ============手动提交offset============
            acknowledgment.acknowledge();
            log.info("✅ 简历索引到ES成功：resumeId={}", event.getResumeId());

        } catch (Exception e) {
            log.error("❌ 简历索引到ES失败：partition={}, offset={}", partition, offset, e);
            // 不提交offset，消息会重试
        }
    }

    /**
     * 构建向量化文本（拼接关键字段）
     */
    private String buildVectorizeText(ResumeParsedEvent event) {
        StringBuilder sb = new StringBuilder();
        if (event.getName() != null) sb.append(event.getName()).append(" ");
        if (event.getExpectedPosition() != null) sb.append(event.getExpectedPosition()).append(" ");
        if (event.getSkills() != null) sb.append(event.getSkills()).append(" ");
        if (event.getRawContent() != null && event.getRawContent().length() < 2000) {
            sb.append(event.getRawContent());
        }
        return sb.toString().trim();
    }

    /**
     * 构建ES文档（简化版）
     */
    private CandidateResumeDocument buildESDocument(ResumeVectorizedEvent event) {
        // TODO: 实际应该从数据库加载完整的Resume数据
        CandidateResumeDocument doc = new CandidateResumeDocument();
        doc.setId(event.getResumeId().toString());
        doc.setContentVector(event.getVector());
        doc.setUploadTime(event.getEventTime());
        doc.setUpdateTime(event.getEventTime());
        doc.setStatus("待筛选");
        doc.setSource("upload");
        return doc;
    }
}
