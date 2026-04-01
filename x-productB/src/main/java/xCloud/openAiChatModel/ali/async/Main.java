package xCloud.openAiChatModel.ali.async;

/*
 * @Description 文本对话，异步回答 测试使用
 * @Author Andy Fan
 * @Date 2025/11/6 10:16
 * @ClassName Main
 */

import com.openai.client.OpenAIClientAsync;
import com.openai.client.okhttp.OpenAIOkHttpClientAsync;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import lombok.extern.slf4j.Slf4j;
import xCloud.entity.constant.AliConstant;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class Main {


    public static void main(String[] args) {


        // 创建 OpenAI 客户端，连接 DashScope 的兼容接口
        OpenAIClientAsync client = OpenAIOkHttpClientAsync.builder()
                // 新加坡和北京地域的API Key不同。获取API Key：https://help.aliyun.com/zh/model-studio/get-api-key
                // 若没有配置环境变量，请用阿里云百炼API Key将下行替换为.apiKey("sk-xxx")
                .apiKey(AliConstant.API_KEY)
                // 以下是北京地域base_url，如果使用新加坡地域的模型，需要将base_url替换为：https://dashscope-intl.aliyuncs.com/compatible-mode/v1
                .baseUrl(AliConstant.BASE_URL)
                .build();

        // 定义问题列表
        List<String> questions = Arrays.asList("你是谁？", "你会什么？", "天气怎么样？");

        // 创建异步任务列表
        CompletableFuture<?>[] futures = questions.stream()
                .map(question -> CompletableFuture.supplyAsync(() -> {
                    log.info("发送问题: " + question);
                    // 创建 ChatCompletion 参数
                    ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                            .model(AliConstant.CHAT_MODEL_NAME)  // 指定模型
                            .addSystemMessage("You are a helpful assistant.")
                            .addUserMessage(question)
                            .build();

                    // 发送异步请求并处理响应
                    return client.chat().completions().create(params)
                            .thenAccept(chatCompletion -> {
                                String content = chatCompletion.choices().get(0).message().content().orElse("无响应内容");
                                log.info("模型回复: " + content);
                            })
                            .exceptionally(e -> {
                                log.info("错误信息：" + e.getMessage());
                                log.info("请参考文档：https://help.aliyun.com/zh/model-studio/developer-reference/error-code");
                                return null;
                            });
                }).thenCompose(future -> future))
                .toArray(CompletableFuture[]::new);

        // 等待所有异步操作完成
        CompletableFuture.allOf(futures).join();
    }
}
