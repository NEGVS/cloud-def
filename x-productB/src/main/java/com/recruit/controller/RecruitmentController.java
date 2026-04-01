package com.recruit.controller;

import com.recruit.service.RecruitmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class RecruitmentController {

    @Autowired
    private RecruitmentService recruitmentService;

    @PostMapping("/chat")
    public String chat(@RequestBody String userInput) {
        return recruitmentService.handleUserInput(userInput);
    }
}
