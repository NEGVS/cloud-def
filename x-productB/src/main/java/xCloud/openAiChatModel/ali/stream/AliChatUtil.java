package xCloud.openAiChatModel.ali.stream;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import io.reactivex.Flowable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import xCloud.entity.constant.AliConstant;

import java.util.Arrays;

/**
 * @Description 使用的是阿里云百炼
 * @Author Andy Fan
 * @Date 2025/11/11 15:18
 * @ClassName AliChatUtil
 */
@Slf4j
@Component
public class AliChatUtil {

    /*
     * 使用 DashScope API 进行流式文本生成。
     *
     * @param prompt 用户提示词
     * @param model  模型名称，默认为 "qwen-plus"
     */
//    public static void streamChat(String prompt, String model) {
//        String apiKey = aliApiKey;
//
//        Generation gen = new Generation();
//        CountDownLatch latch = new CountDownLatch(1);
//
//        GenerationParam param = GenerationParam.builder()
//                .apiKey(apiKey)
//                .model(model != null ? model : "qwen-plus")
//                .messages(Arrays.asList(
//                        Message.builder()
//                                .role(Role.USER.getValue())
//                                .content(prompt)
//                                .build()
//                ))
//                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
//                .incrementalOutput(true) // 开启增量输出，流式返回
//                .build();
//
//        try {
//            Flowable<GenerationResult> result = gen.streamCall(param);
//            StringBuilder fullContent = new StringBuilder();
//            System.out.print("AI: ");
//            result
//                    .subscribeOn(Schedulers.io()) // IO线程执行请求
//                    .observeOn(Schedulers.computation()) // 计算线程处理响应
//                    .subscribe(
//                            // onNext: 处理每个响应片段
//                            message -> {
//                                String content = message.getOutput().getChoices().getFirst().getMessage().getContent();
//                                String finishReason = message.getOutput().getChoices().getFirst().getFinishReason();
//                                // 输出内容
//                                System.out.print(content);
//                                fullContent.append(content);
//                                // 当 finishReason 不为 null 时，表示是最后一个 chunk，输出用量信息
//                                if (finishReason != null && !"null".equals(finishReason)) {
//                                    log.info("\n\n--- 请求用量 ---");
//                                    log.info("输入 Tokens：" + message.getUsage().getInputTokens());
//                                    log.info("输出 Tokens：" + message.getUsage().getOutputTokens());
//                                    log.info("总 Tokens：" + message.getUsage().getTotalTokens());
//                                }
//                                System.out.flush(); // 立即刷新输出
//                            },
//                            // onError: 处理错误
//                            error -> {
//                                System.err.println("\n\n请求失败: " + error.getMessage());
//                                latch.countDown();
//                            },
//                            // onComplete: 完成回调
//                            () -> {
//                                log.info(); // 换行
//                                // log.info("完整响应: " + fullContent.toString());
//                                latch.countDown();
//                            }
//                    );
//            // 主线程等待异步任务完成
//            latch.await();
//            log.info("程序执行完成");
//        } catch (Exception e) {
//            System.err.println("请求异常: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }


    /**
     * 使用 DashScope API 进行流式文本生成 使用默认apikey、模型
     */
    public static Flux<String> streamChatToFrontend(String prompt) {
        log.info("📝 [流式对话] 使用默认配置开始流式对话");
        return streamChatToFrontend(null, prompt, null);
    }

    /**
     * 使用 DashScope API 进行流式文本生成，并支持流式返回给前端。
     *
     * @param apiKey API 密钥
     * @param prompt 用户提示词
     * @param model  模型名称，默认为 "qwen-plus"
     * @return Flux<String> 流式响应，每个元素为内容片段（最后一个元素包含用量信息，如果适用）
     */
    public static Flux<String> streamChatToFrontend(String apiKey, String prompt, String model) {
        // 记录开始时间
        long startTime = System.currentTimeMillis();
        String sessionId = java.util.UUID.randomUUID().toString().substring(0, 8);

        log.info("\n" + "=".repeat(80));
        log.info("🚀 [流式对话开始] SessionID: {}", sessionId);
        log.info("📥 [用户问题]: {}", prompt);
        log.info("⚙️  [模型配置]: {}", model != null ? model : AliConstant.CHAT_MODEL_NAME);
        log.info("=".repeat(80));

        // 处理系统提示词
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(prompt);
        stringBuilder.append("回答内容要精简，不要说废话，控制在100字以内。");

        Generation gen = new Generation();

        GenerationParam param = GenerationParam.builder()
                .apiKey(apiKey)
                .model(model != null ? model : AliConstant.CHAT_MODEL_NAME)
                .messages(Arrays.asList(
                        Message.builder()
                                .role(Role.USER.getValue())
                                .content(stringBuilder.toString())
                                .build()
                ))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .incrementalOutput(true) // 开启增量输出，流式返回
                .build();

        log.info("🔗 [API调用] 正在连接阿里云DashScope...");

        try {
            Flowable<GenerationResult> resultFlowable = gen.streamCall(param);

            // 将 RxJava Flowable 转换为 Reactor Flux
            Flux<GenerationResult> resultFlux = RxJava2Adapter.flowableToFlux(resultFlowable)
                    .subscribeOn(Schedulers.boundedElastic())
                    .publishOn(Schedulers.parallel());

            log.info("✅ [连接成功] 开始接收流式响应\n");

            // 用于累积完整内容
            StringBuilder fullContent = new StringBuilder();
            final int[] chunkCount = {0}; // 使用数组以便在lambda中修改

            return resultFlux
                    .map(message -> {
                        String content = message.getOutput().getChoices().getFirst().getMessage().getContent();
                        String finishReason = message.getOutput().getChoices().getFirst().getFinishReason();

                        // 累积内容
                        fullContent.append(content);
                        chunkCount[0]++;

                        if (finishReason != null && !"null".equals(finishReason)) {
                            // 最后一个chunk，打印完整信息
                            long endTime = System.currentTimeMillis();
                            long duration = endTime - startTime;

                            log.info("\n" + "=".repeat(80));
                            log.info("✅ [流式对话完成] SessionID: {}", sessionId);
                            log.info("⏱️  [耗时]: {} ms ({} 秒)", duration, String.format("%.2f", duration / 1000.0));
                            log.info("📊 [统计]: 共接收 {} 个数据块", chunkCount[0]);
                            log.info("📝 [完整回复]:\n{}", fullContent.toString());
                            log.info("\n💰 [Token用量]:");
                            log.info("   • 输入 Tokens: {}", message.getUsage().getInputTokens());
                            log.info("   • 输出 Tokens: {}", message.getUsage().getOutputTokens());
                            log.info("   • 总 Tokens: {}", message.getUsage().getTotalTokens());
                            log.info("=".repeat(80) + "\n");
                        }

                        return content;
                    })
                    .doOnError(error -> {
                        log.error("\n" + "=".repeat(80));
                        log.error("❌ [流式对话失败] SessionID: {}", sessionId);
                        log.error("❌ [错误信息]: {}", error.getMessage());
                        log.error("=".repeat(80) + "\n", error);
                    })
                    .doOnComplete(() -> {
                        log.info("🏁 [流式传输] SessionID: {} 数据流传输完毕\n", sessionId);
                    });

        } catch (Exception e) {
            log.error("\n" + "=".repeat(80));
            log.error("❌ [API调用异常] SessionID: {}", sessionId);
            log.error("❌ [异常信息]: {}", e.getMessage());
            log.error("=".repeat(80) + "\n", e);
            return Flux.error(e);
        }
    }


    /**
     * 1-文本对话，同步回答，设定角色
     *
     * @param userMessage   userMessage
     * @param systemMessage systemMessage
     * @return String
     */
    public String chat(String userMessage, String systemMessage) {
        long startTime = System.currentTimeMillis();
        String sessionId = java.util.UUID.randomUUID().toString().substring(0, 8);

        try {
            if (ObjectUtil.isEmpty(systemMessage)) {
                systemMessage = "You are a helpful assistant.";
            }

            log.info("\n" + "=".repeat(80));
            log.info("🚀 [同步对话开始] SessionID: {}", sessionId);
            log.info("📥 [用户问题]: {}", userMessage);
            log.info("🎭 [角色设置]: {}", systemMessage);
            log.info("=".repeat(80));

            if (ObjectUtil.isEmpty(userMessage)) {
                log.warn("⚠️  [参数错误] 用户消息为空");
                return "";
            }

            // 处理系统提示词
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(systemMessage);
            stringBuilder.append("回答内容要精简，不要说废话，控制在100字以内，条理清晰，可以按照 1，2，3点回答。");

            OpenAIClient client = OpenAIOkHttpClient.builder()
                    .apiKey(AliConstant.API_KEY)
                    .baseUrl(AliConstant.BASE_URL)
                    .build();

            // 创建 ChatCompletion 参数
            ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                    .model(AliConstant.CHAT_MODEL_NAME)
                    .addSystemMessage(stringBuilder.toString())
                    .addUserMessage(userMessage)
                    .build();

            // 发送请求并获取响应
            log.info("🔗 [API调用] 正在请求模型，请稍等...");
            ChatCompletion chatCompletion = client.chat().completions().create(params);
            String content = chatCompletion.choices().getFirst().message().content().orElse("未返回有效内容");

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            log.info("\n" + "=".repeat(80));
            log.info("✅ [同步对话完成] SessionID: {}", sessionId);
            log.info("⏱️  [耗时]: {} ms ({} 秒)", duration, String.format("%.2f", duration / 1000.0));
            log.info("📝 [AI回复]:\n{}", content);
            log.info("=".repeat(80) + "\n");

            return content;

        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            log.error("\n" + "=".repeat(80));
            log.error("❌ [同步对话失败] SessionID: {}", sessionId);
            log.error("⏱️  [已耗时]: {} ms", duration);
            log.error("❌ [错误信息]: {}", e.getMessage());
            log.error("📚 [参考文档]: https://help.aliyun.com/zh/model-studio/developer-reference/error-code");
            log.error("=".repeat(80) + "\n", e);
        }
        return "";
    }
}
