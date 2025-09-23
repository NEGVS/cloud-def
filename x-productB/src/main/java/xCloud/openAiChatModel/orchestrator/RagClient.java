package xCloud.openAiChatModel.orchestrator;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import xCloud.openAiChatModel.orchestrator.request.RagRequest;
import xCloud.openAiChatModel.orchestrator.response.RagResponse;

//@Component // 关键注解，让Spring自动扫描并创建Bean
@FeignClient(name = "rag-service", url = "${rag.url}")
public interface RagClient {

    @PostMapping("/chat")
    RagResponse chat(@RequestBody RagRequest req);
}