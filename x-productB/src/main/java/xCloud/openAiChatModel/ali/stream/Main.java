package xCloud.openAiChatModel.ali.stream;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/11/6 10:27
 * @ClassName Main
 */
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

public class Main {
    // 若使用新加坡地域的模型，请释放下列注释
    // static {
    //     Constants.baseHttpApiUrl="https://dashscope-intl.aliyuncs.com/api/v1";
    // }
    public static void main(String[] args) {
        // 1. 获取 API Key
        String apiKey = System.getenv("DASHSCOPE_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("请设置环境变量 DASHSCOPE_API_KEY");
            return;
        }

        // 2. 初始化 Generation 实例
        Generation gen = new Generation();
        CountDownLatch latch = new CountDownLatch(1);

        // 3. 构建请求参数
        GenerationParam param = GenerationParam.builder()
                .apiKey(apiKey)
                .model("qwen-plus")
                .messages(Arrays.asList(
                        Message.builder()
                                .role(Role.USER.getValue())
                                .content("这位女子身姿高挑，亭亭玉立，身高172厘米的她宛如一幅精心勾勒的画卷。她体态匀称，曲线玲珑有致，前凸后翘的身形展现出自然流畅的美感，既不失优雅又充满健康活力。她的美不是浮于表面的艳丽，而是一种由内而外散发的魅力——举手投足间带着自信与从容，仿佛阳光洒在湖面上，波光潋滟，令人心动。她的气质如兰，清雅中透着坚韧，既有女性的柔美，又蕴含独立的力量，令人过目难忘。")
                                .build()
                ))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .incrementalOutput(true) // 开启增量输出，流式返回
                .build();
        // 4. 发起流式调用并处理响应
        try {
            Flowable<GenerationResult> result = gen.streamCall(param);
            StringBuilder fullContent = new StringBuilder();
            System.out.print("AI: ");
            result
                    .subscribeOn(Schedulers.io()) // IO线程执行请求
                    .observeOn(Schedulers.computation()) // 计算线程处理响应
                    .subscribe(
                            // onNext: 处理每个响应片段
                            message -> {
                                String content = message.getOutput().getChoices().get(0).getMessage().getContent();
                                String finishReason = message.getOutput().getChoices().get(0).getFinishReason();
                                // 输出内容
                                System.out.print(content);
                                fullContent.append(content);
                                // 当 finishReason 不为 null 时，表示是最后一个 chunk，输出用量信息
                                if (finishReason != null && !"null".equals(finishReason)) {
                                    System.out.println("\n--- 请求用量 ---");
                                    System.out.println("输入 Tokens：" + message.getUsage().getInputTokens());
                                    System.out.println("输出 Tokens：" + message.getUsage().getOutputTokens());
                                    System.out.println("总 Tokens：" + message.getUsage().getTotalTokens());
                                }
                                System.out.flush(); // 立即刷新输出
                            },
                            // onError: 处理错误
                            error -> {
                                System.err.println("\n请求失败: " + error.getMessage());
                                latch.countDown();
                            },
                            // onComplete: 完成回调
                            () -> {
                                System.out.println(); // 换行
                                // System.out.println("完整响应: " + fullContent.toString());
                                latch.countDown();
                            }
                    );
            // 主线程等待异步任务完成
            latch.await();
            System.out.println("程序执行完成");
        } catch (Exception e) {
            System.err.println("请求异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
