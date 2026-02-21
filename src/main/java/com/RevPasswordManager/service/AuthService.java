package com.RevPasswordManager.service;

import com.RevPasswordManager.dto.LoginRequest;
import com.RevPasswordManager.entities.User;
import com.RevPasswordManager.repository.UserRepository;
import com.RevPasswordManager.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public String login(LoginRequest request) {

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 🔒 Check if account locked
        if (user.isAccountLocked()) {
            throw new RuntimeException("Account is locked due to multiple failed attempts");
        }

        // ❌ Wrong password
        if (!passwordEncoder.matches(request.getPassword(), user.getMasterPassword())) {

            user.setFailedAttempts(user.getFailedAttempts() + 1);

            if (user.getFailedAttempts() >= 5) {
                user.setAccountLocked(true);
            }

            userRepository.save(user);
            throw new RuntimeException("Invalid credentials");
        }

        // ✅ Successful login
        user.setFailedAttempts(0);
        userRepository.save(user);

        return jwtService.generateToken(user.getUsername());
    }
}