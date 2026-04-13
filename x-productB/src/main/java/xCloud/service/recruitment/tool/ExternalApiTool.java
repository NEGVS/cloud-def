package xCloud.service.recruitment.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

/**
 * 外部 API 工具
 *
 * 调用业务系统接口获取实时数据，如候选人状态、面试安排、HR 系统数据等。
 * 使用场景：查询候选人面试进度、获取岗位实时招聘状态。
 */
@Slf4j
@Component
public class ExternalApiTool implements AgentTool {

    @Value("${recruitment.api.base-url:http://localhost:8080}")
    private String apiBaseUrl;

    private final WebClient webClient;

    public ExternalApiTool(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public String getName() {
        return "external_api";
    }

    @Override
    public String getDescription() {
        return "调用外部业务接口获取实时数据。当用户询问候选人面试状态、" +
               "岗位当前招聘人数、offer 进度等实时业务数据时使用。" +
               "Input: 查询类型和参数，如 '查询岗位Java工程师的招聘状态' 或 '候选人张三的面试进度'。";
    }

    @Override
    public String execute(String input) {
        try {
            log.info("[ExternalApiTool] 调用外部接口: {}", input);

            // 根据 input 路由到不同接口
            if (input.contains("岗位") || input.contains("招聘状态")) {
                return queryJobStatus(input);
            }
            if (input.contains("候选人") || input.contains("面试")) {
                return queryCandidateStatus(input);
            }

            return callGenericApi(input);

        } catch (Exception e) {
            log.warn("[ExternalApiTool] 接口调用失败: {}", e.getMessage());
            return "外部接口暂时不可用，请稍后重试。错误: " + e.getMessage();
        }
    }

    private String queryJobStatus(String input) {
        Map<?, ?> result = webClient.get()
                .uri(apiBaseUrl + "/api/jobs/status?query=" + encode(input))
                .retrieve()
                .bodyToMono(Map.class)
                .block();
        return result != null ? result.toString() : "未获取到岗位状态数据。";
    }

    private String queryCandidateStatus(String input) {
        Map<?, ?> result = webClient.get()
                .uri(apiBaseUrl + "/api/candidates/status?query=" + encode(input))
                .retrieve()
                .bodyToMono(Map.class)
                .block();
        return result != null ? result.toString() : "未获取到候选人状态数据。";
    }

    private String callGenericApi(String input) {
        Map<?, ?> result = webClient.post()
                .uri(apiBaseUrl + "/api/query")
                .bodyValue(Map.of("query", input))
                .retrieve()
                .bodyToMono(Map.class)
                .block();
        return result != null ? result.toString() : "接口返回为空。";
    }

    private String encode(String s) {
        try {
            return java.net.URLEncoder.encode(s, "UTF-8");
        } catch (Exception e) {
            return s;
        }
    }
}
