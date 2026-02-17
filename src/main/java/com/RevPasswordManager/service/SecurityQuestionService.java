package com.RevPasswordManager.service;

import com.RevPasswordManager.dto.QuestionAnswer;
import com.RevPasswordManager.entities.SecurityQuestion;
import com.RevPasswordManager.entities.User;
import com.RevPasswordManager.repository.SecurityQuestionRepository;
import com.RevPasswordManager.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SecurityQuestionService {

    private final SecurityQuestionRepository securityQuestionRepository;
    private final UserRepository userRepository;

    public SecurityQuestionService(SecurityQuestionRepository securityQuestionRepository,
                                   UserRepository userRepository) {
        this.securityQuestionRepository = securityQuestionRepository;
        this.userRepository = userRepository;
    }

    public void addQuestions(List<QuestionAnswer> questions) {

        User user = getCurrentUser();

        for (QuestionAnswer dto : questions) {

            SecurityQuestion question = new SecurityQuestion();
            question.setQuestion(dto.getQuestion());
            question.setAnswer(dto.getAnswer());
            question.setUser(user);

            securityQuestionRepository.save(question);
        }
    }

    private User getCurrentUser() {

        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
