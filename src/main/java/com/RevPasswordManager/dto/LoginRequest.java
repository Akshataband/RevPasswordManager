package com.RevPasswordManager.dto;

import lombok.Data;

@Data
public class LoginRequest {

    private String username;
    private String masterPassword;
}