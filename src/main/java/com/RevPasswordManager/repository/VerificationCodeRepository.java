package com.RevPasswordManager.repository;

import com.RevPasswordManager.entities.User;
import com.RevPasswordManager.entities.VerificationCode;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface VerificationCodeRepository
        extends JpaRepository<VerificationCode, Long> {

    VerificationCode findTopByUserOrderByExpiryTimeDesc(User user);
    Optional<VerificationCode> findByUserAndCode(User user, String code);
    Optional<VerificationCode>
    findTopByUserIdOrderByExpiryTimeDesc(Long userId);


    @Transactional
    @Modifying
    @Query("UPDATE VerificationCode v SET v.used = true WHERE v.user.id = :userId AND v.used = false")
    void invalidateAllUnusedByUser(Long userId);
}
