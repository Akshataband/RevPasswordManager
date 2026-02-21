package com.RevPasswordManager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SecurityAuditResponse {

    private int weakPasswords;
    private int reusedPasswords;
    private int oldPasswords;
}