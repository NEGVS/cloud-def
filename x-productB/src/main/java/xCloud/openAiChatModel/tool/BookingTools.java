package xCloud.openAiChatModel.tool;

import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

/**
 * @Description Automatic Component Wiring
 * The following components will be automatically wired into the AI Service if available in the application context:
 * <p>
 * ChatModel
 * StreamingChatModel
 * ChatMemory
 * ChatMemoryProvider
 * ContentRetriever
 * RetrievalAugmentor
 * All methods of any @Component or @Service class that are annotated with @Tool An example:
 * 自动元件接线
 * 如果在应用程序上下文中可用，以下组件将自动连接到AI服务中：
 * <p>
 * 聊天模型
 * 流式聊天模型
 * 聊天记忆
 * 聊天记忆提供者
 * 内容检索器
 * 检索
 * 用@Tool注释的任何@Component或@Service类的所有方法示例：
 * @Author Andy Fan
 * @Date 2025/9/4 19:44
 * @ClassName BookingTools
 * If multiple components of the same type are present in the application context, the application will fail to start. In this case, use the explicit wiring mode (explained below).
 * 如果应用程序上下文中存在多个相同类型的组件，则应用程序将无法启动。在这种情况下，使用显式接线模式（如下所述）。
 */
@Component
public class BookingTools {
//    private final BookingService bookingService;
//
//    public BookingTools(BookingService bookingService) {
//        this.bookingService = bookingService;
//    }
//
//    @Tool
//    public Booking getBookingDetails(String bookingNumber, String customerName, String customerSurname) {
//        return bookingService.getBookingDetails(bookingNumber, customerName, customerSurname);
//    }
//
//    @Tool
//    public void cancelBooking(String bookingNumber, String customerName, String customerSurname) {
//        bookingService.cancelBooking(bookingNumber, customerName, customerSurname);
//    }
}
