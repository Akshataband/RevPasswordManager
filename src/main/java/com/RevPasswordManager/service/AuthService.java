package com.RevPasswordManager.service;

import com.RevPasswordManager.dto.*;
import com.RevPasswordManager.entities.*;
import com.RevPasswordManager.exception.CustomException;
import com.RevPasswordManager.repository.*;
import com.RevPasswordManager.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final SecurityQuestionRepository securityQuestionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final TwoFactorService twoFactorService;

    // ================= REGISTER =================
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.findByUsername(request.getUsername()).isPresent())
            throw new CustomException("Username already exists");

        if (userRepository.findByEmail(request.getEmail()).isPresent())
            throw new CustomException("Email already exists");

        if (request.getSecurityAnswers() == null ||
                request.getSecurityAnswers().size() < 3)
            throw new CustomException("Minimum 3 security answers required");

        User user = User.builder()
                .username(request.getUsername())
                .name(request.getName())
                .email(request.getEmail())
                .masterPassword(passwordEncoder.encode(request.getMasterPassword()))
                .role(Role.USER)
                .twoFactorEnabled(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        return AuthResponse.builder()
                .message("Registration successful")
                .otpRequired(false)
                .build();
    }

    // ================= LOGIN =================
    public AuthResponse login(LoginRequest request) {

        User user = userRepository
                .findByUsernameOrEmail(request.getUsername(), request.getUsername())
                .orElseThrow(() ->
                        new CustomException("Invalid username or email"));

        if (!passwordEncoder.matches(
                request.getMasterPassword(),
                user.getMasterPassword())) {

            throw new CustomException("Invalid password");
        }

        // 🔐 If 2FA disabled → Direct login
        if (!user.isTwoFactorEnabled()) {

            String token = jwtService.generateToken(user.getUsername());

            return AuthResponse.builder()
                    .token(token)
                    .otpRequired(false)
                    .message("Login successful")
                    .build();
        }

        // 🔐 If 2FA enabled → Require TOTP
        return AuthResponse.builder()
                .otpRequired(true)
                .message("2FA verification required")
                .build();
    }

    // ================= VERIFY 2FA =================
    public AuthResponse verify2FA(OtpRequest request) {

        User user = userRepository
                .findByUsernameOrEmail(request.getUsername(), request.getUsername())
                .orElseThrow(() -> new CustomException("User not found"));

        boolean valid = twoFactorService
                .verifyCode(user.getTwoFactorSecret(), request.getOtp());

        if (!valid)
            throw new CustomException("Invalid authentication code");

        String token = jwtService.generateToken(user.getUsername());

        return AuthResponse.builder()
                .token(token)
                .otpRequired(false)
                .message("Login successful")
                .build();
    }

    // ================= ENABLE 2FA =================
    public Map<String, String> enable2FA(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("User not found"));

        if (user.getTwoFactorSecret() == null) {
            String secret = twoFactorService.generateSecret();
            user.setTwoFactorSecret(secret);
            user.setTwoFactorEnabled(false);
            userRepository.save(user);
        }

        String qr = "otpauth://totp/RevPasswordManager:" + user.getUsername()
                + "?secret=" + user.getTwoFactorSecret()
                + "&issuer=RevPasswordManager";

        return Map.of("qr", qr);
    }
    // ================= 2FA STATUS =================
    public boolean get2FAStatus(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("User not found"));

        return user.isTwoFactorEnabled();
    }

    // ================= CONFIRM 2FA =================
    public String confirm2FA(String username, String code) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("User not found"));

        if (user.getTwoFactorSecret() == null) {
            throw new CustomException("2FA not initialized");
        }

        System.out.println("Username: " + username);
        System.out.println("Secret: " + user.getTwoFactorSecret());
        System.out.println("OTP Received: " + code);

        boolean isValid = twoFactorService
                .verifyCode(user.getTwoFactorSecret(), code);

        if (!isValid) {
            throw new CustomException("Invalid or expired OTP");
        }

        user.setTwoFactorEnabled(true);
        userRepository.save(user);

        return "2FA enabled successfully";
    }

    // ================= DISABLE 2FA =================
    public String disable2FA(String username, String code) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("User not found"));

        if (!user.isTwoFactorEnabled()) {
            throw new CustomException("2FA is not enabled");
        }

        boolean valid = twoFactorService
                .verifyCode(user.getTwoFactorSecret(), code);

        if (!valid) {
            throw new CustomException("Invalid OTP");
        }


        user.setTwoFactorEnabled(false);
        user.setTwoFactorSecret(null);   // optional but recommended
        userRepository.save(user);

        return "2FA disabled successfully";
    }
    // ================= LOGOUT =================
    public void blacklistToken(String token) {

        BlacklistedToken blacklisted = new BlacklistedToken();
        blacklisted.setToken(token);
        blacklistedTokenRepository.save(blacklisted);
    }

    public UserProfileResponse getCurrentUser(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("User not found"));

        return UserProfileResponse.builder()
                .name(user.getName())
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .createdAt(user.getCreatedAt())
                .build();
    }
}