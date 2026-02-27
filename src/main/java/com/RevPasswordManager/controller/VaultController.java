package com.RevPasswordManager.controller;

import com.RevPasswordManager.dto.*;
import com.RevPasswordManager.service.PasswordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/vault")
@RequiredArgsConstructor
public class VaultController {

    private final PasswordService passwordService;

    // ================= GET ALL =================
    @GetMapping
    public ResponseEntity<?> getAll(Authentication authentication) {

        String username = authentication.getName();

        return ResponseEntity.ok(
                passwordService.getAll(username)
        );
    }

    // ================= ADD PASSWORD =================
    @PostMapping
    public ResponseEntity<?> addPassword(
            @Valid @RequestBody CreatePasswordRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        passwordService.addPassword(
                                request,
                                authentication.getName()
                        )
                )
        );
    }

    // ================= VIEW PASSWORD =================
    @PostMapping("/{id}/view")
    public ResponseEntity<?> viewPassword(
            @PathVariable Long id,
            @RequestBody ViewPasswordRequest request,
            Authentication authentication) {

        String password = passwordService.viewPassword(
                id,
                request.getMasterPassword(),
                authentication.getName());

        return ResponseEntity.ok(
                Map.of("password", password)
        );
    }

    // ================= SEARCH =================
    @GetMapping("/search")
    public ResponseEntity<?> searchPasswords(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            Authentication authentication) {

        String username = authentication.getName();

        return ResponseEntity.ok(
                passwordService.searchPasswords(
                        search,
                        category,
                        page,
                        size,
                        sortBy,
                        direction,
                        username
                )
        );
    }

    // ================= FAVORITES =================
    @GetMapping("/favorites")
    public ResponseEntity<?> getFavorites(Authentication authentication) {

        String username = authentication.getName();

        return ResponseEntity.ok(
                passwordService.getFavorites(username)
        );
    }

    @PutMapping("/{id}/favorite")
    public ResponseEntity<?> addToFavorite(
            @PathVariable Long id,
            Authentication authentication) {

        String username = authentication.getName();

        return ResponseEntity.ok(
                passwordService.addToFavorite(id, username)
        );
    }

    @DeleteMapping("/{id}/favorite")
    public ResponseEntity<?> removeFromFavorite(
            @PathVariable Long id,
            Authentication authentication) {

        String username = authentication.getName();

        return ResponseEntity.ok(
                passwordService.removeFromFavorite(id, username)
        );
    }

    // ================= UPDATE =================
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePassword(
            @PathVariable Long id,
            @RequestBody UpdatePasswordRequest request,
            Authentication authentication) {

        String username = authentication.getName();

        return ResponseEntity.ok(
                passwordService.updatePassword(id, request, username)
        );
    }

    // ================= DELETE =================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePassword(
            @PathVariable Long id,
            Authentication authentication) {

        String username = authentication.getName();

        return ResponseEntity.ok(
                passwordService.deletePassword(id, username)
        );
    }

    // ================= SECURITY AUDIT =================
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
    @PostMapping("/save-generated")
    public ResponseEntity<?> saveGeneratedPassword(
            @RequestBody SaveGeneratedPasswordRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                passwordService.saveGeneratedPassword(
                        request,
                        authentication.getName()
                )
        );
    }
    // ================= GET BY ID =================
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(
                passwordService.getById(
                        id,
                        authentication.getName()
                )
        );
    }


}