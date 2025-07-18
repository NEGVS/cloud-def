package xcloud.xproduct.controller;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xcloud.xproduct.config.kafka.KafkaProducer;
import xcloud.xproduct.config.kafka.KafkaService;
import xcloud.xproduct.entity.ResultEntity;

/**
 * @Description test use  kafka
 * @Author Andy Fan
 * @Date 2025/5/27 19:57
 * @ClassName KafkaController
 */
@RestController
@RequestMapping("/kafka")
@Slf4j
public class KafkaController {

    @Autowired
    private KafkaService kafkaService;

    @Resource
    KafkaProducer kafkaProducer;

    /**
     * kafka发送消息to test-topic
     */
    @GetMapping("/sendTestMessage")
    public ResultEntity<String> sendMessage(@RequestParam String message) {
        log.info("\n向kafka test-topic发送消息：" + message);
        kafkaService.sendMessage("{\"id\":1,\"content\":\"" + message + "\"}");

        return ResultEntity.success("向kafka test-topic发送消息成功，消息内容：" + message);
    }

    /**
     * kafka发送消息to andy-topic
     */
    @GetMapping("/sendAndyMessage")
    public ResultEntity<String> sendProduceMessage(@RequestParam String message) {
        log.info("向kafka andy-topic发送消息成功，消息内容：" + message);
        kafkaProducer.sendMessage(message);
        return ResultEntity.success("向kafka andy-topic发送消息成功，消息内容：" + message);
    }
}
