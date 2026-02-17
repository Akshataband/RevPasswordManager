package com.RevPasswordManager.dto;

import lombok.Data;

import java.util.List;

@Data
public class SecurityQuestionRequest {

    private List<QuestionAnswer> questions;
}
