package com.RevPasswordManager.controller;

import com.RevPasswordManager.service.PasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final PasswordService passwordService;

    @GetMapping
    public ResponseEntity<?> getDashboard(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User userDetails) {

        return ResponseEntity.ok(
                passwordService.getDashboard(userDetails.getUsername())
        );
    }
}