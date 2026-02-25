package com.RevPasswordManager.controller;

import com.RevPasswordManager.entities.User;
import com.RevPasswordManager.repository.UserRepository;
import com.RevPasswordManager.service.TwoFactorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/2fa")
@RequiredArgsConstructor
public class TwoFactorController {

    private final UserRepository userRepository;
    private final TwoFactorService twoFactorService;

    @PostMapping("/enable")
    public ResponseEntity<?> enable(Authentication authentication) {

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setTwoFactorEnabled(true);
        userRepository.save(user);

        return ResponseEntity.ok("Two-factor authentication enabled");
    }

    @PostMapping("/disable")
    public ResponseEntity<?> disable(Authentication authentication) {

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setTwoFactorEnabled(false);
        userRepository.save(user);

        return ResponseEntity.ok("Two-factor authentication disabled");
    }

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(Authentication authentication) {

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isTwoFactorEnabled()) {
            return ResponseEntity.badRequest()
                    .body("Two-factor authentication is not enabled");
        }

        String code = twoFactorService.generateCode(user);

        // Simulation only
        System.out.println("2FA OTP: " + code);

        return ResponseEntity.ok("OTP sent successfully");
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(
            @RequestParam String code,
            Authentication authentication) {

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean valid = twoFactorService.verifyCode(user, code);

        if (!valid) {
            return ResponseEntity.badRequest()
                    .body("Invalid or expired OTP");
        }

        return ResponseEntity.ok("OTP verified successfully");
    }
}