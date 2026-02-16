package com.RevPasswordManager.service;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;

import com.RevPasswordManager.dto.BackupDTO;
import com.RevPasswordManager.dto.DashboardResponse;
import com.RevPasswordManager.dto.PasswordBackupItem;
import com.RevPasswordManager.dto.SecurityAuditResponse;
import com.RevPasswordManager.entities.PasswordEntry;
import com.RevPasswordManager.entities.User;
import com.RevPasswordManager.util.PasswordStrengthUtil;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.RevPasswordManager.repository.PasswordEntryRepository;
import com.RevPasswordManager.repository.UserRepository;

import java.util.List;

@Service
public class PasswordService {

    private final PasswordEntryRepository passwordRepository;
    private final UserRepository userRepository;
    private final EncryptionService encryptionService;

    public PasswordService(PasswordEntryRepository passwordRepository,
                           UserRepository userRepository,
                           EncryptionService encryptionService) {
        this.passwordRepository = passwordRepository;
        this.userRepository = userRepository;
        this.encryptionService = encryptionService;
    }

    public PasswordEntry addPassword(PasswordEntry entry) {

        User user = getCurrentUser();

        entry.setEncryptedPassword(
                encryptionService.encrypt(entry.getEncryptedPassword())
        );

        entry.setUser(user);

        return passwordRepository.save(entry);
    }


    public List<PasswordEntry> getAll() {

        User user = getCurrentUser();

        return passwordRepository.findByUserId(user.getId());
    }


    public void delete(Long id) {
        passwordRepository.deleteById(id);
    }
    public DashboardResponse getDashboard(Long userId) {

        List<PasswordEntry> list = passwordRepository.findByUserId(userId);

        int total = list.size();
        int weak = 0;
        int favorite = 0;

        for (PasswordEntry entry : list) {

            String decrypted = encryptionService.decrypt(entry.getEncryptedPassword());

            if (PasswordStrengthUtil.isWeak(decrypted)) {
                weak++;
            }

            if (entry.isFavorite())
            {
                favorite++;
            }
        }

        return new DashboardResponse(total, weak, favorite);
    }

    public BackupDTO exportBackup(Long userId) {

        List<PasswordEntry> entries =
                passwordRepository.findByUserId(userId);

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

    public void importBackup(Long userId, BackupDTO backup) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

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
    public SecurityAuditResponse securityAudit(Long userId) {

        List<PasswordEntry> entries =
                passwordRepository.findByUserId(userId);

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

    private User getCurrentUser() {

        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }


}
