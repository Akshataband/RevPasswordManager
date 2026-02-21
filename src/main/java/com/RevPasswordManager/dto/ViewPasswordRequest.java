package com.RevPasswordManager.dto;

import lombok.Data;

@Data
public class ViewPasswordRequest {

    private Long entryId;

    private String masterPassword;
}