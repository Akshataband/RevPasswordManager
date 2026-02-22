package com.RevPasswordManager.service;

import com.RevPasswordManager.dto.*;
import com.RevPasswordManager.entities.User;
import com.RevPasswordManager.exception.CustomException;
import com.RevPasswordManager.repository.UserRepository;
import com.RevPasswordManager.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // ================= REGISTER =================
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new CustomException("Username already exists");
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new CustomException("Email already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .masterPassword(passwordEncoder.encode(request.getMasterPassword()))
                .twoFactorEnabled(false)
                .accountLocked(false)
                .failedAttempts(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user.getUsername());

        return new AuthResponse(token);
    }

    // ================= LOGIN =================
    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new CustomException("User not found"));

        if (user.isAccountLocked()) {
            throw new CustomException("Account is locked");
        }

        if (!passwordEncoder.matches(request.getMasterPassword(), user.getMasterPassword())) {

            user.setFailedAttempts(user.getFailedAttempts() + 1);

            if (user.getFailedAttempts() >= 5) {
                user.setAccountLocked(true);
            }

            userRepository.save(user);

            throw new CustomException("Invalid credentials");
        }

        user.setFailedAttempts(0);
        userRepository.save(user);

        String token = jwtService.generateToken(user.getUsername());
        return new AuthResponse(token);
    }

    // ================= VERIFY OTP =================
    public String verifyOtp(OtpRequest request) {

        // For now simple simulation
        if (!"123456".equals(request.getOtp())) {
            throw new CustomException("Invalid OTP");
        }

        return "OTP verified successfully";
    }

    // ================= UPDATE PROFILE =================
    public String updateProfile(UpdateProfileRequest request) {

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new CustomException("User not found"));

        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        return "Profile updated successfully";
    }

    // ================= CHANGE MASTER PASSWORD =================
    public String changeMasterPassword(
            String username,
            ChangeMasterPasswordRequest request) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("User not found"));

        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getMasterPassword())) {

            throw new CustomException("Current password is incorrect");
        }

        user.setMasterPassword(
                passwordEncoder.encode(request.getNewPassword()));

        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        return "Master password updated successfully";
    }

    // ================= TOGGLE 2FA =================
    public String toggle2FA(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("User not found"));

        user.setTwoFactorEnabled(!user.isTwoFactorEnabled());
        userRepository.save(user);

        return user.isTwoFactorEnabled()
                ? "2FA Enabled"
                : "2FA Disabled";
    }
}