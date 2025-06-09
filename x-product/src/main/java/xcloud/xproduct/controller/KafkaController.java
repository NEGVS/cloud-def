package xcloud.xproduct.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xcloud.xproduct.config.kafka.KafkaService;

/**
 * @Description test use  kafka
 * @Author Andy Fan
 * @Date 2025/5/27 19:57
 * @ClassName KafkaController
 */
@RestController
@RequestMapping("/kafka")
public class KafkaController {
    @Autowired
    private KafkaService kafkaService;

    @GetMapping("/sendMessage")
    public String sendMessage(@RequestParam String message) {
        kafkaService.sendMessage("{\"id\":1,\"content\":\"" + message + "\"}");
        return "success";
    }
}
