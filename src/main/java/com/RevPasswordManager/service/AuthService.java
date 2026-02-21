package com.RevPasswordManager.service;

import com.RevPasswordManager.dto.*;
import com.RevPasswordManager.entities.User;
import com.RevPasswordManager.entities.VerificationCode;
import com.RevPasswordManager.repository.UserRepository;
import com.RevPasswordManager.repository.VerificationCodeRepository;
import com.RevPasswordManager.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final VerificationCodeRepository verificationCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // ================= REGISTER =================
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .masterPassword(passwordEncoder.encode(request.getMasterPassword()))
                .twoFactorEnabled(false)
                .failedAttempts(0)
                .accountLocked(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user.getUsername());

        return new AuthResponse(token, "User registered successfully");
    }

    // ================= LOGIN =================
    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isAccountLocked()) {
            throw new RuntimeException("Account locked");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getMasterPassword())) {

            user.setFailedAttempts(user.getFailedAttempts() + 1);

            if (user.getFailedAttempts() >= 5) {
                user.setAccountLocked(true);
            }

            userRepository.save(user);

            throw new RuntimeException("Invalid credentials");
        }

        user.setFailedAttempts(0);
        userRepository.save(user);

        // Generate OTP if 2FA enabled
        if (user.isTwoFactorEnabled()) {
            generateOtp(user);
            return new AuthResponse(null, "OTP sent to email");
        }

        String token = jwtService.generateToken(user.getUsername());
        return new AuthResponse(token, "Login successful");
    }

    // ================= GENERATE OTP =================
    private void generateOtp(User user) {

        String otp = String.valueOf(100000 + new Random().nextInt(900000));

        VerificationCode code = VerificationCode.builder()
                .code(otp)
                .user(user)
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .build();

        verificationCodeRepository.save(code);

        System.out.println("OTP: " + otp); // simulate email
    }

    // ================= VERIFY OTP =================
    public String verifyOtp(OtpRequest request) {

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        VerificationCode code = verificationCodeRepository
                .findByUserAndCode(user, request.getOtp())
                .orElseThrow(() -> new RuntimeException("Invalid OTP"));

        if (code.isUsed()) {
            throw new RuntimeException("OTP already used");
        }

        if (code.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired");
        }

        code.setUsed(true);
        verificationCodeRepository.save(code);

        return jwtService.generateToken(user.getUsername());
    }

    // ================= UPDATE PROFILE =================
    public String updateProfile(UpdateProfileRequest request) {

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        return "Profile updated successfully";
    }
}