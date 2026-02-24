package com.RevPasswordManager.service;

import com.RevPasswordManager.entities.User;
import com.RevPasswordManager.entities.VerificationCode;
import com.RevPasswordManager.repository.VerificationCodeRepository;
import com.RevPasswordManager.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class TwoFactorService {

    private final VerificationCodeRepository repository;
    private final UserRepository userRepository;

    // ✅ FIXED CONSTRUCTOR
    public TwoFactorService(VerificationCodeRepository repository,
                            UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    public String generateCode(User user) {

        String code =
                String.valueOf(new Random().nextInt(900000) + 100000);

        VerificationCode verification = new VerificationCode();
        verification.setCode(code);
        verification.setExpiryTime(
                LocalDateTime.now().plusMinutes(5));
        verification.setUsed(false);
        verification.setUser(user);

        repository.save(verification);

        return code;
    }

    public boolean verifyCode(User user, String code) {

        VerificationCode verification =
                repository.findTopByUserOrderByExpiryTimeDesc(user);

        if (verification == null) return false;
        if (verification.isUsed()) return false;
        if (verification.getExpiryTime()
                .isBefore(LocalDateTime.now())) return false;
        if (!verification.getCode().equals(code)) return false;

        verification.setUsed(true);
        repository.save(verification);

        return true;
    }
}