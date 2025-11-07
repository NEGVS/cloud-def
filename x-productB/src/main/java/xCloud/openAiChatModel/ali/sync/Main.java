package xCloud.openAiChatModel.ali.sync;

/**
 * @Description 文本对话，同步回答，设定角色
 * @Author Andy Fan
 * @Date 2025/11/6 10:20
 * @ClassName Main
 */
// 建议 OpenAI Java SDK版本 >= 3.5.0

import cn.hutool.json.JSONUtil;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;

public class Main {
    public static void main(String[] args) {
        try {
            OpenAIClient client = OpenAIOkHttpClient.builder()
                    // 新加坡和北京地域的API Key不同。获取API Key：https://help.aliyun.com/zh/model-studio/get-api-key
                    // 若没有配置环境变量，请用阿里云百炼API Key将下行替换为.apiKey("sk-xxx")
                    .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                    // 以下是北京地域base_url，如果使用新加坡地域的模型，需要将base_url替换为：https://dashscope-intl.aliyuncs.com/compatible-mode/v1
                    .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                    .build();

            // 创建 ChatCompletion 参数
            ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                    .model("qwen-plus")  // 指定模型
                    .addSystemMessage("You are a helpful assistant.")
                    .addUserMessage("你是谁？马云是谁？")
                    .build();

            // 发送请求并获取响应
            System.out.println(JSONUtil.toJsonStr(params));
            System.out.println("正在请求模型，请稍等...");
            ChatCompletion chatCompletion = client.chat().completions().create(params);
            String content = chatCompletion.choices().get(0).message().content().orElse("未返回有效内容");
            System.out.println(content);
            // 如需查看完整响应，请取消下列注释
            // System.out.println(chatCompletion);

        } catch (Exception e) {
            System.err.println("错误信息：" + e.getMessage());
            System.out.println("请参考文档：https://help.aliyun.com/zh/model-studio/developer-reference/error-code");
        }
    }
}