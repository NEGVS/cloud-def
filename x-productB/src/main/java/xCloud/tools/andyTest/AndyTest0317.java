package xCloud.tools.andyTest;

import xCloud.openAiChatModel.ali.stream.AliChatUtil;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2026/3/17 10:48
 * @ClassName AndyTest0317
 */
public class AndyTest0317 {
    public static void main(String[] args) {
        System.out.println("andy test 0317");

        AliChatUtil aliChatUtil = new AliChatUtil();
        String chat = aliChatUtil.chat("你好,你是谁", null);
        System.out.println(chat);
    }
}
