package xCloud.openAiChatModel.controller;

import dev.langchain4j.model.chat.ChatModel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description In this case, an instance of OpenAiChatModel (an implementation of a ChatModel) will be automatically created, and you can autowire it where needed:
 * @Author Andy Fan
 * @Date 2025/9/4 19:36
 * @ClassName ChatController
 * If you need an instance of a StreamingChatModel, use the streaming-chat-model instead of the chat-model properties:
 * <p>
 * langchain4j.open-ai.streaming-chat-model.api-key=${OPENAI_API_KEY}
 */
@Tag(name = "chatController Management", description = "APIs for managing chatControllers")
@RequestMapping("/chatController")
@RestController
public class ChatController {


    ChatModel chatModel;

    public ChatController(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Operation(
            summary = "Search chatControllers with pagination",
            description = "Retrieve a paginated list of chatControllers based on search criteria"
    )
    @GetMapping("/chat")
    public String model(@RequestParam(value = "message", defaultValue = "Hello") String message) {
        return chatModel.chat(message);
    }
}
