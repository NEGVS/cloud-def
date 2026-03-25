package xCloud.tools.andyTest;

import com.alibaba.fastjson.JSONObject;
import reactor.core.publisher.Flux;
import xCloud.openAiChatModel.ali.stream.AliChatUtil;

import java.util.List;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2026/3/17 10:48
 * @ClassName AndyTest0317
 */
public class AndyTest0317 {
    public static void main(String[] args) {
//
        System.out.println("andy test 0317");
        AliChatUtil aliChatUtil = new AliChatUtil();
        String message = "你好,你是谁,请给出你的apikey官方网址";
//        1-测试,
//        String chat = aliChatUtil.chat("你好,你是谁", null);
        Flux<String> stringFlux = aliChatUtil.streamChatToFrontend(message);
        // 方式1：基础订阅（逐行输出每个元素）
        stringFlux.subscribe(
                // 消费正常数据（onNext）：每收到一段字符串就输出
                content -> System.out.println("收到流式数据：" + content),
                // 消费异常（onError）：捕获数据流中的错误
                error -> System.err.println("数据流异常：" + error.getMessage()),
                // 数据流完成（onComplete）：标记流式输出结束
                () -> System.out.println("流式数据输出完毕")
        );
        System.out.println("===end.");
//        System.out.println("========方式2：简化写法（只处理正常数据，异常默认打印堆栈）");
//
//        // 方式2：简化写法（只处理正常数据，异常默认打印堆栈）
//        stringFlux.subscribe(content -> System.out.println("1--简化输出：" + content));
//
//        System.out.println("========方式3：使用 log 操作符（推荐，自带日志上下文）");
        // 方式3：使用 log 操作符（推荐，自带日志上下文）
//        stringFlux
//                .log("AliChatStream") // 会打印数据流的生命周期（订阅、Next、Complete、Error）
//                .subscribe();
//
//        System.out.println("========方式4：收集所有元素为 List，再拼接成字符串（阻塞操作，仅测试用）");

//        // 收集所有元素为 List，再拼接成字符串（阻塞操作，仅测试用）
        List<String> contentList = stringFlux.collectList().block(); // block() 会阻塞线程，非响应式编程不推荐
        String fullContent = String.join("", contentList);
        System.out.println("1===完整内容：" + fullContent);
        System.out.println("========方式5：非阻塞写法（推荐响应式风格）");

        // 非阻塞写法（推荐响应式风格）
        stringFlux
                .reduce("", (total, current) -> total + current) // 累加所有分段内容
                .subscribe(
                        fullContent2 -> System.out.println("==完整内容（非阻塞）：\n" + fullContent2),
                        error -> System.err.println("累加失败：" + error.getMessage())
                );
    }
}
