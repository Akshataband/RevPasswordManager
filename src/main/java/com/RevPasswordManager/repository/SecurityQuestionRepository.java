package com.RevPasswordManager.repository;

import com.RevPasswordManager.entities.SecurityQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SecurityQuestionRepository extends JpaRepository<SecurityQuestion, Long> {

    List<SecurityQuestion> findByUserId(Long userId);


    Optional<SecurityQuestion> findByUserIdAndQuestion(
            Long userId,
            String question);
}
