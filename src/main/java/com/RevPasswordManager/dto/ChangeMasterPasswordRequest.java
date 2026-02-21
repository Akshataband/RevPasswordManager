package com.RevPasswordManager.dto;

import lombok.Data;

@Data
public class ChangeMasterPasswordRequest {

    private String currentPassword;
    private String newPassword;
}