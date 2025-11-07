package xCloud.openAiChatModel.ali.muilt;

/**
 * url
 * https://bailian.console.aliyun.com/?tab=doc#/doc/?type=model&url=2866125
 *
 * @Description 通义千问 API 是无状态的 (Stateless)。它不会自动记录历史对话。要实现多轮对话，需要在每次请求中显式地传递完整的上下文信息。
 *
 * 工作原理
 * 实现多轮对话的核心是维护一个 messages 数组。每一轮对话都需要将用户的最新提问和模型的历史回复追加到此数组中，并将其作为下一次请求的输入。
 *
 * 以下示例为多轮对话时 messages 的状态变化：
 *
 * @Author Andy Fan
 * @Date 2025/11/6 10:24
 * @ClassName Main
 */
import java.util.ArrayList;
import java.util.List;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import java.util.Scanner;
import com.alibaba.dashscope.utils.Constants;


public class Main {
    // 若使用新加坡地域的模型，请释放下列注释
    // static {
    //     Constants.baseHttpApiUrl="https://dashscope-intl.aliyuncs.com/api/v1";
    // }
    public static GenerationParam createGenerationParam(List<Message> messages) {
        return GenerationParam.builder()
                // 若没有配置环境变量，请用阿里云百炼API Key将下行替换为：.apiKey("sk-xxx")
                // 新加坡和北京地域的API Key不同。获取API Key：https://help.aliyun.com/zh/model-studio/get-api-key
                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                // 模型列表：https://help.aliyun.com/zh/model-studio/getting-started/models
                .model("qwen-plus")
                .messages(messages)
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .build();
    }
    public static GenerationResult callGenerationWithMessages(GenerationParam param) throws ApiException, NoApiKeyException, InputRequiredException {
        Generation gen = new Generation();
        return gen.call(param);
    }
    public static void main(String[] args) {
        try {
            List<Message> messages = new ArrayList<>();
            messages.add(createMessage(Role.SYSTEM, "You are a helpful assistant."));
            for (int i = 0; i < 3;i++) {
                Scanner scanner = new Scanner(System.in);
                System.out.print("请输入：");
                String userInput = scanner.nextLine();
                if ("exit".equalsIgnoreCase(userInput)) {
                    break;
                }
                messages.add(createMessage(Role.USER, userInput));
                GenerationParam param = createGenerationParam(messages);
                GenerationResult result = callGenerationWithMessages(param);
                System.out.println("模型输出："+result.getOutput().getChoices().get(0).getMessage().getContent());
                messages.add(result.getOutput().getChoices().get(0).getMessage());
            }
        } catch (ApiException | NoApiKeyException | InputRequiredException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
    private static Message createMessage(Role role, String content) {
        return Message.builder().role(role.getValue()).content(content).build();
    }
}
