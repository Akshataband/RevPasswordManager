package com.RevPasswordManager.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileResponse {

    private String username;
    private String email;
    private String phoneNumber;
    private boolean twoFactorEnabled;
}