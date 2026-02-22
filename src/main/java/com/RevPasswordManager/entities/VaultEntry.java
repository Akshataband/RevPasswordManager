package com.RevPasswordManager.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import com.RevPasswordManager.entities.User;

@Entity
@Table(name = "vault_entries")
@Getter
@Setter
public class VaultEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String accountName;
    private String website;
    private String username;

    @Column(length = 500)
    private String encryptedPassword;

    private String category;
    private String notes;
    private boolean favorite;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;   // ✅ This must be your entity User
}