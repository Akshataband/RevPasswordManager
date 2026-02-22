package com.RevPasswordManager.dto;

import lombok.Data;

@Data
public class PasswordGeneratorRequest {

    private int length;
    private boolean includeUppercase;
    private boolean includeLowercase;
    private boolean includeNumbers;
    private boolean includeSpecial;
}