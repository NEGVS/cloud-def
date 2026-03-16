package xCloud.openAiChatModel.intent;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.stereotype.Component;

/**
 * @Description 意图分类器，用DeepSeek模型判断
 * @Author Andy Fan
 * @Date 2026/3/16 23:59
 * @ClassName IntentClassifier
 */
@Component
public class IntentClassifier {

    private final ChatModel chatModel;

    public IntentClassifier() {
        this.chatModel = OpenAiChatModel.builder()
                .apiKey("你的DeepSeek API Key")
                .modelName("deepseek-chat")
                .baseUrl("https://api.deepseek.com/v1")
                .build();
    }

    public Intent classify(String userQuery) {
        String prompt = String.format("""
                你是意图分类专家，判断用户问题是否和招聘相关，只返回RECRUITMENT或GENERAL。
                用户问题：%s
                    """, userQuery);
        String result = chatModel.chat(prompt);
        return result.trim().equals("RECRUITMENT") ? Intent.RECRUITMENT : Intent.GENERAL;
    }

}
