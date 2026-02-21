package com.RevPasswordManager.service;

import com.RevPasswordManager.dto.BackupDTO;
import com.RevPasswordManager.entities.PasswordEntry;
import com.RevPasswordManager.entities.User;
import com.RevPasswordManager.repository.PasswordEntryRepository;
import com.RevPasswordManager.repository.UserRepository;
import com.RevPasswordManager.util.PasswordSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import java.util.*;
import com.RevPasswordManager.dto.SecurityAuditResponse;

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

    public Page<PasswordEntry> searchPasswords(
            String search,
            String category,
            int page,
            int size,
            String sortBy,
            String direction,
            String username
    ) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return passwordEntryRepository.findAll(
                PasswordSpecification.filter(search, category, user.getId()),
                pageable
        );
    }

    public SecurityAuditResponse securityAudit(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<PasswordEntry> entries =
                passwordEntryRepository.findByUserId(user.getId());

        int weak = 0;
        int reused = 0;
        int old = 0;

        Map<String, Integer> passwordMap = new HashMap<>();

        for (PasswordEntry entry : entries) {

            String decrypted =
                    encryptionService.decrypt(entry.getEncryptedPassword());

            // Weak password check
            if (decrypted.length() < 8) {
                weak++;
            }

            passwordMap.put(decrypted,
                    passwordMap.getOrDefault(decrypted, 0) + 1);

            // Old password check (> 90 days)
            if (entry.getCreatedAt() != null &&
                    entry.getCreatedAt().isBefore(
                            java.time.LocalDateTime.now().minusDays(90))) {
                old++;
            }
        }

        reused = (int) passwordMap.values().stream()
                .filter(count -> count > 1)
                .count();

        return new SecurityAuditResponse(weak, reused, old);
    }

    public Map<String, Object> getDashboard(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<PasswordEntry> entries =
                passwordEntryRepository.findByUserId(user.getId());

        long total = entries.size();

        long weak = entries.stream()
                .filter(e -> encryptionService.decrypt(e.getEncryptedPassword()).length() < 8)
                .count();

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("totalPasswords", total);
        dashboard.put("weakPasswords", weak);

        return dashboard;
    }

    public List<PasswordEntry> getAll(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return passwordEntryRepository.findByUserId(user.getId());
    }

    // ================= EXPORT BACKUP =================
    public List<PasswordEntry> exportBackup(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return passwordEntryRepository.findByUserId(user.getId());
    }

    // ================= IMPORT BACKUP =================
    public String importBackup(String username, BackupDTO backupDTO) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        for (PasswordEntry entry : backupDTO.getEntries()) {
            entry.setUser(user);
            passwordEntryRepository.save(entry);
        }

        return "Backup imported successfully";
    }
}