package com.RevPasswordManager.dto;

import lombok.Data;
import java.util.List;

@Data
public class BackupDTO {

    private List<PasswordBackupItem> items;
}
