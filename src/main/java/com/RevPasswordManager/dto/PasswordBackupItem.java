package com.RevPasswordManager.dto;

import lombok.Data;

@Data
public class PasswordBackupItem {

    private String accountName;
    private String website;
    private String username;
    private String encryptedPassword;
    private String category;
    private String notes;
    private boolean favorite;
}
