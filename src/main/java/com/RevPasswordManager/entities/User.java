package com.RevPasswordManager.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String masterPassword;

    @Column(nullable = false, length = 100)
    private String name;


    private boolean twoFactorEnabled;
    private boolean accountLocked;
    private int failedAttempts;

    private String phoneNumber;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private boolean recoveryVerified;
    private String twoFactorSecret;

    private String otp;

    private LocalDateTime otpExpiry;

    @Enumerated(EnumType.STRING)
    private Role role;

}