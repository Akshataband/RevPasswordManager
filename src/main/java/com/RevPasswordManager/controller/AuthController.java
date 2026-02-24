package com.RevPasswordManager.controller;

import com.RevPasswordManager.dto.*;
import com.RevPasswordManager.service.AuthService;
import com.RevPasswordManager.service.PasswordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordService passwordService;

    // ================= REGISTER =================
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(authService.login(request));
    }

    // ================= SEND OTP =================
    @PostMapping("/send-otp")
    public ResponseEntity<String> sendOtp(
            @RequestBody SendOtpRequest request) {

        return ResponseEntity.ok(
                authService.sendOtp(request.getUsername())
        );
    }

    // ================= VERIFY OTP =================
    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(
            @RequestBody OtpRequest request) {

        return ResponseEntity.ok(authService.verifyOtp(request));
    }
    // ================= UPDATE PROFILE =================
    @PutMapping("/update-profile")
    public ResponseEntity<String> updateProfile(
            @RequestBody UpdateProfileRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                authService.updateProfile(authentication.getName(), request)
        );
    }

    // ================= CHANGE MASTER PASSWORD =================
    @PutMapping("/change-master-password")
    public ResponseEntity<?> changeMasterPassword(
            @RequestBody ChangeMasterPasswordRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                passwordService.changeMasterPassword(
                        authentication.getName(),
                        request.getOldPassword(),
                        request.getNewPassword()
                )
        );
    }

    // ================= TOGGLE 2FA =================
    @PutMapping("/toggle-2fa")
    public ResponseEntity<?> toggle2FA(Authentication authentication) {

        return ResponseEntity.ok(
                authService.toggle2FA(authentication.getName())
        );
    }

    @PostMapping("/verify-2fa")
    public String verify2FA(
            @RequestParam String code,
            Authentication authentication) {

        return authService.verify2FA(
                authentication.getName(),
                code
        );
    }
    @PostMapping("/logout")
    public String logout(
            @RequestHeader("Authorization") String header) {

        String token = header.substring(7);

        authService.blacklistToken(token);

        return "Logged out successfully";
    }

    // ================= VERIFY SECURITY ANSWER =================
    @PostMapping("/verify-security-answer")
    public ResponseEntity<?> verifySecurityAnswer(
            @RequestBody VerifySecurityAnswerRequest request) {

        return ResponseEntity.ok(
                authService.verifySecurityAnswer(request)
        );
    }

    // ================= RESET MASTER PASSWORD =================
    @PutMapping("/reset-master-password")
    public ResponseEntity<?> resetMasterPassword(
            @RequestBody ResetMasterPasswordRequest request) {

        return ResponseEntity.ok(
                authService.resetMasterPassword(request)
        );
    }


    @PutMapping("/security-questions")
    public String updateSecurityQuestions(
            @RequestBody SecurityQuestionRequest request,
            Authentication authentication) {

        return authService.updateSecurityQuestions(
                authentication.getName(),
                request
        );
    }
}