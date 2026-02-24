package com.RevPasswordManager.dto;

import lombok.Data;

@Data
public class SaveGeneratedPasswordRequest {

    private String generatedPassword;

    private String accountName;
    private String website;
    private String username;
    private String category;
    private String notes;
}