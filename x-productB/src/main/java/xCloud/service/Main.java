package xCloud.service;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/10/16 20:17
 * @ClassName Main
 */
public class Main {
    public static void main(String[] args) {
        // Create client from environment variables
        OpenAIClient client = OpenAIOkHttpClient.fromEnv();

        ResponseCreateParams params = ResponseCreateParams.builder()
                .input("Say this is a test")
                .model("gpt-5")
                .build();

        Response response = client.responses().create(params);
        System.out.println(response.output());
    }
}
