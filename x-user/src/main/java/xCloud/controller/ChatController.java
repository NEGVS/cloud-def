package xCloud.controller;

import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.ChatLanguageModel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import xCloud.service.Assistant;
import xCloud.service.StreamChatLanguageModel;

/**
 * @Description In this case, an instance of OpenAiChatModel (an implementation of a ChatLanguageModel)
 * will be automatically created, and you can autowire it where needed:
 * @Author Andy Fan
 * @Date 2025/4/21 14:30
 * @ClassName ChatController
 */
@RestController
@Tag(name = "ChatController", description = "ChatController")
public class ChatController {

    private ChatLanguageModel chatLanguageModel;


    @Autowired
    StreamChatLanguageModel streamChatLanguageModel;

    @Autowired
    Assistant assistant;

    public ChatController(ChatLanguageModel chatLanguageModel) {
        this.chatLanguageModel = chatLanguageModel;
    }


    /**
     * 1. chatLanguageModel
     *
     * @param message
     * @return
     */
    @Operation(summary = "1. chatLanguageModel")
    @GetMapping("/chat")
    public String model(@RequestParam(value = "message", defaultValue = "hello") String message) {
        return chatLanguageModel.chat(message);
    }

    /**
     * 2. assistant
     *
     * @param message
     * @return
     */
    @Operation(summary = "2. assistant")
    @GetMapping("/chat2")
    public String chat(@RequestParam(value = "message", defaultValue = "hello") String message) {
        return assistant.chat(message);
    }

    /**
     * 3-流式返回
     * const eventSource = new EventSource('/chat?message=hello');
     * eventSource.onmessage = function(event) {
     *     console.log('New token:', event.data);
     *     // Append token to UI (e.g., a chat window)
     * };
     * eventSource.onerror = function() {
     *     console.error('Stream error');
     *     eventSource.close();
     * };
     */
    @Operation(summary = "3. assistant stream")
    @GetMapping("/chatStream")
    public Flux<String> chatStream(@RequestParam(value = "message", defaultValue = "hello") String message) {

        return Flux.generate((synchronousSink) -> {
            streamChatLanguageModel.generate(message, new StreamingResponseHandler() {
                @Override
                public void onNext(String s) {
                    synchronousSink.next(s);
                }

                @Override
                public void onError(Throwable throwable) {
                    synchronousSink.error(throwable);
                }
            });
        });

    }
}
