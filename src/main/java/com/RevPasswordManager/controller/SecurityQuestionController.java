package com.RevPasswordManager.controller;

import com.RevPasswordManager.dto.SecurityQuestionRequest;
import com.RevPasswordManager.dto.UpdateSecurityQuestionsRequest;
import com.RevPasswordManager.service.SecurityQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/security-questions")
@RequiredArgsConstructor
public class SecurityQuestionController {

    private final SecurityQuestionService service;

    // ================= ADD QUESTIONS =================
    @PostMapping
    public String add(@RequestBody SecurityQuestionRequest request) {

        service.addQuestions(request.getQuestions());

        return "Security questions added successfully";
    }

    // ================= GET QUESTIONS =================
    @GetMapping
    public List<String> getQuestions(Authentication authentication) {

        return service.getQuestions(authentication.getName());
    }
    @PutMapping
    public String update(
            @RequestBody UpdateSecurityQuestionsRequest request) {

        return service.updateQuestions(request);
    }
}