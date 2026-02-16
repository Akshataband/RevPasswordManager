package com.RevPasswordManager.controller;

import com.RevPasswordManager.dto.SecurityAuditResponse;
import com.RevPasswordManager.service.PasswordService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/security")
public class SecurityController {

    private final PasswordService passwordService;

    public SecurityController(PasswordService passwordService) {
        this.passwordService = passwordService;
    }

    @GetMapping("/audit")
    public SecurityAuditResponse audit() {
        return passwordService.securityAudit();
    }
}
