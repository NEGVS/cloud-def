package xCloud.service.recruitment;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import xCloud.entity.constant.AliConstant;

import java.util.List;

/**
 * 招聘模块统一 LLM 同步调用助手
 * <p>
 * 背景：JDGenerationService / ResumeParseService / CandidateScoringService 三处
 * 各自实现了几乎相同的 {@code private String callLLM(String prompt)}，
 * 且每次都 {@code new Generation()}，造成重复代码 + 每次调用重建客户端。
 * <p>
 * 本类收敛为唯一入口：
 * - {@link Generation} 无共享可变状态，{@code call(param)} 每次请求独立构造，线程安全，故用单例复用；
 * - apiKey / modelName 统一取自 {@link AliConstant}，各服务不再各写一份 @Value 字段。
 *
 * @author Claude
 * @date 2026-10-12
 */
@Slf4j
@Component
public class RecruitmentLlmHelper {

    /**
     * 单例复用：Generation 本身不持有对话状态，可并发调用
     */
    private final Generation generation = new Generation();

    /**
     * 同步调用通义千问，返回首个 choice 的文本内容
     *
     * @param prompt      用户提示词
     * @param temperature 温度（简历解析建议 0.1，JD 生成建议 0.7，评分建议 0.3）
     * @param maxTokens   最大输出 token 数
     * @return LLM 返回的文本；调用失败抛出 RuntimeException，由 GlobalExceptionHandler 统一兜底
     */
    public String syncChat(String prompt, float temperature, int maxTokens) {
        try {
            GenerationParam param = GenerationParam.builder()
                    .apiKey(AliConstant.API_KEY)
                    .model(AliConstant.CHAT_MODEL_NAME)
                    .messages(List.of(Message.builder().role(Role.USER.getValue()).content(prompt).build()))
                    .temperature(temperature)
                    .maxTokens(maxTokens)
                    .build();

            GenerationResult result = generation.call(param);
            return result.getOutput().getChoices().get(0).getMessage().getContent();

        } catch (Exception e) {
            log.error("LLM 调用失败: temperature={}, maxTokens={}, err={}", temperature, maxTokens, e.getMessage(), e);
            throw new RuntimeException("LLM 调用失败：" + e.getMessage(), e);
        }
    }
}
