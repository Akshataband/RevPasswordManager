package com.RevPasswordManager.service;

import com.RevPasswordManager.entities.User;
import com.RevPasswordManager.entities.VerificationCode;
import com.RevPasswordManager.exception.CustomException;
import com.RevPasswordManager.repository.VerificationCodeRepository;
import com.RevPasswordManager.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class TwoFactorService {

    private final VerificationCodeRepository repository;
    private final UserRepository userRepository;

    public TwoFactorService(VerificationCodeRepository repository,
                            UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    // ✅ GENERATE OTP (Invalidate old ones)
    public String generateCode(User user) {

        // 🔐 Invalidate all previous unused codes
        repository.invalidateAllUnusedByUser(user.getId());

        String code =
                String.valueOf(new Random().nextInt(900000) + 100000);

        VerificationCode verification = new VerificationCode();
        verification.setCode(code);
        verification.setExpiryTime(
                LocalDateTime.now().plusMinutes(1)); // 🔥 1 minute
        verification.setUsed(false);
        verification.setUser(user);

        repository.save(verification);

        return code;
    }

    // ✅ VERIFY OTP
    public boolean verifyCode(User user, String code) {

        VerificationCode verification =
                repository.findTopByUserOrderByExpiryTimeDesc(user);

        if (verification == null) return false;

        // ❌ Already used
        if (verification.isUsed()) return false;

        // ⏳ Expired
        if (verification.getExpiryTime()
                .isBefore(LocalDateTime.now())) return false;

        // ❌ Wrong code
        if (!verification.getCode().equals(code)) return false;

        // ✅ Single-use enforcement
        verification.setUsed(true);
        repository.save(verification);

        return true;
    }
}