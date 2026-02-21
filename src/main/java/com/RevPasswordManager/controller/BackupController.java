package com.RevPasswordManager.controller;

import com.RevPasswordManager.dto.BackupDTO;
import com.RevPasswordManager.service.PasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/backup")
@RequiredArgsConstructor
public class BackupController {

    private final PasswordService passwordService;

    // ================= EXPORT =================
    @GetMapping("/export")
    public ResponseEntity<?> exportBackup(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User userDetails) {

        return ResponseEntity.ok(
                passwordService.exportBackup(userDetails.getUsername())
        );
    }

    // ================= IMPORT =================
    @PostMapping("/import")
    public ResponseEntity<?> importBackup(
            @RequestBody BackupDTO backupDTO,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User userDetails) {

        return ResponseEntity.ok(
                passwordService.importBackup(userDetails.getUsername(), backupDTO)
        );
    }
}