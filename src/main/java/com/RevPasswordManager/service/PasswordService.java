package com.RevPasswordManager.service;

import com.RevPasswordManager.dto.BackupDTO;
import com.RevPasswordManager.dto.SecurityAuditResponse;
import com.RevPasswordManager.entities.PasswordEntry;
import com.RevPasswordManager.entities.User;
import com.RevPasswordManager.exception.CustomException;
import com.RevPasswordManager.repository.PasswordEntryRepository;
import com.RevPasswordManager.repository.UserRepository;
import com.RevPasswordManager.util.PasswordSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PasswordService {

    private final PasswordEntryRepository passwordEntryRepository;
    private final UserRepository userRepository;
    private final EncryptionService encryptionService;
    private final PasswordEncoder passwordEncoder;

    // ================= VIEW PASSWORD =================
    public String viewPassword(Long entryId, String masterPassword, String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("User not found"));

        if (!passwordEncoder.matches(masterPassword, user.getMasterPassword())) {
            throw new CustomException("Invalid master password");
        }

        PasswordEntry entry = passwordEntryRepository.findById(entryId)
                .orElseThrow(() -> new CustomException("Password entry not found"));

        if (!entry.getUser().getId().equals(user.getId())) {
            throw new CustomException("Unauthorized access");
        }

        return encryptionService.decrypt(entry.getEncryptedPassword());
    }

    // ================= SEARCH =================
    public Page<PasswordEntry> searchPasswords(
            String search,
            String category,
            int page,
            int size,
            String sortBy,
            String direction,
            String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("User not found"));

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return passwordEntryRepository.findAll(
                PasswordSpecification.filter(search, category, user.getId()),
                pageable
        );
    }

    // ================= SECURITY AUDIT =================
    public SecurityAuditResponse securityAudit(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("User not found"));

        List<PasswordEntry> entries =
                passwordEntryRepository.findByUserId(user.getId());

        if (entries == null || entries.isEmpty()) {
            return new SecurityAuditResponse(0, 0, 0);
        }

        int weak = 0;
        int reused = 0;
        int old = 0;

        Map<String, Integer> passwordMap = new HashMap<>();

        for (PasswordEntry entry : entries) {

            if (entry.getEncryptedPassword() == null) {
                continue;
            }

            String decrypted;

            try {
                decrypted = encryptionService.decrypt(entry.getEncryptedPassword());
            } catch (Exception e) {
                continue; // skip corrupted entry
            }

            if (decrypted.length() < 8) {
                weak++;
            }

            passwordMap.put(decrypted,
                    passwordMap.getOrDefault(decrypted, 0) + 1);

            if (entry.getCreatedAt() != null &&
                    entry.getCreatedAt().isBefore(
                            LocalDateTime.now().minusDays(90))) {
                old++;
            }
        }

        reused = (int) passwordMap.values().stream()
                .filter(count -> count > 1)
                .count();

        return new SecurityAuditResponse(weak, reused, old);
    }
    // ================= DASHBOARD =================
    public Map<String, Object> getDashboard(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("User not found"));

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

    // ================= GET ALL =================
    public List<PasswordEntry> getAll(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("User not found"));

        return passwordEntryRepository.findByUserId(user.getId());
    }

    // ================= EXPORT BACKUP =================
    public List<PasswordEntry> exportBackup(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("User not found"));

        return passwordEntryRepository.findByUserId(user.getId());
    }

    // ================= IMPORT BACKUP =================
    public String importBackup(String username, BackupDTO backupDTO) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("User not found"));

        for (PasswordEntry entry : backupDTO.getEntries()) {
            entry.setUser(user);
            passwordEntryRepository.save(entry);
        }

        return "Backup imported successfully";
    }

    // ================= DELETE =================
    public String deletePassword(Long id, String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("User not found"));

        PasswordEntry entry = passwordEntryRepository.findById(id)
                .orElseThrow(() -> new CustomException("Password not found"));

        if (!entry.getUser().getId().equals(user.getId())) {
            throw new CustomException("Unauthorized");
        }

        passwordEntryRepository.delete(entry);

        return "Password deleted successfully";
    }

    // ================= TOGGLE FAVORITE =================
    public String toggleFavorite(Long id, String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("User not found"));

        PasswordEntry entry = passwordEntryRepository.findById(id)
                .orElseThrow(() -> new CustomException("Password not found"));

        if (!entry.getUser().getId().equals(user.getId())) {
            throw new CustomException("Unauthorized");
        }

        entry.setFavorite(!entry.isFavorite());
        passwordEntryRepository.save(entry);

        return entry.isFavorite()
                ? "Marked as favorite"
                : "Removed from favorite";
    }

    // ================= GET FAVORITES =================
    public List<PasswordEntry> getFavorites(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("User not found"));

        return passwordEntryRepository
                .findByUserIdAndFavoriteTrue(user.getId());
    }
}