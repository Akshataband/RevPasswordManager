package com.RevPasswordManager.controller;

import com.RevPasswordManager.dto.*;
import com.RevPasswordManager.service.PasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/generator")
@RequiredArgsConstructor
public class GeneratorController {

    private final PasswordService passwordService;

    @PostMapping
    public List<GeneratedPasswordResponse> generate(
            @RequestBody PasswordGeneratorRequest request) {

        return passwordService.generatePasswords(request);
    }

    @PostMapping("/strength")
    public PasswordStrengthResponse checkStrength(
            @RequestBody PasswordStrengthRequest request) {

        return passwordService.checkPasswordStrength(
                request.getPassword()
        );
    }
}