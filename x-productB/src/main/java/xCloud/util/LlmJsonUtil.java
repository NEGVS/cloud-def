package xCloud.util;

/**
 * LLM 返回内容 JSON 抽取工具
 * <p>
 * 背景：LLM 常把 JSON 包在 Markdown 代码块里，或在 JSON 前后追加解释性文字，
 * 例如：
 * <pre>
 * 以下是结果：
 * ```json
 * { "name": "张三" }
 * ```
 * 希望对你有帮助
 * </pre>
 * <p>
 * 原实现散落在 JDGenerationService / ResumeParseService / CandidateScoringService 三处，
 * 且用 {@code startsWith("```")} + {@code substring(3)} 硬裁，遇到短文本（如 "``"）会抛
 * {@code StringIndexOutOfBoundsException}。本类统一为边界安全的处理。
 *
 * @author Claude
 * @date 2026-10-12
 */
public final class LlmJsonUtil {

    private LlmJsonUtil() {
    }

    /**
     * 从 LLM 返回文本中抽取 JSON 字符串：
     * 1. 去除首尾空白
     * 2. 剥离 ```json / ``` 围栏（长度安全）
     * 3. 兜底截取首个 { 或 [ 到最后一个 } 或 ]
     *
     * @param raw LLM 原始返回
     * @return 可被 JSON 解析器直接解析的字符串；raw 为 null 时返回 null
     */
    public static String extractJson(String raw) {
        if (raw == null) {
            return null;
        }

        String json = raw.trim();

        // ============1-剥离 Markdown 围栏（边界安全）============
        if (json.startsWith("```json")) {
            json = json.substring(7).trim();
        } else if (json.startsWith("```")) {
            json = json.substring(3).trim();
        }
        if (json.endsWith("```")) {
            json = json.substring(0, json.length() - 3).trim();
        }

        // ============2-兜底截取首 { / [ 到末 } / ]============
        int objStart = json.indexOf('{');
        int arrStart = json.indexOf('[');
        int start = (objStart < 0) ? arrStart : (arrStart < 0 ? objStart : Math.min(objStart, arrStart));
        if (start > 0) {
            int end = Math.max(json.lastIndexOf('}'), json.lastIndexOf(']'));
            if (end > start) {
                json = json.substring(start, end + 1);
            }
        }

        return json;
    }
}
