package com.RevPasswordManager.dto;

import lombok.Data;

@Data
public class SecurityQuestionRequest {
    private String question;
    private String answer;
}
