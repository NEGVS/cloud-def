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
        Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();

        // 异步执行 Agent 循环，结果推入 sink
        Thread.ofVirtual().start(() -> {
            try {
                memoryService.addMessage(sessionId, "user", userQuery);

                // 1. Plan 阶段
                sink.tryEmitNext("[PLAN]\n");
                List<String> plan = makePlan(userQuery, sessionId);
                plan.forEach(step -> sink.tryEmitNext("• " + step + "\n"));
                sink.tryEmitNext("\n[EXECUTING]\n");

                // 2. ReAct 执行阶段
                String finalAnswer = executeReAct(userQuery, sessionId, plan, sink);

                // 3. 记忆 & 流式输出最终答案
                memoryService.addMessage(sessionId, "assistant", finalAnswer);
                sink.tryEmitNext("\n[ANSWER]\n");
                streamAnswer(finalAnswer, sink);

            } catch (Exception e) {
                log.error("Agent 执行异常: {}", e.getMessage(), e);
                sink.tryEmitNext("\n[ERROR] " + e.getMessage());
            } finally {
                sink.tryEmitComplete();
            }
        });

        return sink.asFlux();
    }

    // ─────────────────────────────────────────────
    // Plan 阶段
    // ─────────────────────────────────────────────

    private List<String> makePlan(String userQuery, String sessionId) {
        String historyText = memoryService.buildHistoryText(sessionId);
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

    private String executeReAct(String userQuery, String sessionId,
                                 List<String> plan, Sinks.Many<String> sink) {
        String historyText = memoryService.buildHistoryText(sessionId);
        String toolDesc = buildToolDescriptions();
        String planText = java.util.stream.IntStream.range(0, plan.size())
                .mapToObj(i -> (i + 1) + ". " + plan.get(i))
                .collect(Collectors.joining("\n"));

        StringBuilder scratchpad = new StringBuilder(); // ReAct 推理轨迹
        AtomicInteger stepCount = new AtomicInteger(0);

        while (stepCount.get() < MAX_STEPS) {
            String reactPrompt = buildReActPrompt(userQuery, historyText, toolDesc, planText, scratchpad.toString());
            String llmOutput = callLLMWithRetry(reactPrompt, 800);

            log.debug("ReAct Step {} LLM output:\n{}", stepCount.get(), llmOutput);
            sink.tryEmitNext("\n[STEP " + (stepCount.incrementAndGet()) + "]\n" + llmOutput + "\n");

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
                scratchpad.append(llmOutput).append("\nObservation: 请按格式输出 Action 或 Final Answer。\n");
                continue;
            }

            String toolName  = actionMatcher.group(1).trim();
            String toolInput = inputMatcher.group(1).trim();

            // 执行工具
            String observation = executeTool(toolName, toolInput);
            log.info("Tool [{}] input={}, observation length={}", toolName, toolInput, observation.length());

            scratchpad.append(llmOutput)
                      .append("\nObservation: ").append(observation).append("\n");
        }

        // 超出最大步数，强制总结
        return forceSummarize(userQuery, scratchpad.toString());
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
        for (int i = 0; i <= MAX_RETRIES; i++) {
            try {
                return callLLM(prompt, maxTokens);
            } catch (Exception e) {
                log.warn("LLM 调用第 {} 次失败: {}", i + 1, e.getMessage());
                if (i == MAX_RETRIES) throw e;
            }
        }
        return "";
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
     * 流式输出最终答案（逐字推送，模拟打字效果）
     * 实际生产中应直接调用 LLM 的 stream=true 接口
     */
    private void streamAnswer(String answer, Sinks.Many<String> sink) {
        webClient.post()
                .uri(baseUrl + "/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(Map.of(
                        "model", model,
                        "messages", List.of(Map.of("role", "user", "content",
                                "请将以下内容以友好、专业的方式重新表述并输出：\n" + answer)),
                        "stream", true
                ))
                .retrieve()
                .bodyToFlux(String.class)
                .filter(chunk -> chunk.startsWith("data:") && !chunk.contains("[DONE]"))
                .map(chunk -> {
                    try {
                        String json = chunk.substring(5).trim();
                        // 简单解析 delta.content
                        int idx = json.indexOf("\"content\":\"");
                        if (idx < 0) return "";
                        int start = idx + 11;
                        int end = json.indexOf("\"", start);
                        return end > start ? json.substring(start, end) : "";
                    } catch (Exception e) {
                        return "";
                    }
                })
                .filter(s -> !s.isEmpty())
                .doOnNext(sink::tryEmitNext)
                .blockLast();
    }

}
