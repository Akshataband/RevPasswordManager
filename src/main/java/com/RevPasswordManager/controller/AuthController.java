package com.RevPasswordManager.controller;
import com.RevPasswordManager.dto.AuthResponse;
import com.RevPasswordManager.dto.LoginRequest;
import com.RevPasswordManager.dto.RegisterRequest;
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
}
