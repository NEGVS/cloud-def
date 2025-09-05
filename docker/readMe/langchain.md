Spring Boot starter for declarative AI Services
LangChain4j provides a Spring Boot starter for auto-configuring AI Services, RAG, Tools etc.

Assuming you have already imported one of the integrations starters (see above), import langchain4j-spring-boot-starter:
声明式AI服务的Spring Boot启动器
LangChain4j提供了一个Spring Boot启动器，用于自动配置AI服务、RAG、工具等。

假设您已经导入了一个集成启动器（见上文），请导入langchain4j spring boot starter：

Explicit Component Wiring
If you have multiple AI Services and want to wire different LangChain4j components into each of them, you can specify which components to use with explicit wiring mode (@AiService(wiringMode = EXPLICIT)).
Let's say we have two ChatModels configured:
# OpenAI
langchain4j.open-ai.chat-model.api-key=${OPENAI_API_KEY}
langchain4j.open-ai.chat-model.model-name=gpt-4o-mini

# Ollama
langchain4j.ollama.chat-model.base-url=http://localhost:11434
langchain4j.ollama.chat-model.model-name=llama3.1
显式组件接线
如果你有多个AI服务，并且想将不同的LangChain4j组件连接到每个服务中，你可以通过显式连接模式（@AiService（wiringMode=explicit））指定要使用的组件。

假设我们配置了两个ChatModel：

```java
@AiService(wiringMode = EXPLICIT, chatModel = "openAiChatModel")
interface OpenAiAssistant {

    @SystemMessage("You are a polite assistant")
    String chat(String userMessage);
}

@AiService(wiringMode = EXPLICIT, chatModel = "ollamaChatModel")
interface OllamaAssistant {

    @SystemMessage("You are a polite assistant")
    String chat(String userMessage);
}
```
# Listening for AI Service Registration Events
After you have completed the development of the AI Service in a declarative manner, you can listen for the AiServiceRegisteredEvent by implementing the ApplicationListener<AiServiceRegisteredEvent> interface. This event is triggered when AI Service is registered in the Spring context, allowing you to obtain information about all registered AI services and their tools at runtime. Here is an example:
监听AI服务注册事件
在以声明方式完成AI服务的开发后，您可以通过实现ApplicationListener<AiServiceRegisteredEvent>接口来监听AiServiceRegistereEvent。当AI服务在Spring上下
文中注册时，会触发此事件，允许您在运行时获取有关所有注册的AI服务及其工具的信息。以下是一个示例：
```java
@Component
class AiServiceRegisteredEventListener implements ApplicationListener<AiServiceRegisteredEvent> {


    @Override
    public void onApplicationEvent(AiServiceRegisteredEvent event) {
        Class<?> aiServiceClass = event.aiServiceClass();
        List<ToolSpecification> toolSpecifications = event.toolSpecifications();
        for (int i = 0; i < toolSpecifications.size(); i++) {
            System.out.printf("[%s]: [Tool-%s]: %s%n", aiServiceClass.getSimpleName(), i + 1, toolSpecifications.get(i));
        }
    }
}
```
# Flux
When streaming, you can use Flux<String> as a return type of AI Service:流式传输时，您可以使用Flux<String>作为AI服务的返回类型：
```java
@AiService
interface Assistant {

    @SystemMessage("You are a polite assistant")
    Flux<String> chat(String userMessage);
}
```

# Observability
To enable observability for a ChatModel or StreamingChatModel bean, you need to declare one or more ChatModelListener beans:
可观测性
要为ChatModel或StreamingChatModel bean启用可观察性，您需要声明一个或多个ChatModelListener bean：
```java
@Configuration
class MyConfiguration {
    
    @Bean
    ChatModelListener chatModelListener() {
        return new ChatModelListener() {

            private static final Logger log = LoggerFactory.getLogger(ChatModelListener.class);

            @Override
            public void onRequest(ChatModelRequestContext requestContext) {
                log.info("onRequest(): {}", requestContext.chatRequest());
            }

            @Override
            public void onResponse(ChatModelResponseContext responseContext) {
                log.info("onResponse(): {}", responseContext.chatResponse());
            }

            @Override
            public void onError(ChatModelErrorContext errorContext) {
                log.info("onError(): {}", errorContext.error().getMessage());
            }
        };
    }
}
```

Every ChatModelListener bean in the application context will be automatically injected into all ChatModel and StreamingChatModel beans created by one of our Spring Boot starters.
应用程序上下文中的每个ChatModelListener bean都将自动注入到我们的Spring Boot启动器之一创建的所有ChatModel和StreamingChatModel bean中。
Testing
An example of integration testing for a Customer Support Agent
https://github.com/langchain4j/langchain4j-examples/blob/main/customer-support-agent-example/src/test/java/dev/langchain4j/example/CustomerSupportAgentIT.java
Supported versions
LangChain4j Spring Boot integration requires Java 17 and Spring Boot 3.2.
Examples
Low-level Spring Boot example using ChatModel API
High-level Spring Boot example using AI Services
Example of customer support agent using Spring Boot