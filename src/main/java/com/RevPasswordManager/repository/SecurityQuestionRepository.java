package com.RevPasswordManager.repository;

import com.RevPasswordManager.entities.SecurityQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SecurityQuestionRepository extends JpaRepository<SecurityQuestion, Long> {

    List<SecurityQuestion> findByUserId(Long userId);
}
