package xCloud.openAiChatModel.ali.stream;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import io.reactivex.Flowable;
import org.springframework.stereotype.Component;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.Arrays;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/11/11 15:18
 * @ClassName AliChatUtil
 */
@Component
public class AliChatUtil {

    /**
     * 使用 DashScope API 进行流式文本生成。
     *
     * @param prompt 用户提示词
     * @param model  模型名称，默认为 "qwen-plus"
     */
//    public static void streamChat(String prompt, String model) {
//        String apiKey = System.getenv("DASHSCOPE_API_KEY");
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
//                                String content = message.getOutput().getChoices().get(0).getMessage().getContent();
//                                String finishReason = message.getOutput().getChoices().get(0).getFinishReason();
//                                // 输出内容
//                                System.out.print(content);
//                                fullContent.append(content);
//                                // 当 finishReason 不为 null 时，表示是最后一个 chunk，输出用量信息
//                                if (finishReason != null && !"null".equals(finishReason)) {
//                                    System.out.println("\n--- 请求用量 ---");
//                                    System.out.println("输入 Tokens：" + message.getUsage().getInputTokens());
//                                    System.out.println("输出 Tokens：" + message.getUsage().getOutputTokens());
//                                    System.out.println("总 Tokens：" + message.getUsage().getTotalTokens());
//                                }
//                                System.out.flush(); // 立即刷新输出
//                            },
//                            // onError: 处理错误
//                            error -> {
//                                System.err.println("\n请求失败: " + error.getMessage());
//                                latch.countDown();
//                            },
//                            // onComplete: 完成回调
//                            () -> {
//                                System.out.println(); // 换行
//                                // System.out.println("完整响应: " + fullContent.toString());
//                                latch.countDown();
//                            }
//                    );
//            // 主线程等待异步任务完成
//            latch.await();
//            System.out.println("程序执行完成");
//        } catch (Exception e) {
//            System.err.println("请求异常: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }

    /**
     * 使用 DashScope API 进行流式文本生成，并支持流式返回给前端。
     *
     * @param apiKey API 密钥（如果为 null，则从环境变量 DASHSCOPE_API_KEY 获取）
     * @param prompt 用户提示词
     * @param model  模型名称，默认为 "qwen-plus"
     * @return Flux<String> 流式响应，每个元素为内容片段（最后一个元素包含用量信息，如果适用）
     */
    public static Flux<String> streamChatToFrontend(String apiKey, String prompt, String model) {
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = System.getenv("DASHSCOPE_API_KEY");
            if (apiKey == null || apiKey.isEmpty()) {
                return Flux.error(new RuntimeException("请设置环境变量 DASHSCOPE_API_KEY"));
            }
        }

        Generation gen = new Generation();

        GenerationParam param = GenerationParam.builder()
                .apiKey(apiKey)
                .model(model != null ? model : "qwen-plus")
                .messages(Arrays.asList(
                        Message.builder()
                                .role(Role.USER.getValue())
                                .content(prompt)
                                .build()
                ))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .incrementalOutput(true) // 开启增量输出，流式返回
                .build();

        try {
            Flowable<GenerationResult> resultFlowable = gen.streamCall(param);

            // 将 RxJava Flowable 转换为 Reactor Flux，并优化调度策略
            Flux<GenerationResult> resultFlux = RxJava2Adapter.flowableToFlux(resultFlowable)
                    // IO 线程用于调用外部接口（如 embedding API、数据库 IO）
                    .subscribeOn(Schedulers.boundedElastic())
                    // 计算线程用于处理响应（如 embedding 结果计算、聚合）
                    .publishOn(Schedulers.parallel());
            return resultFlux
                    .map(message -> {
                        String content = message.getOutput().getChoices().get(0).getMessage().getContent();
                        String finishReason = message.getOutput().getChoices().get(0).getFinishReason();

                        if (finishReason != null && !"null".equals(finishReason)) {
                            // 最后一个 chunk，附加用量信息
                            String usageInfo = "\n--- 请求用量 ---\n" +
                                    "输入 Tokens：" + message.getUsage().getInputTokens() + "\n" +
                                    "输出 Tokens：" + message.getUsage().getOutputTokens() + "\n" +
                                    "总 Tokens：" + message.getUsage().getTotalTokens();
                            // + usageInfo
                            return content;
                        }
                        return content;
                    });
//                    .doOnNext(chunk -> System.out.print(chunk)) // 可选：同时打印到控制台用于调试
//                    .doOnComplete(() -> System.out.println("\n程序执行完成")); // 可选：完成时打印

        } catch (Exception e) {
            return Flux.error(e);
        }
    }
}
