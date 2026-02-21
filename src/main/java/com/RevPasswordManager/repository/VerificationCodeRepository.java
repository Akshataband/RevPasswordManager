package com.RevPasswordManager.repository;

import com.RevPasswordManager.entities.User;
import com.RevPasswordManager.entities.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationCodeRepository
        extends JpaRepository<VerificationCode, Long> {

    VerificationCode findTopByUserOrderByExpiryTimeDesc(User user);
    Optional<VerificationCode> findByUserAndCode(User user, String code);
}
