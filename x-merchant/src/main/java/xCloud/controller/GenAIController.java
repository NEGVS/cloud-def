package xCloud.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xCloud.genAI.GenAIService;


/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/7/2 17:06
 * @ClassName GenAIController
 */
@Tag(name = "GenAIController", description = "GenAIController")
@Slf4j
@RestController
@RequestMapping("/genAI")
public class GenAIController {

    @Resource
    private GenAIService genAIService;

    @Operation(summary = "generate Content")
    @PostMapping("/generateContent")
    public String generateContent(String prompt) {
        log.info("\nprompt: {}", prompt);
        String s = genAIService.generateContent(prompt);
        log.info("\nresult: {}", s);
        return s;
    }
}
