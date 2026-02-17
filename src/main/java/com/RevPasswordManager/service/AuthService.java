package com.RevPasswordManager.service;

import com.RevPasswordManager.dto.AuthResponse;
import com.RevPasswordManager.dto.LoginRequest;
import com.RevPasswordManager.dto.OtpRequest;
import com.RevPasswordManager.dto.RegisterRequest;
import com.RevPasswordManager.entities.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.RevPasswordManager.repository.UserRepository;
import com.RevPasswordManager.security.JwtService;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TwoFactorService twoFactorService;


    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       TwoFactorService twoFactorService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.twoFactorService = twoFactorService;
    }


    public String register(RegisterRequest request) {

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .build();

        userRepository.save(user);
        return "User registered successfully";
    }



    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        if (user.isTwoFactorEnabled())
        {

            String code = twoFactorService.generateCode(user);

            return new AuthResponse("OTP_REQUIRED");
        }

        String token = jwtService.generateToken(user.getUsername());

        return new AuthResponse(token);
    }
    public AuthResponse verifyOtp(OtpRequest request) {

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean valid = twoFactorService.verifyCode(user, request.getCode());

        if (!valid) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        String token = jwtService.generateToken(user.getUsername());

        return new AuthResponse(token);
    }



}
