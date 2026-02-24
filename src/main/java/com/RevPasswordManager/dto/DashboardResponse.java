package com.RevPasswordManager.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    private long totalPasswords;
    private long weakPasswords;
    private long reusedPasswords;
    private List<RecentPasswordDTO> recentPasswords;
}