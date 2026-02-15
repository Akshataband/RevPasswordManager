package com.RevPasswordManager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DashboardResponse {

    private int totalPasswords;
    private int weakPasswords;
    private int favoritePasswords;
}
