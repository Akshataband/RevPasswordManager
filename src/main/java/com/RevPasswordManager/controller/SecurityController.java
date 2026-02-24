package com.RevPasswordManager.controller;

import com.RevPasswordManager.dto.SecurityAuditResponse;
import com.RevPasswordManager.dto.SensitiveActionRequest;
import com.RevPasswordManager.service.PasswordService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/security")
public class SecurityController {

    private final PasswordService passwordService;

    public SecurityController(PasswordService passwordService) {
        this.passwordService = passwordService;
    }

    @PostMapping("/audit")
    public ResponseEntity<?> audit(
            @RequestBody SensitiveActionRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                passwordService.securityAudit(
                        authentication.getName(),
                        request.getMasterPassword()
                )
        );
    }

//    =============================================================================
    @PostMapping("/alerts")
    public ResponseEntity<?> getSecurityAlerts(
            @RequestBody SensitiveActionRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                passwordService.getSecurityAlerts(
                        authentication.getName(),
                        request.getMasterPassword()
                )
        );
    }
}
