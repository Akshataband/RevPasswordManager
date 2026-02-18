package com.RevPasswordManager.controller;
import com.RevPasswordManager.dto.*;
import org.springframework.web.bind.annotation.*;
import com.RevPasswordManager.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/verify-otp")
    public AuthResponse verifyOtp(@RequestBody OtpRequest request) {
        return authService.verifyOtp(request);
    }

    @PutMapping("/profile")
    public String updateProfile(@RequestBody UpdateProfileRequest request) {
        authService.updateProfile(request);
        return "Profile updated successfully";
    }

}
