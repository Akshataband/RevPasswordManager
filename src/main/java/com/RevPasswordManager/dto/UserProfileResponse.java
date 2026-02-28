package com.RevPasswordManager.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserProfileResponse {

    private String name;
    private String username;
    private String email;
    private String phoneNumber;
    private LocalDateTime createdAt;
}