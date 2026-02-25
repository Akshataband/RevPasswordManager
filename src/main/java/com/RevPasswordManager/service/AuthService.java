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

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new CustomException("Username already exists");
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new CustomException("Email already exists");
        }

        if (request.getSecurityQuestions() == null
                || request.getSecurityQuestions().size() < 3) {
            throw new CustomException("Minimum 3 security questions required");
        }

        User user = User.builder()
                .username(request.getUsername())
                .name(request.getName())
                .email(request.getEmail())
                .masterPassword(passwordEncoder.encode(request.getMasterPassword()))
                .role(Role.USER)
                .twoFactorEnabled(false)
                .accountLocked(false)
                .failedAttempts(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        for (QuestionAnswer qa : request.getSecurityQuestions()) {
            SecurityQuestion question = new SecurityQuestion();
            question.setQuestion(qa.getQuestion());
            question.setAnswer(passwordEncoder.encode(qa.getAnswer()));
            question.setUser(user);
            securityQuestionRepository.save(question);
        }

        return AuthResponse.builder()
                .message("Registration successful")
                .otpRequired(false)
                .build();
    }

    // ================= LOGIN =================
    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username"));

        if (!passwordEncoder.matches(request.getMasterPassword(), user.getMasterPassword())) {
            throw new RuntimeException("Invalid password");
        }

        // Generate OTP
        String otp = generateOtp();

        user.setOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(1));
        userRepository.save(user);

        // Print OTP in console
        System.out.println("Generated OTP for " + user.getUsername() + ": " + otp);

        return AuthResponse.builder()
                .otpRequired(true)
                .message("OTP generated")
                .build();
    }

    // ================= VERIFY OTP =================
    public AuthResponse verifyOtp(OtpRequest request) {

        User user = userRepository
                .findByUsernameOrEmail(request.getUsername(), request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getOtp() == null || user.getOtpExpiry() == null) {
            throw new RuntimeException("OTP not generated");
        }

        if (user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired");
        }

        if (!user.getOtp().equals(request.getOtp())) {
            throw new RuntimeException("Invalid OTP");
        }

        user.setOtp(null);
        user.setOtpExpiry(null);
        userRepository.save(user);

        String token = jwtService.generateToken(user.getUsername());

        return AuthResponse.builder()
                .token(token)
                .otpRequired(false)
                .build();
    }

    // ================= GENERATE OTP =================
    private String generateOtp() {
        return String.valueOf((int) (Math.random() * 900000) + 100000);
    }

    // ================= VERIFY SECURITY ANSWER =================
    public String verifySecurityAnswer(VerifySecurityAnswerRequest request) {

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new CustomException("User not found"));

        List<SecurityQuestion> questions =
                securityQuestionRepository.findByUserId(user.getId());

        boolean matched = questions.stream()
                .anyMatch(q ->
                        passwordEncoder.matches(
                                request.getAnswer(),
                                q.getAnswer()
                        )
                );

        if (!matched) {
            throw new CustomException("Incorrect security answer");
        }

        user.setRecoveryVerified(true);
        userRepository.save(user);

        return "Security answer verified successfully";
    }

    // ================= RESET MASTER PASSWORD =================
    public String resetMasterPassword(ResetMasterPasswordRequest request) {

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new CustomException("User not found"));

        // 🔐 Verify current password
        if (!passwordEncoder.matches(
                request.getCurrentMasterPassword(),
                user.getMasterPassword())) {

            throw new CustomException("Current master password is incorrect");
        }

        // 🔄 Update to new password
        user.setMasterPassword(
                passwordEncoder.encode(request.getNewMasterPassword()));

        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        return "Master password updated successfully";
    }

    // ================= TOGGLE 2FA =================
    public String toggle2FA(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("User not found"));

        if (!user.isTwoFactorEnabled()) {

            String secret = java.util.UUID.randomUUID().toString();

            user.setTwoFactorSecret(secret);
            user.setTwoFactorEnabled(true);
            userRepository.save(user);

            return "2FA Enabled. Secret (simulated): " + secret;

        } else {

            user.setTwoFactorSecret(null);
            user.setTwoFactorEnabled(false);
            userRepository.save(user);

            return "2FA Disabled";
        }
    }

    // ================= UPDATE PROFILE =================
    public String updateProfile(String username, UpdateProfileRequest request) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("User not found"));

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());

        userRepository.save(user);

        return "Profile updated successfully";
    }

    // ================= UPDATE SECURITY QUESTIONS =================
    public String updateSecurityQuestions(String username,
                                          SecurityQuestionRequest request) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("User not found"));

        // 🔐 Require master password verification
        if (request.getMasterPassword() == null ||
                !passwordEncoder.matches(
                        request.getMasterPassword(),
                        user.getMasterPassword())) {

            throw new CustomException("Invalid master password");
        }

        if (request.getQuestions() == null ||
                request.getQuestions().size() < 3) {

            throw new CustomException("Minimum 3 questions required");
        }

        // Delete old questions
        securityQuestionRepository.deleteAll(
                securityQuestionRepository.findByUserId(user.getId())
        );

        // Save new questions
        for (QuestionAnswer qa : request.getQuestions()) {

            if (qa.getAnswer() == null || qa.getAnswer().isBlank()) {
                throw new CustomException("Answer cannot be empty");
            }

            SecurityQuestion question = new SecurityQuestion();
            question.setQuestion(qa.getQuestion());
            question.setAnswer(passwordEncoder.encode(qa.getAnswer()));
            question.setUser(user);

            securityQuestionRepository.save(question);
        }

        return "Security questions updated successfully";
    }

    // ================= LOGOUT =================
    public void blacklistToken(String token) {

        BlacklistedToken blacklisted = new BlacklistedToken();
        blacklisted.setToken(token);

        blacklistedTokenRepository.save(blacklisted);
    }
}