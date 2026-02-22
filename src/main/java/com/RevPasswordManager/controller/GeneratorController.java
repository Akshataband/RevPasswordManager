package com.RevPasswordManager.controller;

import com.RevPasswordManager.dto.PasswordGeneratorRequest;
import com.RevPasswordManager.service.PasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GeneratorController {

    private final PasswordService passwordService;

    @PostMapping("/generator")
    public ResponseEntity<String> generate(
            @RequestBody PasswordGeneratorRequest request) {

        String password = passwordService.generatePassword(request);

        return ResponseEntity.ok(password);
    }
}