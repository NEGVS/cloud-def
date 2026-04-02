package xCloud.openAiChatModel.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import xCloud.openAiChatModel.service.RecruitmentService;

@RestController
@RequestMapping("/api/recruitment")
public class RecruitmentController {

    @Autowired
    private RecruitmentService recruitmentService;

    @PostMapping("/chat")
    public String chat(@RequestBody String query) {
        return recruitmentService.handleUserQuery(query);
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestBody String query) {
        return recruitmentService.handleUserQueryStream(query);
    }
}
