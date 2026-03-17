package xCloud.controller;

import cn.hutool.core.lang.UUID;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import xCloud.openAiChatModel.ali.stream.AliChatUtil;

/**
 * @Description 阿里大模型流式对话控制器，提供流式对话接口，支持 Server-Sent Events (SSE) 实时返回结果
 * @Author Andy Fan
 * @Date 2025/11/11 15:34
 * @ClassName AliChatController
 */
@Slf4j
@Tag(name = "AliChatController", description = "阿里大模型流式对话接口（SSE）")
@RestController
@RequestMapping("ali")
public class AliChatController {

    /**
     * 阿里大模型流式对话接口
     * 采用 SSE (Server-Sent Events) 方式实时返回对话结果，前端可实时接收流式数据
     */
    @Operation(summary = "阿里大模型流式对话",  // 完善摘要，更具语义
            description = "调用阿里大模型进行流式对话，通过 SSE 实时返回回复内容，支持逐字输出",
            parameters = {
                    @Parameter(
                            name = "prompt",
                            description = "用户对话输入的提示词/问题内容",
                            required = true,  // 标记参数为必填
                            example = "请介绍一下Java的流式编程",  // 提供示例值，方便测试
                            schema = @Schema(type = "string")
                    )
            })
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",  // 成功响应码
                    description = "流式响应成功，持续返回对话内容",
                    content = @Content(
                            mediaType = MediaType.TEXT_EVENT_STREAM_VALUE,
                            schema = @Schema(
                                    type = "string",
                                    description = "SSE 格式的流式文本内容",
                                    example = "data: Java的流式编程基于Stream API...\n\n"
                            )
                    )
            )
    })
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamChat(@RequestParam String prompt) {

        //空值校验
        if (prompt == null || prompt.trim().isEmpty()) {
            log.error("streamChat 参数 prompt 为空");
            return Flux.just(ServerSentEvent.<String>builder()
                    .data("参数错误：prompt 不能为空")
                    .build());
        }

        //转换为 SSE 格式并输出（返回给前端
        return AliChatUtil.streamChatToFrontend(null, prompt, null)
                // 异常处理：出错时返回友好提示
                .onErrorResume(error -> {
                    log.error("streamChat 错误：{}", error.getMessage());
                    return Flux.just("对话异常" + error.getMessage());
                })
                // 包装为 ServerSentEvent（SSE 标准格式）
                .map(content -> ServerSentEvent.<String>builder()
                        .id(UUID.randomUUID().toString()) // 可选：给每个消息加唯一ID
                        .event("chat-message") // 可选：指定事件类型，前端可按类型监听
                        .data(content) // 核心：流式内容
                        .build());
    }
}
