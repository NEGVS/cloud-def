package xCloud.openAiChatModel.orchestrator;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import xCloud.openAiChatModel.orchestrator.request.RagRequest;
import xCloud.openAiChatModel.orchestrator.response.RagResponse;


@FeignClient(name = "rag-service", url = "${rag.url}")
public interface RagClient {

    @PostMapping("/chat")
    RagResponse chat(@RequestBody RagRequest req);
}