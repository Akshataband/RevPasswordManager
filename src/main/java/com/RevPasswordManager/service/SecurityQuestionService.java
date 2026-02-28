package com.RevPasswordManager.service;

import com.RevPasswordManager.dto.QuestionAnswer;
import com.RevPasswordManager.dto.UpdateSecurityQuestionsRequest;
import com.RevPasswordManager.entities.SecurityQuestion;
import com.RevPasswordManager.entities.User;
import com.RevPasswordManager.repository.SecurityQuestionRepository;
import com.RevPasswordManager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SecurityQuestionService {

    private final SecurityQuestionRepository securityQuestionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void addQuestions(List<QuestionAnswer> questions) {

        if (questions == null || questions.size() < 3) {
            throw new RuntimeException("Minimum 3 security questions required");
        }

        User user = getCurrentUser();

        securityQuestionRepository.deleteAll(
                securityQuestionRepository.findByUserId(user.getId())
        );

        for (QuestionAnswer dto : questions) {

            SecurityQuestion question = new SecurityQuestion();
            question.setQuestion(dto.getQuestion());
            question.setHashedAnswer(passwordEncoder.encode(dto.getAnswer()));
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

    public List<String> getQuestions(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return securityQuestionRepository
                .findByUserId(user.getId())
                .stream()
                .map(SecurityQuestion::getQuestion)
                .toList();
    }
    public String updateQuestions(UpdateSecurityQuestionsRequest request) {

        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 🔐 Verify master password
        if (!passwordEncoder.matches(
                request.getMasterPassword(),
                user.getMasterPassword())) {

            throw new RuntimeException("Invalid master password");
        }

        if (request.getQuestions() == null ||
                request.getQuestions().size() < 3) {

            throw new RuntimeException("Minimum 3 security questions required");
        }

        // Delete old questions
        securityQuestionRepository.deleteAll(
                securityQuestionRepository.findByUserId(user.getId())
        );

        // Save new hashed answers
        for (QuestionAnswer dto : request.getQuestions()) {

            SecurityQuestion question = new SecurityQuestion();
            question.setQuestion(dto.getQuestion());
            question.setHashedAnswer(
                    passwordEncoder.encode(dto.getAnswer())
            );
            question.setUser(user);

            securityQuestionRepository.save(question);
        }

        return "Security questions updated successfully";
    }

    public void verifySecurityAnswers(String username, Map<String, String> answers) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("User not found"));

        List<SecurityQuestion> questions =
                securityQuestionRepository.findByUserId(user.getId());

        for (SecurityQuestion q : questions) {

            String providedAnswer = answers.get(q.getQuestion());

            if (providedAnswer == null ||
                    !passwordEncoder.matches(providedAnswer, q.getAnswer())) {

                throw new CustomException("Incorrect security answers");
            }
        }
    }
}