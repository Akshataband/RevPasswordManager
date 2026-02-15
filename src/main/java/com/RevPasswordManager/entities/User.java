package com.RevPasswordManager.entities;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
@Builder
@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    @Column(unique = true)
    private String email;

    private String password;
    private String phone;
    private boolean twoFactorEnabled;
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<PasswordEntry> passwordEntries;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<SecurityQuestion> securityQuestions;
}
