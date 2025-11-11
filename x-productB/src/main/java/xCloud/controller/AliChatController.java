package xCloud.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import xCloud.openAiChatModel.ali.stream.AliChatUtil;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/11/11 15:34
 * @ClassName AliChatController
 */
@Slf4j
@Tag(name = "AliChatController", description = "AliChatController")
@RestController("ali")
public class AliChatController {

    @Operation(summary = "0 streamChat")
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamChat(@RequestParam String prompt) {
        return AliChatUtil.streamChatToFrontend(null, prompt, null)
                .map(content -> ServerSentEvent.<String>builder()
                        .data(content)
                        .build());
    }
}
