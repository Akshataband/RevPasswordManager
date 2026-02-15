package com.RevPasswordManager.dto;

import lombok.Data;

@Data
public class SecurityAuditResponse {

    private int weakPasswords;
    private int reusedPasswords;
    private int oldPasswords;
}
