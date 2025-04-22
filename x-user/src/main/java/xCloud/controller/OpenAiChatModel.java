package xCloud.controller;

import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/4/22 17:24
 * @ClassName OpenAiChatModel
 */
public class OpenAiChatModel {

    public static void main(String[] args) {

        stream();
//        chat();
    }

    /**
     * 输出结果
     */
    public static void chat() {
        // 构造 Chat 模型（这里以 gpt-4o-mini 为例）
        dev.langchain4j.model.openai.OpenAiChatModel model = dev.langchain4j.model.openai.OpenAiChatModel.builder()
                .apiKey("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")
                .modelName("gpt-4o-mini")
                .build();

        // 最简单的 String 输入输出
        String reply = model.chat("I understand that you might be feeling frustrated. I'm here to help, so if you'd like to talk about something or need assistance, feel free to share.,翻译为中文");
        System.out.println(reply);  // 输出：Hello World
    }

    /**
     * 输出流式结果
     */
    public static void stream() {
        OpenAiStreamingChatModel model = OpenAiStreamingChatModel.builder().modelName("gpt-4o-mini")
                .apiKey("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")
                .build();

        // 最简单的 String 输入输出
        model.chat("I understand that you might, feel free to share.,翻译为中文", new StreamingChatResponseHandler() {
            @Override
            public void onPartialResponse(String s) {

                try {
                    Thread.sleep(190);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.print(s);
            }

            @Override
            public void onCompleteResponse(ChatResponse chatResponse) {

                try {
                    Thread.sleep(190);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
                System.out.println("onError");

            }
        });
    }
}
