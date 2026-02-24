package com.RevPasswordManager.controller;

import com.RevPasswordManager.dto.BackupDTO;
import com.RevPasswordManager.dto.ImportBackupRequest;
import com.RevPasswordManager.dto.SensitiveActionRequest;
import com.RevPasswordManager.service.PasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/backup")
@RequiredArgsConstructor
public class BackupController {

    private final PasswordService passwordService;

    @PostMapping("/export")
    public ResponseEntity<?> export(
            @RequestBody SensitiveActionRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                passwordService.exportBackup(
                        authentication.getName(),
                        request.getMasterPassword()
                )
        );
    }
    @PostMapping("/import")
    public ResponseEntity<?> importBackup(
            @RequestBody ImportBackupRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                passwordService.importBackup(
                        authentication.getName(),
                        request.getMasterPassword(),
                        request.getEncryptedData()
                )
        );
    }
}