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

    // ================= LOGIN =================
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(authService.login(request));
    }

    // ================= VERIFY 2FA =================
    @PostMapping("/verify-2fa")
    public ResponseEntity<AuthResponse> verify2FA(
            @RequestBody OtpRequest request) {

        System.out.println("USERNAME: " + request.getUsername());
        System.out.println("OTP: " + request.getOtp());

        return ResponseEntity.ok(authService.verify2FA(request));
    }
    // ================= ENABLE 2FA =================
    // ENABLE
    @PostMapping("/enable-2fa")
    public ResponseEntity<?> enable2FA(Authentication authentication) {
        return ResponseEntity.ok(
                authService.enable2FA(authentication.getName())
        );
    }

    // ================= 2FA STATUS =================
    @GetMapping("/2fa-status")
    public ResponseEntity<Boolean> get2FAStatus(Authentication authentication) {

        return ResponseEntity.ok(
                authService.get2FAStatus(authentication.getName())
        );
    }

    // ================= CONFIRM 2FA =================
    @PostMapping("/confirm-2fa")
    public ResponseEntity<?> confirm2FA(
            Authentication authentication,
            @RequestParam String code) {

        return ResponseEntity.ok(
                authService.confirm2FA(authentication.getName(), code)
        );
    }

    // ================= DISABLE 2FA =================
    @PostMapping("/disable-2fa")
    public ResponseEntity<?> disable2FA(
            Authentication authentication,
            @RequestParam String code) {

        return ResponseEntity.ok(
                authService.disable2FA(authentication.getName(), code)
        );
    }

    // ================= LOGOUT =================
    @PostMapping("/logout")
    public String logout(
            @RequestHeader("Authorization") String header) {

        String token = header.substring(7);
        authService.blacklistToken(token);

        return "Logged out successfully";
    }
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {

        return ResponseEntity.ok(
                authService.getCurrentUser(authentication.getName())
        );
    }

}