package com.RevPasswordManager.service;

import com.RevPasswordManager.entities.User;
import com.RevPasswordManager.entities.VerificationCode;
import org.springframework.stereotype.Service;
import com.RevPasswordManager.repository.VerificationCodeRepository;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class TwoFactorService {

    private final VerificationCodeRepository repository;

    public TwoFactorService(VerificationCodeRepository repository) {
        this.repository = repository;
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
}
