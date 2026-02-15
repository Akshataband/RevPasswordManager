package com.RevPasswordManager.service;

import com.RevPasswordManager.entities.SecurityQuestion;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.RevPasswordManager.repository.SecurityQuestionRepository;

import java.util.List;

@Service
public class SecurityQuestionService {

    private final SecurityQuestionRepository repository;
    private final PasswordEncoder passwordEncoder;

    public SecurityQuestionService(SecurityQuestionRepository repository,
                                   PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public void addQuestions(List<SecurityQuestion> questions) {

        for (SecurityQuestion q : questions) {
            q.setAnswer(passwordEncoder.encode(q.getAnswer()));
            repository.save(q);
        }
    }

    public boolean verifyAnswers(Long userId, List<String> answers) {

        List<SecurityQuestion> questions = repository.findByUserId(userId);

        for (int i = 0; i < questions.size(); i++) {
            if (!passwordEncoder.matches(answers.get(i), questions.get(i).getAnswer())) {
                return false;
            }
        }

        return true;
    }
}
