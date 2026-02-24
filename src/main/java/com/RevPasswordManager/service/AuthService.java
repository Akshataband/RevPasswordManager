package com.RevPasswordManager.service;

import com.RevPasswordManager.dto.*;
import com.RevPasswordManager.entities.*;
import com.RevPasswordManager.exception.CustomException;
import com.RevPasswordManager.repository.*;
import com.RevPasswordManager.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;


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

        String token = jwtService.generateToken(user.getUsername());

        return AuthResponse.builder()
                .token(token)
                .message("Registration successful")
                .otpRequired(false)
                .build();
    }


    // ================= LOGIN =================
    public AuthResponse login(LoginRequest request) {

        // 1️⃣ Find user
        User user = userRepository.findByUsername(request.getUsername())
                .or(() -> userRepository.findByEmail(request.getUsername()))
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Invalid username or password"
                        )
                );

        // 2️⃣ Check account lock
        if (user.isAccountLocked()) {
            throw new ResponseStatusException(
                    HttpStatus.LOCKED,
                    "Account is locked due to multiple failed attempts"
            );
        }

        // 3️⃣ Validate password
        boolean passwordMatches = passwordEncoder.matches(
                request.getMasterPassword(),
                user.getMasterPassword()
        );

        if (!passwordMatches) {

            int attempts = user.getFailedAttempts() + 1;
            user.setFailedAttempts(attempts);

            if (attempts >= 5) {
                user.setAccountLocked(true);
            }

            userRepository.save(user);

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid username or password"
            );
        }

        // 4️⃣ Reset failed attempts
        user.setFailedAttempts(0);
        userRepository.save(user);

        // 5️⃣ Handle 2FA
        if (user.isTwoFactorEnabled()) {

            String otp = twoFactorService.generateCode(user);

            System.out.println("Generated OTP: " + otp);

            return AuthResponse.builder()
                    .message("OTP required")
                    .otpRequired(true)
                    .build();
        }

        // 6️⃣ Normal login (no 2FA)
        String token = jwtService.generateToken(user.getUsername());

        return AuthResponse.builder()
                .token(token)
                .otpRequired(false)
                .message("Login successful")
                .build();
    }

//    =================================================================================
    public String sendOtp(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("User not found"));

        String otp = twoFactorService.generateCode(user);

        return "OTP generated: " + otp; // simulation
    }
    // ================= VERIFY OTP (LOGIN STEP 2) =================
    public AuthResponse verifyOtp(OtpRequest request) {

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new CustomException("User not found"));

        boolean valid = twoFactorService.verifyCode(user, request.getCode());

        if (!valid) {
            throw new CustomException("Invalid or expired OTP");
        }

        String token = jwtService.generateToken(user.getUsername());

        return AuthResponse.builder()
                .token(token)
                .otpRequired(false)
                .build();
    }


    public String verify2FA(String username, String code) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("User not found"));

        if (!user.isTwoFactorEnabled()) {
            throw new CustomException("2FA is not enabled");
        }

        boolean valid = twoFactorService.verifyCode(user, code);

        if (!valid) {
            throw new CustomException("Invalid or expired 2FA code");
        }

        return "2FA verification successful";
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

        if (!user.isRecoveryVerified()) {
            throw new CustomException("Security verification required before reset");
        }

        user.setMasterPassword(
                passwordEncoder.encode(request.getNewMasterPassword()));

        user.setRecoveryVerified(false);
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        return "Master password reset successfully";
    }

    // ================= TOGGLE 2FA =================
    public String toggle2FA(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("User not found"));

        if (!user.isTwoFactorEnabled()) {

            // generate secret
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

        if (request.getQuestions() == null || request.getQuestions().size() < 3) {
            throw new CustomException("Minimum 3 questions required");
        }

        securityQuestionRepository.deleteAll(
                securityQuestionRepository.findByUserId(user.getId())
        );

        for (QuestionAnswer qa : request.getQuestions()) {

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