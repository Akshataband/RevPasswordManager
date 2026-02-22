package com.RevPasswordManager.controller;

import com.RevPasswordManager.service.PasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final PasswordService passwordService;

    @GetMapping
    public String dashboard() {
        return "Dashboard working";
    }
}