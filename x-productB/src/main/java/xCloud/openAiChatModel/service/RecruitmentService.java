package xCloud.openAiChatModel.service;

import dev.langchain4j.model.chat.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import xCloud.openAiChatModel.intent.Intent;
import xCloud.openAiChatModel.intent.IntentClassifier;

import java.util.List;
import java.util.Map;

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

    @Autowired
    private RecruitmentQueryService queryService;

    @Autowired
    private LLMStreamService llmStreamService;

    public String handleUserQuery(String userQuery) {
        Intent intent = intentClassifier.classify(userQuery);
        if (intent == Intent.RECRUITMENT) {
            return processRecruitmentQuery(userQuery);
        } else {
            return chatModel.chat(userQuery);
        }
    }

    public Flux<String> handleUserQueryStream(String userQuery) {
        Intent intent = intentClassifier.classify(userQuery);
        if (intent == Intent.RECRUITMENT) {
            List<Map<String, Object>> jobs = queryService.searchJobs(userQuery);
            String prompt = buildPrompt(userQuery, jobs);
            return llmStreamService.chatStream(prompt);
        } else {
            return llmStreamService.chatStream(userQuery);
        }
    }

    private String processRecruitmentQuery(String query) {
        List<Map<String, Object>> jobs = queryService.searchJobs(query);
        if (jobs.isEmpty() || !shouldReturnJobs(jobs)) {
            return chatModel.chat(query);
        }
        return chatModel.chat(buildPrompt(query, jobs));
    }

    private boolean shouldReturnJobs(List<Map<String, Object>> jobs) {
        return jobs.stream().anyMatch(job -> (float) job.getOrDefault("score", 0f) > 0.75f);
    }

    private String buildPrompt(String query, List<Map<String, Object>> jobs) {
        StringBuilder sb = new StringBuilder("用户问题: ").append(query).append("\n\n匹配职位:\n");
        for (int i = 0; i < jobs.size(); i++) {
            Map<String, Object> job = jobs.get(i);
            sb.append(i + 1).append(". ").append(job.get("title")).append("\n")
              .append(job.get("desc")).append("\n\n");
        }
        sb.append("请根据以上职位信息回复用户。");
        return sb.toString();
    }
}
