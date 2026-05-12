package xCloud.service.recruitment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import xCloud.service.recruitment.tool.AgentTool;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 智能招聘 Agent（ReAct + Plan-and-Execute）
 *
 * 架构：
 *   用户问题
 *     ↓
 *   Plan（制定步骤）
 *     ↓
 *   ReAct 循环（Thought → Action → Observation → ...）
 *     ↓
 *   Final Answer（流式输出）
 *
 * 工具：document_search / database_query / external_api
 * 特性：自动工具选择、多步推理、失败重试、对话记忆、流式输出
 */
@Slf4j
@Service
public class RecruitmentAgentService {

    private static final int MAX_STEPS   = 6;  // ReAct 最大步数
    private static final int MAX_RETRIES = 2;  // 单步最大重试次数

    private static final Pattern ACTION_PATTERN =
            Pattern.compile("Action:\\s*(.+?)\\s*\\n", Pattern.DOTALL);
    private static final Pattern ACTION_INPUT_PATTERN =
            Pattern.compile("Action Input:\\s*(.+?)(?=\\nObservation:|\\nThought:|\\nFinal Answer:|$)",
                    Pattern.DOTALL);
    private static final Pattern FINAL_ANSWER_PATTERN =
            Pattern.compile("Final Answer:\\s*(.+?)$", Pattern.DOTALL);

    @Value("${ali.baseUrl}")
    private String baseUrl;

    @Value("${ali.api-key}")
    private String apiKey;

    @Value("${ali.chat_model_name}")
    private String model;

    private final WebClient webClient;
    private final ConversationMemoryService memoryService;
    private final Map<String, AgentTool> toolMap;

    public RecruitmentAgentService(WebClient webClient,
                                   ConversationMemoryService memoryService,
                                   List<AgentTool> tools) {
        this.webClient = webClient;
        this.memoryService = memoryService;
        this.toolMap = tools.stream()
                .collect(Collectors.toMap(AgentTool::getName, t -> t));
    }

    // ─────────────────────────────────────────────
    // 主入口：流式对话
    // ─────────────────────────────────────────────

    /**
     * Agent 对话（流式输出）
     *
     * @param sessionId 会话 ID（多轮记忆）
     * @param userQuery 用户问题
     * @return SSE 流
     */
    public Flux<String> chat(String sessionId, String userQuery) {
        // 用 multicast 支持多订阅者（如断连重连、框架内部多次 subscribe）
        // unicast 只允许一个订阅者，第二次 subscribe 会抛 IllegalStateException
        Sinks.Many<String> sink = Sinks.many().multicast().onBackpressureBuffer();

        // 异步执行 Agent 循环，结果推入 sink
        Thread.ofVirtual().start(() -> {
            try {
                // 先快照历史，再加入本轮消息，避免用户问题在历史和问题字段里重复出现
                String historySnapshot = memoryService.buildHistoryText(sessionId);
                memoryService.addMessage(sessionId, "user", userQuery);

                // 1. Plan 阶段
                log.info("1. Plan 阶段");
                emit(sink, "[PLAN]\n");
                List<String> plan = makePlan(userQuery, historySnapshot);
                plan.forEach(step -> emit(sink, "• " + step + "\n"));
                emit(sink, "\n[EXECUTING]\n");

                // 2. ReAct 执行阶段
                log.info("2. ReAct 执行阶段");
                String finalAnswer = executeReAct(userQuery, historySnapshot, plan, sink);

                // 3. 记忆 & 流式输出最终答案
                log.info("3. 记忆 & 流式输出最终答案");
                memoryService.addMessage(sessionId, "assistant", finalAnswer);
                emit(sink, "\n[ANSWER]\n");
                streamAnswer(finalAnswer, sink);

            } catch (Exception e) {
                log.error("Agent 执行异常: {}", e.getMessage(), e);
                // getMessage() 可能为 null（如 NullPointerException），用 toString() 兜底
                emit(sink, "\n[ERROR] " + (e.getMessage() != null ? e.getMessage() : e.toString()));
            } finally {
                sink.tryEmitComplete();
            }
        });

        return sink.asFlux();
    }

    /** 统一推送入口，记录推送失败日志，防止数据静默丢失 */
    private void emit(Sinks.Many<String> sink, String value) {
        Sinks.EmitResult result = sink.tryEmitNext(value);
        if (result.isFailure()) {
            log.warn("SSE 推送失败: {} | value='{}'", result, value);
        }
    }

    // ─────────────────────────────────────────────
    // Plan 阶段
    // ─────────────────────────────────────────────

    private List<String> makePlan(String userQuery, String historyText) {
        String toolDesc = buildToolDescriptions();

        String prompt = "你是一个智能招聘助手。请为以下用户问题制定一个简洁的执行计划（2-4个步骤）。\n\n" +
                "可用工具：\n" + toolDesc + "\n\n" +
                "历史对话：\n" + historyText + "\n\n" +
                "用户问题：" + userQuery + "\n\n" +
                "请输出步骤列表，每行一个步骤，格式：1. 步骤描述";

        String response = callLLM(prompt, 300);
        return parsePlanSteps(response);
    }

    private List<String> parsePlanSteps(String response) {
        List<String> steps = new ArrayList<>();
        for (String line : response.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.matches("^\\d+\\..*")) {
                steps.add(trimmed.replaceFirst("^\\d+\\.\\s*", ""));
            }
        }
        return steps.isEmpty() ? List.of("直接回答用户问题") : steps;
    }

    // ─────────────────────────────────────────────
    // ReAct 执行阶段
    // ─────────────────────────────────────────────

    private String executeReAct(String userQuery, String historyText,
                                 List<String> plan, Sinks.Many<String> sink) {
        String toolDesc = buildToolDescriptions();
        String planText = java.util.stream.IntStream.range(0, plan.size())
                .mapToObj(i -> (i + 1) + ". " + plan.get(i))
                .collect(Collectors.joining("\n"));

        // scratchpad 只保留最近 MAX_SCRATCHPAD_STEPS 步，防止 prompt 无限膨胀
        Deque<String> scratchpadWindow = new ArrayDeque<>();
        AtomicInteger stepCount = new AtomicInteger(0);

        while (stepCount.get() < MAX_STEPS) {
            String scratchpad = String.join("", scratchpadWindow);
            String reactPrompt = buildReActPrompt(userQuery, historyText, toolDesc, planText, scratchpad);
            String llmOutput = callLLMWithRetry(reactPrompt, 800);

            log.debug("ReAct Step {} LLM output:\n{}", stepCount.get(), llmOutput);
            emit(sink, "\n[STEP " + (stepCount.incrementAndGet()) + "]\n" + llmOutput + "\n");

            // 检查是否有 Final Answer
            Matcher finalMatcher = FINAL_ANSWER_PATTERN.matcher(llmOutput);
            if (finalMatcher.find()) {
                return finalMatcher.group(1).trim();
            }

            // 解析 Action
            Matcher actionMatcher = ACTION_PATTERN.matcher(llmOutput);
            Matcher inputMatcher  = ACTION_INPUT_PATTERN.matcher(llmOutput);

            if (!actionMatcher.find() || !inputMatcher.find()) {
                // LLM 没有按格式输出，追加提示继续
                addToWindow(scratchpadWindow, llmOutput + "\nObservation: 请按格式输出 Action 或 Final Answer。\n");
                continue;
            }

            String toolName  = actionMatcher.group(1).trim();
            String toolInput = inputMatcher.group(1).trim();

            // 执行工具
            String observation = executeTool(toolName, toolInput);
            log.info("Tool [{}] input={}, observation length={}", toolName, toolInput, observation.length());

            addToWindow(scratchpadWindow, llmOutput + "\nObservation: " + observation + "\n");
        }

        // 超出最大步数，强制总结
        return forceSummarize(userQuery, String.join("", scratchpadWindow));
    }

    /** 滑动窗口：最多保留最近 MAX_SCRATCHPAD_STEPS 步，防止 prompt 超长 */
    private static final int MAX_SCRATCHPAD_STEPS = 4;

    private void addToWindow(Deque<String> window, String entry) {
        window.addLast(entry);
        while (window.size() > MAX_SCRATCHPAD_STEPS) {
            window.removeFirst();
        }
    }

    private String executeTool(String toolName, String toolInput) {
        AgentTool tool = toolMap.get(toolName);
        if (tool == null) {
            return "工具 [" + toolName + "] 不存在，可用工具: " + String.join(", ", toolMap.keySet());
        }

        // 失败重试
        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            try {
                return tool.execute(toolInput);
            } catch (Exception e) {
                log.warn("工具 [{}] 第 {} 次执行失败: {}", toolName, attempt + 1, e.getMessage());
                if (attempt == MAX_RETRIES) {
                    return "工具执行失败（已重试 " + MAX_RETRIES + " 次）: " + e.getMessage();
                }
            }
        }
        return "工具执行失败";
    }

    private String forceSummarize(String userQuery, String scratchpad) {
        String prompt = "根据以下推理过程，请直接回答用户问题。\n\n" +
                "用户问题：" + userQuery + "\n\n" +
                "推理过程：\n" + scratchpad + "\n\n" +
                "请给出最终答案：";
        return callLLM(prompt, 600);
    }

    // ─────────────────────────────────────────────
    // Prompt 构建
    // ─────────────────────────────────────────────

    private String buildReActPrompt(String userQuery, String history,
                                     String toolDesc, String plan, String scratchpad) {
        return "你是一个智能招聘助手，使用 ReAct 框架（Thought-Action-Observation）逐步解决问题。\n\n" +
               "可用工具：\n" + toolDesc + "\n\n" +
               "历史对话：\n" + history + "\n\n" +
               "执行计划：\n" + plan + "\n\n" +
               "用户问题：" + userQuery + "\n\n" +
               "推理轨迹：\n" + scratchpad + "\n" +
               "请继续推理。输出格式：\n" +
               "Thought: 你的思考\n" +
               "Action: 工具名称（必须是可用工具之一）\n" +
               "Action Input: 工具输入\n" +
               "或者当你有足够信息时：\n" +
               "Final Answer: 最终答案\n";
    }

    private String buildToolDescriptions() {
        return toolMap.values().stream()
                .map(t -> "- " + t.getName() + ": " + t.getDescription())
                .collect(Collectors.joining("\n"));
    }

    // ─────────────────────────────────────────────
    // LLM 调用
    // ─────────────────────────────────────────────

    private String callLLMWithRetry(String prompt, int maxTokens) {
        RuntimeException lastEx = null;
        for (int i = 0; i <= MAX_RETRIES; i++) {
            try {
                return callLLM(prompt, maxTokens);
            } catch (RuntimeException e) {
                lastEx = e;
                log.warn("LLM 调用第 {} 次失败: {}", i + 1, e.getMessage());
            }
        }
        throw lastEx;
    }

    private String callLLM(String prompt, int maxTokens) {
        Map<?, ?> response = webClient.post()
                .uri(baseUrl + "/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(Map.of(
                        "model", model,
                        "messages", List.of(Map.of("role", "user", "content", prompt)),
                        "max_tokens", maxTokens,
                        "temperature", 0.3
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null) throw new RuntimeException("LLM 返回为空");
        List<?> choices = (List<?>) response.get("choices");
        Map<?, ?> message = (Map<?, ?>) ((Map<?, ?>) choices.get(0)).get("message");
        return (String) message.get("content");
    }

    /**
     * 流式输出最终答案。
     * 直接将 finalAnswer 按句子/标点分块推送，避免对已定稿的答案再次调用 LLM 润色
     * （二次润色会增加延迟、额外 token 消耗，且可能改变答案含义）。
     */
    private void streamAnswer(String answer, Sinks.Many<String> sink) {
        if (answer == null || answer.isBlank()) return;
        // 按中文句号/问号/感叹号/换行分块，保留分隔符
        String[] chunks = answer.split("(?<=[。？！\n])");
        for (String chunk : chunks) {
            if (!chunk.isBlank()) {
                emit(sink, chunk);
            }
        }
    }

}
