package com.RevPasswordManager.repository;

import com.RevPasswordManager.entities.User;
import com.RevPasswordManager.entities.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationCodeRepository
        extends JpaRepository<VerificationCode, Long> {

    VerificationCode findTopByUserOrderByExpiryTimeDesc(User user);
}
