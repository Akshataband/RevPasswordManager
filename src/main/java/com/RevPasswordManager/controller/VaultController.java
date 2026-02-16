package com.RevPasswordManager.controller;

import com.RevPasswordManager.entities.PasswordEntry;
import com.RevPasswordManager.service.PasswordService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vault")
public class VaultController {

    private final PasswordService passwordService;

    public VaultController(PasswordService passwordService) {
        this.passwordService = passwordService;
    }

    @PostMapping
    public PasswordEntry add(@RequestBody PasswordEntry entry) {
        return passwordService.addPassword(entry);
    }

    @GetMapping
    public List<PasswordEntry> getAll() {
        return passwordService.getAll();
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        passwordService.delete(id);
        return "Deleted successfully";
    }
}
