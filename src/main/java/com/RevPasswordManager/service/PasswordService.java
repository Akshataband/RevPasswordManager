package com.RevPasswordManager.service;

import com.RevPasswordManager.dto.*;
import com.RevPasswordManager.entities.PasswordEntry;
import com.RevPasswordManager.entities.User;
import com.RevPasswordManager.repository.PasswordEntryRepository;
import com.RevPasswordManager.repository.UserRepository;
import com.RevPasswordManager.util.PasswordStrengthUtil;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class PasswordService {

    private final PasswordEntryRepository passwordRepository;
    private final UserRepository userRepository;
    private final EncryptionService encryptionService;
    private final PasswordEncoder passwordEncoder;

    public PasswordService(PasswordEntryRepository passwordRepository,
                           UserRepository userRepository,
                           EncryptionService encryptionService,
                           PasswordEncoder passwordEncoder) {

        this.passwordRepository = passwordRepository;
        this.userRepository = userRepository;
        this.encryptionService = encryptionService;
        this.passwordEncoder = passwordEncoder;
    }

    // ========================= ADD PASSWORD =========================

    public PasswordEntry addPassword(PasswordEntry entry) {

        User user = getCurrentUser();

        entry.setEncryptedPassword(
                encryptionService.encrypt(entry.getEncryptedPassword())
        );

        entry.setUser(user);

        return passwordRepository.save(entry);
    }

    // ========================= GET ALL =========================

    public List<PasswordEntry> getAll() {

        User user = getCurrentUser();

        return passwordRepository.findByUserId(user.getId());
    }

    // ========================= DELETE =========================

    public void delete(Long id) {

        User user = getCurrentUser();

        PasswordEntry entry = passwordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Password not found"));

        if (!entry.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access");
        }

        passwordRepository.delete(entry);
    }

    // ========================= DASHBOARD =========================

    public DashboardResponse getDashboard() {

        User user = getCurrentUser();

        List<PasswordEntry> list =
                passwordRepository.findByUserId(user.getId());

        int total = list.size();
        int weak = 0;
        int favorite = 0;

        for (PasswordEntry entry : list) {

            String decrypted =
                    encryptionService.decrypt(entry.getEncryptedPassword());

            if (PasswordStrengthUtil.isWeak(decrypted)) {
                weak++;
            }

            if (entry.isFavorite()) {
                favorite++;
            }
        }

        return new DashboardResponse(total, weak, favorite);
    }

    // ========================= EXPORT BACKUP =========================

    public BackupDTO exportBackup() {

        User user = getCurrentUser();
        List<PasswordEntry> entries =
                passwordRepository.findByUserId(user.getId());

        List<PasswordBackupItem> items = entries.stream().map(entry -> {

            PasswordBackupItem item = new PasswordBackupItem();
            item.setAccountName(entry.getAccountName());
            item.setWebsite(entry.getWebsite());
            item.setUsername(entry.getUsername());
            item.setEncryptedPassword(entry.getEncryptedPassword());
            item.setCategory(entry.getCategory());
            item.setNotes(entry.getNotes());
            item.setFavorite(entry.isFavorite());

            return item;

        }).toList();

        BackupDTO backup = new BackupDTO();
        backup.setItems(items);

        return backup;
    }

    // ========================= IMPORT BACKUP =========================

    public void importBackup(BackupDTO backup) {

        User user = getCurrentUser();

        for (PasswordBackupItem item : backup.getItems()) {

            PasswordEntry entry = new PasswordEntry();
            entry.setAccountName(item.getAccountName());
            entry.setWebsite(item.getWebsite());
            entry.setUsername(item.getUsername());
            entry.setEncryptedPassword(item.getEncryptedPassword());
            entry.setCategory(item.getCategory());
            entry.setNotes(item.getNotes());
            entry.setFavorite(item.isFavorite());
            entry.setUser(user);

            passwordRepository.save(entry);
        }
    }

    // ========================= SECURITY AUDIT =========================

    public SecurityAuditResponse securityAudit() {

        User user = getCurrentUser();

        List<PasswordEntry> entries =
                passwordRepository.findByUserId(user.getId());

        int weak = 0;
        int reused = 0;
        int old = 0;

        Map<String, Integer> map = new HashMap<>();

        for (PasswordEntry entry : entries) {

            String decrypted =
                    encryptionService.decrypt(entry.getEncryptedPassword());

            if (PasswordStrengthUtil.isWeak(decrypted)) {
                weak++;
            }

            map.put(decrypted,
                    map.getOrDefault(decrypted, 0) + 1);

            if (entry.getCreatedAt() != null &&
                    entry.getCreatedAt()
                            .isBefore(LocalDateTime.now().minusDays(90))) {
                old++;
            }
        }

        for (Integer count : map.values()) {
            if (count > 1) reused++;
        }

        SecurityAuditResponse response = new SecurityAuditResponse();
        response.setWeakPasswords(weak);
        response.setReusedPasswords(reused);
        response.setOldPasswords(old);

        return response;
    }

    // ========================= VIEW PASSWORD =========================

    public String viewPassword(Long id, String masterPassword) {

        User user = getCurrentUser();

        if (!passwordEncoder.matches(masterPassword, user.getPassword())) {
            throw new RuntimeException("Invalid master password");
        }

        PasswordEntry entry = passwordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Password not found"));

        if (!entry.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access");
        }

        return encryptionService.decrypt(entry.getEncryptedPassword());
    }

    // ========================= CURRENT USER =========================

    private User getCurrentUser() {

        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}