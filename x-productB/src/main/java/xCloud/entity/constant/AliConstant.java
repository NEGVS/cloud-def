package xCloud.entity.constant;

/*
 * @Description 阿里云AI配置常量（统一管理，不重复、不null）
 * @Author Andy Fan
 * @Date 2026/3/31 16:22
 * @ClassName AliConstant
 */

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AliConstant {

    // 从配置文件读取，赋值给静态常量
    public static String API_KEY;
    public static String CHAT_MODEL_NAME;
    public static String EMBEDDING_MODEL_NAME;
    public static String BASE_URL;

    // 【关键】set方法注入，不能直接给static变量加@Value！
    @Value("${ali.api-key}")
    public void setApiKey(String apiKey) {
        API_KEY = apiKey;
    }

    @Value("${ali.chat_model_name}")
    public void setChatModelName(String chatModelName) {
        CHAT_MODEL_NAME = chatModelName;
    }

    @Value("${ali.embedding_model_name}")
    public void setEmbeddingModelName(String embeddingModelName) {
        EMBEDDING_MODEL_NAME = embeddingModelName;
    }

    @Value("${ali.baseUrl}")
    public void setBaseUrl(String baseUrl) {
        BASE_URL = baseUrl;
    }
}
