package com.RevPasswordManager.service;

import com.RevPasswordManager.dto.AuthResponse;
import com.RevPasswordManager.dto.LoginRequest;
import com.RevPasswordManager.dto.RegisterRequest;
import com.RevPasswordManager.entities.User;
import com.RevPasswordManager.exception.CustomException;
import com.RevPasswordManager.repository.UserRepository;
import com.RevPasswordManager.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;

    @BeforeEach
    void setup() {

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@mail.com");
        registerRequest.setMasterPassword("password123");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setMasterPassword("Password@123");

        user = User.builder()
                .username("testuser")
                .email("test@mail.com")
                .masterPassword("encodedPassword")
                .accountLocked(false)
                .failedAttempts(0)
                .build();
    }

    // ✅ Register Success
    @Test
    void testRegisterSuccess() {

        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.empty());

        when(userRepository.findByEmail("test@mail.com"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("password123"))
                .thenReturn("encodedPassword");

        when(jwtService.generateToken("testuser"))
                .thenReturn("mockToken");

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("mockToken", response.getToken());
        assertEquals("User registered successfully", response.getMessage());

        verify(userRepository, times(1)).save(any(User.class));
    }

    // ✅ Login Success
    @Test
    void testLoginSuccess() {

        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(anyString(), anyString()))
                .thenReturn(true);

        when(jwtService.generateToken("testuser"))
                .thenReturn("mockToken");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("mockToken", response.getToken());
        assertEquals("Login successful", response.getMessage());
    }

    // ❌ Login Invalid Password
    @Test
    void testLoginInvalidPassword() {

        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(anyString(), anyString()))
                .thenReturn(false);

        CustomException exception = assertThrows(
                CustomException.class,
                () -> authService.login(loginRequest)
        );

        assertEquals("Invalid credentials", exception.getMessage());
    }
}