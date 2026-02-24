package com.RevPasswordManager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PasswordStrengthResponse {
    private String strength;
}