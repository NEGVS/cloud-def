package xCloud.tools.fileUtil;

/**
 * @Description 文件工具类
 * @Author Andy Fan
 * @Date 2026/3/17 10:19
 * @ClassName FileUtil
 */
public class FileUtil {
    public static void main(String[] args) {
        System.out.println("---1");
        System.out.println(System.getenv("OPENAI_API_KEY"));
        System.out.println(System.getenv("OPENAI_API_KEY_A"));
        System.out.println("---2");
    }

    /**
     * 读取系统环境变量中的 OPENAI_API_KEY
     */
    private static String getApiKeyFromEnvironment() {
        // System.getenv() 获取环境变量，key 区分大小写（不同系统规则不同，建议统一大写）
        return System.getenv("OPENAI_API_KEY");
    }

    /**
     * 读取 JVM 系统属性中的 OPENAI_API_KEY
     * JVM 属性可通过启动参数 -DOPENAI_API_KEY=xxx 传入
     */
    private static String getApiKeyFromSystemProperty() {
        // System.getProperty() 获取 JVM 属性，key 区分大小写
        return System.getProperty("OPENAI_API_KEY");
    }

    /**
     * 封装：优先读取环境变量，环境变量不存在则读 JVM 属性
     */
    private static String getOpenAiApiKey() {
        String apiKey = getApiKeyFromEnvironment();
        if (apiKey == null || apiKey.trim().isEmpty()) {
            apiKey = getApiKeyFromSystemProperty();
        }
        return apiKey != null ? apiKey.trim() : null;
    }
}
