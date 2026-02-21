package com.RevPasswordManager.controller;

import com.RevPasswordManager.dto.*;
import com.RevPasswordManager.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    // ✅ UPDATE PROFILE
    @PutMapping("/update-profile")
    public ResponseEntity<String> updateProfile(
            @RequestBody UpdateProfileRequest request) {

        return ResponseEntity.ok(authService.updateProfile(request));
    }

    @PutMapping("/change-master-password")
    public ResponseEntity<?> changeMasterPassword(
            @RequestBody ChangeMasterPasswordRequest request,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User userDetails) {

        return ResponseEntity.ok(
                authService.changeMasterPassword(
                        userDetails.getUsername(),
                        request
                )
        );
    }
}