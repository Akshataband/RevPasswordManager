package com.RevPasswordManager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GeneratedPasswordResponse {

    private String password;
    private String strength;
}