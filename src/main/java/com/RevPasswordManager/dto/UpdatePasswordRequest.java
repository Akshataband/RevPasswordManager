package com.RevPasswordManager.dto;

import lombok.Data;

@Data
public class UpdatePasswordRequest {

    private String accountName;
    private String website;
    private String username;
    private String password; // plain password
    private String category;
    private String notes;
    private boolean favorite;
}