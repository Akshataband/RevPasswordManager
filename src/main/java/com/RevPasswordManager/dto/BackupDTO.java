package com.RevPasswordManager.dto;

import com.RevPasswordManager.entities.PasswordEntry;
import lombok.Data;
import java.util.List;

@Data
public class BackupDTO {

    private List<PasswordEntry> entries;
}