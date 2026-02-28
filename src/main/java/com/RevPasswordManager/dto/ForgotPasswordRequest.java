package com.RevPasswordManager.dto;

import lombok.Data;

@Data
public class ForgotPasswordRequest {
    private String username;
    private String question;
    private String answer;
    private String newPassword;
}