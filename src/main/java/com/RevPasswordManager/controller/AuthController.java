package com.RevPasswordManager.controller;

import com.RevPasswordManager.dto.*;
import com.RevPasswordManager.service.AuthService;
import com.RevPasswordManager.service.PasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import com.RevPasswordManager.dto.UpdateProfileRequest;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ✅ REGISTER
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @RequestBody RegisterRequest request) {

        return ResponseEntity.ok(authService.register(request));
    }

    // ✅ LOGIN
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody LoginRequest request) {

        return ResponseEntity.ok(authService.login(request));
    }

    // ✅ VERIFY OTP
    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(
            @RequestBody OtpRequest request) {

        return ResponseEntity.ok(authService.verifyOtp(request));
    }

    @PutMapping("/update-profile")
    public ResponseEntity<String> updateProfile(
            @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User userDetails) {

        return ResponseEntity.ok(
                authService.updateProfile(
                        userDetails.getUsername(),
                        request
                )
        );
    }
    private final PasswordService passwordService;
    @PutMapping("/change-master-password")
    public ResponseEntity<?> changeMasterPassword(
            @RequestBody ChangeMasterPasswordRequest request,
            org.springframework.security.core.Authentication authentication) {

        String username = authentication.getName();

        return ResponseEntity.ok(
                passwordService.changeMasterPassword(
                        username,
                        request.getOldPassword(),
                        request.getNewPassword()
                )
        );
    }

    @PutMapping("/toggle-2fa")
    public ResponseEntity<?> toggle2FA(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User userDetails) {

        return ResponseEntity.ok(
                authService.toggle2FA(userDetails.getUsername())
        );
    }

    @PostMapping("/verify-security-answer")
    public ResponseEntity<?> verifySecurityAnswer(
            @RequestBody VerifySecurityAnswerRequest request) {

        return ResponseEntity.ok(
                authService.verifySecurityAnswer(request)
        );
    }
    @PutMapping("/reset-master-password")
    public ResponseEntity<?> resetMasterPassword(
            @RequestBody ResetMasterPasswordRequest request) {

        return ResponseEntity.ok(
                authService.resetMasterPassword(request)
        );
    }
}