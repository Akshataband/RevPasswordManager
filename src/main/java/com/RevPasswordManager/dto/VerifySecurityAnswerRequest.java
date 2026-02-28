package com.RevPasswordManager.dto;

import lombok.Data;

import java.util.Map;

@Data
public class VerifySecurityAnswerRequest {

    private String username;
    private Map<String, String> answers;
}