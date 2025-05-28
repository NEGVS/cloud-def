package xCloud.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xCloud.service.RabbitMQService;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/5/28 10:09
 * @ClassName RabbitMQController
 */
@RestController
@RequestMapping("/rabbitmq")
@Tag(name = "RabbitMQController", description = "RabbitMQController")
public class RabbitMQController {

    @Autowired
    private RabbitMQService rabbitMQService;

    @GetMapping("/sendMessage")
    @Operation(summary = "sendMessage")
    public String sendMessage(@RequestParam String message) {
        rabbitMQService.sendMessage(message);
        return "success";
    }
}
