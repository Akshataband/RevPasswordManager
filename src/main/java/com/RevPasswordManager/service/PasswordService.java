package com.RevPasswordManager.service;

import com.RevPasswordManager.dto.*;
import com.RevPasswordManager.entities.PasswordEntry;
import com.RevPasswordManager.entities.User;
import com.RevPasswordManager.exception.CustomException;
import com.RevPasswordManager.repository.PasswordEntryRepository;
import com.RevPasswordManager.repository.UserRepository;
import com.RevPasswordManager.security.EncryptionService;
import com.RevPasswordManager.util.PasswordSpecification;
import com.RevPasswordManager.util.PasswordStrengthUtil;
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

    // ================= PRIVATE MAPPER =================
    private PasswordResponse mapToDto(PasswordEntry entry) {
        return PasswordResponse.builder()
                .id(entry.getId())
                .accountName(entry.getAccountName())
                .website(entry.getWebsite())
                .username(entry.getUsername())
                .category(entry.getCategory())
                .notes(entry.getNotes())
                .favorite(entry.isFavorite())
                .createdAt(entry.getCreatedAt())
                .updatedAt(entry.getUpdatedAt())
                .build();
    }

    // ================= VIEW PASSWORD =================
    public String viewPassword(Long entryId,
                               String masterPassword,
                               String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("User not found"));

        // 🔐 Check if account locked
        if (user.isAccountLocked()) {
            throw new CustomException("Account is locked due to multiple failed attempts");
        }

        // ❌ Wrong master password
        if (!passwordEncoder.matches(masterPassword, user.getMasterPassword())) {

            user.setFailedAttempts(user.getFailedAttempts() + 1);

            // Lock account after 5 attempts
            if (user.getFailedAttempts() >= 5) {
                user.setAccountLocked(true);
            }

            userRepository.save(user);

            throw new CustomException("Invalid master password");
        }

        // ✅ Correct password → reset attempts
        user.setFailedAttempts(0);
        userRepository.save(user);

        PasswordEntry entry = passwordEntryRepository.findById(entryId)
                .orElseThrow(() -> new CustomException("Password entry not found"));

        if (!entry.getUser().getId().equals(user.getId())) {
            throw new CustomException("Unauthorized access");
        }

        return encryptionService.decrypt(entry.getEncryptedPassword());
    }

    // ================= ADD PASSWORD =================
    public String addPassword(CreatePasswordRequest request, String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("User not found"));

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new CustomException("Password cannot be empty");
        }

        PasswordStrengthUtil.Strength strength =
                PasswordStrengthUtil.checkStrength(request.getPassword());

        if (strength == PasswordStrengthUtil.Strength.WEAK) {
            throw new CustomException("Password is too weak");
        }

        PasswordEntry entry = PasswordEntry.builder()
                .accountName(request.getAccountName())
                .website(request.getWebsite())
                .username(request.getUsername())
                .encryptedPassword(
                        encryptionService.encrypt(request.getPassword()))
                .category(request.getCategory())
                .notes(request.getNotes())
                .favorite(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .user(user)
                .build();

        passwordEntryRepository.save(entry);

        return "Password saved successfully (" + strength + ")";
    }
    // ================= GET ALL =================
    public List<PasswordResponse> getAll(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("User not found"));

        return passwordEntryRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    // ================= SEARCH =================
    public Page<PasswordResponse> searchPasswords(
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
        ).map(this::mapToDto);
    }

    // ================= DELETE =================
    public String deletePassword(Long id, String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("User not found"));

        PasswordEntry entry = passwordEntryRepository
                .findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new CustomException("Password not found or unauthorized"));

        passwordEntryRepository.delete(entry);

        return "Password deleted successfully";
    }
    // ================= ADD TO FAVORITE =================
    public String addToFavorite(Long id, String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("User not found"));

        PasswordEntry entry = passwordEntryRepository.findById(id)
                .orElseThrow(() -> new CustomException("Password not found"));

        if (!entry.getUser().getId().equals(user.getId())) {
            throw new CustomException("Unauthorized");
        }

        if (entry.isFavorite()) {
            return "Already marked as favorite";
        }

        entry.setFavorite(true);
        passwordEntryRepository.save(entry);

        return "Marked as favorite";
    }

    // ================= REMOVE FROM FAVORITE =================
    public String removeFromFavorite(Long id, String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("User not found"));

        PasswordEntry entry = passwordEntryRepository.findById(id)
                .orElseThrow(() -> new CustomException("Password not found"));

        if (!entry.getUser().getId().equals(user.getId())) {
            throw new CustomException("Unauthorized");
        }

        if (!entry.isFavorite()) {
            return "Password is not in favorites";
        }

        entry.setFavorite(false);
        passwordEntryRepository.save(entry);

        return "Removed from favorite";
    }
    // ================= GET FAVORITES =================
    public List<PasswordResponse> getFavorites(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("User not found"));

        return passwordEntryRepository
                .findByUserIdAndFavoriteTrue(user.getId())
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    // ================= SECURITY AUDIT =================
    public SecurityAuditResponse securityAudit(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("User not found"));

        List<PasswordEntry> entries =
                passwordEntryRepository.findByUserId(user.getId());

        int weak = 0;
        int reused = 0;
        int old = 0;

        Map<String, Integer> passwordMap = new HashMap<>();

        for (PasswordEntry entry : entries) {

            String decrypted = encryptionService.decrypt(entry.getEncryptedPassword());

            if (decrypted.length() < 8) weak++;

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
    public List<PasswordResponse> exportBackup(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("User not found"));

        return passwordEntryRepository.findByUserId(user.getId())
                .stream()
                .map(this::mapToDto)
                .toList();
    }
    public String importBackup(String username, BackupDTO backupDTO) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("User not found"));

        for (PasswordEntry entry : backupDTO.getEntries()) {

            PasswordEntry newEntry = PasswordEntry.builder()
                    .accountName(entry.getAccountName())
                    .website(entry.getWebsite())
                    .username(entry.getUsername())
                    .encryptedPassword(entry.getEncryptedPassword())
                    .category(entry.getCategory())
                    .notes(entry.getNotes())
                    .favorite(entry.isFavorite())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .user(user)   // 🔥 important
                    .build();

            passwordEntryRepository.save(newEntry);
        }

        return "Backup imported successfully";
    }

    public String generatePassword(PasswordGeneratorRequest request) {

        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String special = "!@#$%^&*";

        StringBuilder characters = new StringBuilder();

        if (request.isIncludeUppercase()) {
            characters.append(upper);
        }
        if (request.isIncludeLowercase()) {
            characters.append(lower);
        }
        if (request.isIncludeNumbers()) {
            characters.append(numbers);
        }
        if (request.isIncludeSpecial()) {
            characters.append(special);
        }

        if (characters.length() == 0) {
            throw new CustomException("At least one character type must be selected");
        }

        Random random = new Random();
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < request.getLength(); i++) {
            int index = random.nextInt(characters.length());
            password.append(characters.charAt(index));
        }

        return password.toString();
    }

    public String updatePassword(Long id,
                                 UpdatePasswordRequest request,
                                 String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("User not found"));

        PasswordEntry entry = passwordEntryRepository
                .findByIdAndUserId(id, user.getId())
                .orElseThrow(() ->
                        new CustomException("Password not found or unauthorized"));

        entry.setAccountName(request.getAccountName());
        entry.setWebsite(request.getWebsite());
        entry.setUsername(request.getUsername());
        entry.setCategory(request.getCategory());
        entry.setNotes(request.getNotes());
        entry.setFavorite(request.isFavorite());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {

            PasswordStrengthUtil.Strength strength =
                    PasswordStrengthUtil.checkStrength(request.getPassword());

            if (strength == PasswordStrengthUtil.Strength.WEAK) {
                throw new CustomException("Password is too weak");
            }

            entry.setEncryptedPassword(
                    encryptionService.encrypt(request.getPassword()));
        }

        entry.setUpdatedAt(LocalDateTime.now());

        passwordEntryRepository.save(entry);

        return "Password updated successfully";
    }

    public String changeMasterPassword(String username,
                                       String oldPassword,
                                       String newPassword) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("User not found"));

        if (user.isAccountLocked()) {
            throw new CustomException("Account is locked");
        }

        if (!passwordEncoder.matches(oldPassword, user.getMasterPassword())) {
            throw new CustomException("Old master password is incorrect");
        }

        user.setMasterPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(java.time.LocalDateTime.now());

        userRepository.save(user);

        return "Master password changed successfully";
    }

}