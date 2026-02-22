package com.RevPasswordManager.dto;

import lombok.Data;

@Data
public class ResetMasterPasswordRequest {

    private String username;
    private String newMasterPassword;
}