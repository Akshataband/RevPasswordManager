package com.RevPasswordManager.dto;

import lombok.Data;

import java.util.List;

@Data
public class UpdateSecurityQuestionsRequest {

    private String masterPassword;
    private List<QuestionAnswer> questions;
}