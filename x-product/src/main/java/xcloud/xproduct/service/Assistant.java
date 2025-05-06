package xcloud.xproduct.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.spring.AiService;

/**
 * Think of it as a standard Spring Boot @Service, but with AI capabilities.
 *
 * When the application starts, LangChain4j starter will scan the classpath and find all interfaces annotated with @AiService. For each AI Service found, it will create an implementation of this interface using all LangChain4j components available in
 * the application context and will register it as a bean, so you can auto-wire it where needed:
 */
@AiService
public interface Assistant {

    @SystemMessage("You are a helpful assistant.")
    String chat(String message);
}
