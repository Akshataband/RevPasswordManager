package com.RevPasswordManager.dto;

import lombok.Data;

@Data
public class ImportBackupRequest {
    private String masterPassword;
    private String encryptedData;
}