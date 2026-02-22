package com.RevPasswordManager.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PasswordResponse {

    private Long id;
    private String accountName;
    private String website;
    private String username;
    private String category;
    private String notes;
    private boolean favorite;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}