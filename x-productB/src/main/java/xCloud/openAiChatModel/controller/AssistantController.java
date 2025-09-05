package xCloud.openAiChatModel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xCloud.openAiChatModel.service.Assistant;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/9/4 19:43
 * @ClassName AssistantController
 */
@Tag(name = "assistantController Management", description = "APIs for managing assistantControllers")
@RequestMapping("/assistantController")

@RestController
public class AssistantController {
    @Autowired
    Assistant assistant;

    @Operation(
            summary = "Search assistantControllers with pagination",
            description = "Retrieve a paginated list of assistantControllers based on search criteria"
    )
    @GetMapping("/chat")
    public String chat(String message) {
        return assistant.chat(message);
    }
}
