package com.RevPasswordManager.service;

import dev.samstevens.totp.code.*;
import dev.samstevens.totp.secret.*;
import dev.samstevens.totp.time.*;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class TwoFactorService {

    private final SecretGenerator secretGenerator = new DefaultSecretGenerator();
    private final TimeProvider timeProvider = new SystemTimeProvider();
    private final DefaultCodeGenerator codeGenerator = new DefaultCodeGenerator();

    public String generateSecret() {
        return secretGenerator.generate();
    }

    public boolean verifyCode(String secret, String code) {
        CodeVerifier verifier =
                new DefaultCodeVerifier(codeGenerator, timeProvider);
        return verifier.isValidCode(secret, code);
    }

    // 🔥 Generate QR URL for Google Authenticator
    public String generateQrUrl(String secret, String username) {

        String issuer = "RevPasswordManager";

        String otpAuthUrl = String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s",
                issuer,
                username,
                secret,
                issuer
        );

        // Use Google Chart API to generate QR image
        return "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data="
                + URLEncoder.encode(otpAuthUrl, StandardCharsets.UTF_8);
    }
}