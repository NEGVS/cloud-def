package xCloud.service;

import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.stereotype.Component;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/4/22 19:16
 * @ClassName ChatStreamLanguageModel
 */
@Component
public class StreamChatLanguageModel {

    private final OpenAiStreamingChatModel model;

    public StreamChatLanguageModel() {
        this.model = OpenAiStreamingChatModel.builder()
                .apiKey("xxxxxx")
                .modelName("gpt-4o-mini")
                .build();
    }

    public void generate(String message, StreamingResponseHandler handler) {
        // 最简单的 String 输入输出
        model.chat(message, new StreamingChatResponseHandler() {
            @Override
            public void onPartialResponse(String s) {
                try {
                    Thread.sleep(190);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                handler.onNext(s);
            }

            @Override
            public void onCompleteResponse(ChatResponse chatResponse) {

                try {
                    Thread.sleep(190);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
                handler.onError(throwable);
            }
        });
    }
}
