package com.RevPasswordManager.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class RecentPasswordDTO {

    private Long id;
    private String accountName;
    private String category;
    private LocalDateTime createdAt;
}