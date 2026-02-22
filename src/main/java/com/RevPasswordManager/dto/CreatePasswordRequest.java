package com.RevPasswordManager.dto;

import lombok.Data;

@Data
public class CreatePasswordRequest {

    private String accountName;
    private String website;
    private String username;
    private String password;
    private String category;
    private String notes;
}