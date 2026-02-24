package com.RevPasswordManager.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Data
public class CreatePasswordRequest {

    @NotBlank(message = "Account name is required")
    private String accountName;

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    private String website;
    private String category;
    private String notes;
}