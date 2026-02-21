package com.RevPasswordManager.controller;

import com.RevPasswordManager.dto.SecurityAuditResponse;
import com.RevPasswordManager.service.PasswordService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/security")
public class SecurityController {

    private final PasswordService passwordService;

    public SecurityController(PasswordService passwordService) {
        this.passwordService = passwordService;
    }

    @GetMapping("/audit")
    public ResponseEntity<?> audit(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User userDetails) {

        return ResponseEntity.ok(
                passwordService.securityAudit(userDetails.getUsername())
        );
    }
}
