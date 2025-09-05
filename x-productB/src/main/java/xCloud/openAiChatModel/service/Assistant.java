package xCloud.openAiChatModel.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.spring.AiService;

/**
 * You can now define AI Service interface and annotate it with @AiService:
 * 现在，您可以定义AI服务接口并用@AiService对其进行注释：
 *
 * Think of it as a standard Spring Boot @Service, but with AI capabilities.
 *
 * When the application starts, LangChain4j starter will scan the classpath and find all interfaces annotated with @AiService. For each AI Service found, it will create an implementation of this interface using all LangChain4j components available in the application context and will register it as a bean, so you can auto-wire it where needed:
 * 将其视为标准的Spring Boot@Service，但具有AI功能。
 *
 * 当应用程序启动时，LangChain4j starter将扫描类路径并找到所有用@AiService注释的接口。对于找到的每个AI服务，它将使用应用程序上下文中可
 * 用的所有LangChain4j组件创建此接口的实现，并将其注册为bean，以便您可以在需要时自动连接它：
 *
 */
@AiService
public interface Assistant {

    @SystemMessage("You are a polite assistant")
    String chat(String userMessage);
}
