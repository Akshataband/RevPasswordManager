package com.RevPasswordManager.controller;

import com.RevPasswordManager.dto.DashboardResponse;
import com.RevPasswordManager.service.PasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final PasswordService passwordService;

    @GetMapping
    public DashboardResponse dashboard(Authentication authentication) {
        return passwordService.getDashboardSummary(authentication.getName());
    }
}