package com.RevPasswordManager.service;

import com.RevPasswordManager.entities.PasswordEntry;
import com.RevPasswordManager.entities.User;
import com.RevPasswordManager.repository.PasswordEntryRepository;
import com.RevPasswordManager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasswordService {

    private final PasswordEntryRepository passwordEntryRepository;
    private final UserRepository userRepository;
    private final EncryptionService encryptionService;
    private final PasswordEncoder passwordEncoder;

    // 🔐 View password after master password verification
    public String viewPassword(Long entryId, String masterPassword, String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify master password
        if (!passwordEncoder.matches(masterPassword, user.getMasterPassword())) {
            throw new RuntimeException("Invalid master password");
        }

        PasswordEntry entry = passwordEntryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Password entry not found"));

        // Prevent accessing other user's password
        if (!entry.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access");
        }

        return encryptionService.decrypt(entry.getEncryptedPassword());
    }
}