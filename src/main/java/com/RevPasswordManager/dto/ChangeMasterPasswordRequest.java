package com.RevPasswordManager.dto;

import lombok.Data;

@Data
public class ChangeMasterPasswordRequest {

    private String oldPassword;
    private String newPassword;
}