package com.RevPasswordManager.controller;

import com.RevPasswordManager.dto.DashboardResponse;
import org.springframework.web.bind.annotation.*;
import com.RevPasswordManager.service.PasswordService;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final PasswordService passwordService;

    public DashboardController(PasswordService passwordService) {
        this.passwordService = passwordService;
    }

    @GetMapping("/{userId}")
    public DashboardResponse getDashboard(@PathVariable Long userId) {
        return passwordService.getDashboard(userId);
    }
}
