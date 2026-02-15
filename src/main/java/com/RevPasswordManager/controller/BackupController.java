package com.RevPasswordManager.controller;

import com.RevPasswordManager.dto.BackupDTO;
import org.springframework.web.bind.annotation.*;
import com.RevPasswordManager.service.PasswordService;

@RestController
@RequestMapping("/api/backup")
public class BackupController {

    private final PasswordService passwordService;

    public BackupController(PasswordService passwordService) {
        this.passwordService = passwordService;
    }

    @GetMapping("/export/{userId}")
    public BackupDTO export(@PathVariable Long userId) {
        return passwordService.exportBackup(userId);
    }

    @PostMapping("/import/{userId}")
    public String importBackup(@PathVariable Long userId,
                               @RequestBody BackupDTO backup) {

        passwordService.importBackup(userId, backup);
        return "Backup imported successfully";
    }
}
