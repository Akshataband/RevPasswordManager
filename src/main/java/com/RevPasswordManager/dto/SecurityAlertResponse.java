package com.RevPasswordManager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SecurityAlertResponse {

    private List<String> alerts;
}