package com.RevPasswordManager.controller;

import com.RevPasswordManager.dto.SecurityQuestionRequest;
import com.RevPasswordManager.service.SecurityQuestionService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/security-questions")
public class SecurityQuestionController {

    private final SecurityQuestionService service;

    public SecurityQuestionController(SecurityQuestionService service) {
        this.service = service;
    }

    @PostMapping
    public String add(@RequestBody SecurityQuestionRequest request) {

        service.addQuestions(request.getQuestions());

        return "Security questions added successfully";
    }
}
