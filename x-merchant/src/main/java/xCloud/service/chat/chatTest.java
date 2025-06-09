package xCloud.service.chat;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

import java.util.Scanner;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/6/5 16:25
 * @ClassName chatTest
 */
public class chatTest {

    private ChatLanguageModel chatLanguageModel;


    Assistant assistant;

    public static void main(String[] args) {
        // 初始化 OpenAI 聊天模型并指定 demo 端点
        ChatLanguageModel model = OpenAiChatModel.builder()
                .baseUrl("http://langchain4j.dev/demo/openai/v1") // 显式设置 demo 端点
                .apiKey("demo") // 使用 demo 密钥
                .modelName("gpt-4o-mini") // 指定模型
                .build();

        int count = 10;
        while (count >= 0) {
            count--;
            try {
                // 发送简单文本请求
                String prompt = "";
                System.out.println("请输入问题: " + prompt);
                Scanner scanner = new Scanner(System.in);
                prompt = scanner.nextLine();
                System.out.println("AI 正在思考... ");
                Thread.sleep(1000);
                String response = model.chat(prompt);
                System.out.println("AI 回复: \n" + response);
                System.out.println("------------------------");
                Thread.sleep(1000);

            } catch (InterruptedException e) {
                System.out.println("线程中断");
            }
        }
    }
}
