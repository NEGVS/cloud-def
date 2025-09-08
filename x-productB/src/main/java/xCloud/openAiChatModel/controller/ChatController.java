package xCloud.openAiChatModel.controller;

import dev.langchain4j.model.chat.ChatModel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xCloud.openAiChatModel.orchestrator.RagClient;
import xCloud.openAiChatModel.orchestrator.entity.UserPrincipal;
import xCloud.openAiChatModel.orchestrator.request.ChatReq;
import xCloud.openAiChatModel.orchestrator.request.RagRequest;
import xCloud.openAiChatModel.orchestrator.response.RagResponse;
import xCloud.openAiChatModel.orchestrator.tool.ToolCall;
import xCloud.openAiChatModel.orchestrator.tool.ToolExecutor;
import xCloud.openAiChatModel.orchestrator.tool.ToolFormatter;
import xCloud.openAiChatModel.orchestrator.tool.ToolParser;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @Description In this case, an instance of OpenAiChatModel (an implementation of a ChatModel) will be automatically created, and you can autowire it where needed:
 * @Author Andy Fan
 * @Date 2025/9/4 19:36
 * @ClassName ChatController
 * If you need an instance of a StreamingChatModel, use the streaming-chat-model instead of the chat-model properties:
 * <p>
 * langchain4j.open-ai.streaming-chat-model.api-key=${OPENAI_API_KEY}
 */
@Tag(name = "chatController Management", description = "APIs for managing chatControllers")
@RequestMapping("/chatController")
@RestController
//@RequiredArgsConstructor
public class ChatController {

    @Resource
    RagClient ragClient;
    //    private final JobService jobService; // MyBatis-Plus
    @Resource
    ToolExecutor toolExecutor;


    ChatModel chatModel;


    @Operation(
            summary = "Search chatControllers with pagination",
            description = "Retrieve a paginated list of chatControllers based on search criteria"
    )
    @GetMapping("/chat")
    public String model(@RequestParam(value = "message", defaultValue = "Hello") String message) {
        return chatModel.chat(message);
    }

    /**
     * 从用户登录上下文与请求中构造用户画像。
     * 典型字段：年龄、学历、技能、过往投递、简历亮点。
     */
    private Map<String, Object> buildProfile(UserPrincipal up, ChatReq req) {
        Map<String, Object> p = new HashMap<>();
        p.put("user_id", up.getUserId());
        p.put("username", up.getUsername());
        p.put("skills", up.getSkills());
        p.put("preferred_city", req.getCity());
        return p;
    }

    /**
     * 从请求参数构建过滤条件（传递给 Chroma 向量库的 metadata filter）。
     */
    private Map<String, Object> buildFilters(ChatReq req) {
        Map<String, Object> f = new HashMap<>();
        if (req.getCity() != null) f.put("city", req.getCity());
        if (req.getSalaryMin() != null) f.put("salary_min", req.getSalaryMin());
        if (req.getSkills() != null) f.put("skills", req.getSkills());
        return f;
    }

    /**
     * @param req req
     * @return
     * @AuthenticationPrincipal UserPrincipal up ,@AuthenticationPrincipal
     */
    @PostMapping
    public Object chat(@RequestBody ChatReq req, UserPrincipal up) {
        Map<String, Object> profile = buildProfile(up, req); // 从用户/简历服务聚合
        Map<String, Object> filters = buildFilters(req);     // city/salary/skills -> Chroma metadata

        RagRequest r = new RagRequest();
        r.setQuery(req.getMessage());
        r.setUser_profile(profile);
        r.setFilters(filters);

        RagResponse rr = ragClient.chat(r);

        // 解析 LLM 答案中可能的“工具调用 JSON”
        Optional<ToolCall> tool = ToolParser.tryParse(rr.getAnswer());
        if (tool.isPresent()) {
//            todo
//            Object toolResult = toolExecutor.execute(tool.get()); // 调 Job-Service / 投递 / 预约
            Object toolResult = toolExecutor.execute(tool.get()); // 调 Job-Service / 投递 / 预约
            // 将工具结果再发 RAG-Service（或直接在 Java 层重组回复）
            return Map.of(
                    "message", ToolFormatter.composeFollowup(rr.getAnswer(), null),
                    "chunks", rr.getChunks()
            );
        }
        return rr;
    }
}
