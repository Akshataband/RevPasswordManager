package com.RevPasswordManager.controller;

import com.RevPasswordManager.dto.ViewPasswordRequest;
import com.RevPasswordManager.service.PasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vault")
@RequiredArgsConstructor
public class VaultController {

    private final PasswordService passwordService;

    // ================= GET ALL =================
    @GetMapping
    public ResponseEntity<?> getAll(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User userDetails) {

        return ResponseEntity.ok(
                passwordService.getAll(userDetails.getUsername())
        );
    }

    // ================= VIEW PASSWORD =================
    @PostMapping("/view")
    public ResponseEntity<?> viewPassword(
            @RequestBody ViewPasswordRequest request,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User userDetails) {

        return ResponseEntity.ok(
                passwordService.viewPassword(
                        request.getEntryId(),
                        request.getMasterPassword(),
                        userDetails.getUsername()
                )
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
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User userDetails) {

        return ResponseEntity.ok(
                passwordService.searchPasswords(
                        search,
                        category,
                        page,
                        size,
                        sortBy,
                        direction,
                        userDetails.getUsername()
                )
        );
    }

    // ================= SECURITY AUDIT =================
    @GetMapping("/audit")
    public ResponseEntity<?> audit(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User userDetails) {

        return ResponseEntity.ok(
                passwordService.securityAudit(userDetails.getUsername())
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePassword(
            @PathVariable Long id,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User userDetails) {

        return ResponseEntity.ok(
                passwordService.deletePassword(id, userDetails.getUsername())
        );
    }

    @PutMapping("/{id}/favorite")
    public ResponseEntity<?> toggleFavorite(
            @PathVariable Long id,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User userDetails) {

        return ResponseEntity.ok(
                passwordService.toggleFavorite(id, userDetails.getUsername())
        );
    }
    @GetMapping("/favorites")
    public ResponseEntity<?> getFavorites(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User userDetails) {

        return ResponseEntity.ok(
                passwordService.getFavorites(userDetails.getUsername())
        );
    }
}