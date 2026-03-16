package xCloud.openAiChatModel.service;

import dev.langchain4j.model.chat.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xCloud.openAiChatModel.intent.Intent;
import xCloud.openAiChatModel.intent.IntentClassifier;

/**
 * @Description 招聘服务
 * @Author Andy Fan
 * @Date 2026/3/17 00:07
 * @ClassName RecruitmentService
 */
@Service
public class RecruitmentService {
    @Autowired
    private IntentClassifier intentClassifier;

    @Autowired
    private ChatModel chatModel;

    /**
     * 招聘服务,处理用户问题
     */
    public String handleUserQuery(String userQuery) {
        Intent intent = intentClassifier.classify(userQuery);
        if (intent == Intent.RECRUITMENT) {
//            return processRecruitmentQuery(query); // 走向量检索+生成流程
            return "走向量检索+生成流程";
        } else {
            // 直接用大模型回答
            return chatModel.chat(userQuery);
        }
    }
}
