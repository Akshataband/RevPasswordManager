package com.RevPasswordManager.dto;

import lombok.Data;

@Data
public class VerifySecurityAnswerRequest {

    private String username;
    private String answer;
}