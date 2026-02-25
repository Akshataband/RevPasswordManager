package com.RevPasswordManager.service;

import com.RevPasswordManager.dto.*;
import com.RevPasswordManager.entities.PasswordEntry;
import com.RevPasswordManager.entities.User;
import com.RevPasswordManager.exception.CustomException;
import com.RevPasswordManager.repository.PasswordEntryRepository;
import com.RevPasswordManager.repository.UserRepository;
import com.RevPasswordManager.security.BackupEncryptionService;
import com.RevPasswordManager.security.EncryptionService;
import com.RevPasswordManager.util.PasswordSpecification;
import com.RevPasswordManager.util.PasswordStrengthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PasswordService {

    private final PasswordEntryRepository passwordEntryRepository;
    private final UserRepository userRepository;
    private final EncryptionService encryptionService;
    private final PasswordEncoder passwordEncoder;
    private final BackupEncryptionService backupEncryptionService;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;
    // ================= PRIVATE MAPPER =================
    private PasswordResponse mapToDto(PasswordEntry entry) {

        String decrypted = encryptionService.decrypt(
                entry.getEncryptedPassword()
        );

        PasswordStrengthUtil.Strength strength =
                PasswordStrengthUtil.checkStrength(decrypted);

        return PasswordResponse.builder()
                .id(entry.getId())
                .accountName(entry.getAccountName())
                .website(entry.getWebsite())
                .username(entry.getUsername())
                .category(entry.getCategory())
                .notes(entry.getNotes())
                .favorite(entry.isFavorite())
                .strength(strength.name())
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
    public SecurityAuditResponse securityAudit(String username, String masterPassword) {
        validateMasterPassword(username, masterPassword);
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

// 🔥 improved reused calculation
        for (int count : passwordMap.values()) {
            if (count > 1) {
                reused += count;
            }
        }

// 🔥 improved reused calculation
        for (int count : passwordMap.values()) {
            if (count > 1) {
                reused += count;
            }
        }
        return new SecurityAuditResponse(weak, reused, old);
    }

    // ================= DASHBOARD =================
    public DashboardResponse getDashboardSummary(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<PasswordEntry> entries =
                passwordEntryRepository.findByUserId(user.getId());

        long total = entries.size();

        long weak = entries.stream()
                .filter(entry -> isWeak(entry.getEncryptedPassword()))
                .count();

        long reused = entries.stream()
                .collect(Collectors.groupingBy(
                        PasswordEntry::getEncryptedPassword,
                        Collectors.counting()
                ))
                .values()
                .stream()
                .filter(count -> count > 1)
                .count();

        List<RecentPasswordDTO> recent = entries.stream()
                .sorted(Comparator.comparing(
                        PasswordEntry::getCreatedAt).reversed())
                .limit(5)
                .map(entry -> RecentPasswordDTO.builder()
                        .id(entry.getId())
                        .accountName(entry.getAccountName())
                        .category(entry.getCategory())
                        .createdAt(entry.getCreatedAt())
                        .build())
                .toList();

        return DashboardResponse.builder()
                .totalPasswords(total)
                .weakPasswords(weak)
                .reusedPasswords(reused)
                .recentPasswords(recent)
                .build();
    }

    //    =====================================================================================
    public String exportBackup(String username, String masterPassword) {

        validateMasterPassword(username, masterPassword); // 🔐 secure

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("User not found"));

        List<PasswordEntry> entries =
                passwordEntryRepository.findByUserId(user.getId());

        try {
            String json = objectMapper.writeValueAsString(entries);
            return backupEncryptionService.encrypt(json);

        } catch (Exception e) {
            throw new CustomException("Backup export failed");
        }
    }
//    ===================================================================================
public String importBackup(String username,
                           String masterPassword,
                           String encryptedBackup) {

    validateMasterPassword(username, masterPassword); // 🔐 secure

    User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new CustomException("User not found"));

    try {

        String json = backupEncryptionService.decrypt(encryptedBackup);

        PasswordEntry[] entries =
                objectMapper.readValue(json, PasswordEntry[].class);

        for (PasswordEntry entry : entries) {

            entry.setId(null);
            entry.setUser(user);
            entry.setCreatedAt(LocalDateTime.now());
            entry.setUpdatedAt(LocalDateTime.now());

            passwordEntryRepository.save(entry);
        }

        return "Backup imported successfully";

    } catch (Exception e) {
        throw new CustomException("Invalid backup file");
    }
}
//===========================================================================================
    public List<GeneratedPasswordResponse> generatePasswords(
            PasswordGeneratorRequest request) {

        if (request.getLength() < 8 || request.getLength() > 64) {
            throw new CustomException("Password length must be between 8 and 64");
        }

        if (request.getCount() <= 0 || request.getCount() > 10) {
            throw new CustomException("Count must be between 1 and 10");
        }

        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String special = "!@#$%^&*";

        String similar = "0O1l";

        StringBuilder characters = new StringBuilder();

        if (request.isIncludeUppercase()) characters.append(upper);
        if (request.isIncludeLowercase()) characters.append(lower);
        if (request.isIncludeNumbers()) characters.append(numbers);
        if (request.isIncludeSpecial()) characters.append(special);

        if (characters.length() == 0) {
            throw new CustomException("Select at least one character type");
        }

        if (request.isExcludeSimilar()) {
            for (char c : similar.toCharArray()) {
                int index = characters.indexOf(String.valueOf(c));
                if (index != -1) {
                    characters.deleteCharAt(index);
                }
            }
        }

        Random random = new Random();
        List<GeneratedPasswordResponse> result = new ArrayList<>();

        for (int i = 0; i < request.getCount(); i++) {

            StringBuilder password = new StringBuilder();

            for (int j = 0; j < request.getLength(); j++) {
                int index = random.nextInt(characters.length());
                password.append(characters.charAt(index));
            }

            String generated = password.toString();

            PasswordStrengthUtil.Strength strength =
                    PasswordStrengthUtil.checkStrength(generated);

            result.add(
                    new GeneratedPasswordResponse(
                            generated,
                            strength.name()
                    )
            );
        }

        return result;
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

    private boolean isWeak(String encryptedPassword) {

        // Since encrypted, we cannot check real strength here
        // For now assume short encrypted string = weak
        return encryptedPassword.length() < 30;
    }

    public String saveGeneratedPassword(
            SaveGeneratedPasswordRequest request,
            String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("User not found"));

        if (request.getGeneratedPassword() == null
                || request.getGeneratedPassword().isBlank()) {
            throw new CustomException("Generated password is required");
        }

        PasswordStrengthUtil.Strength strength =
                PasswordStrengthUtil.checkStrength(
                        request.getGeneratedPassword());

        PasswordEntry entry = PasswordEntry.builder()
                .accountName(request.getAccountName())
                .website(request.getWebsite())
                .username(request.getUsername())
                .encryptedPassword(
                        encryptionService.encrypt(
                                request.getGeneratedPassword()))
                .category(request.getCategory())
                .notes(request.getNotes())
                .favorite(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .user(user)
                .build();

        passwordEntryRepository.save(entry);

        return "Generated password saved successfully (" + strength + ")";
    }

    public SecurityAlertResponse getSecurityAlerts(
            String username,
            String masterPassword) {

        validateMasterPassword(username, masterPassword); // 🔐 secure

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("User not found"));

        List<PasswordEntry> entries =
                passwordEntryRepository.findByUserId(user.getId());

        List<String> alerts = new ArrayList<>();
        Map<String, Integer> passwordMap = new HashMap<>();

        for (PasswordEntry entry : entries) {

            String decrypted;

            try {
                decrypted = encryptionService.decrypt(entry.getEncryptedPassword());
            } catch (Exception e) {
                alerts.add("Password could not be analyzed for account: "
                        + entry.getAccountName());
                continue;
            }

            // Weak password check
            if (PasswordStrengthUtil.checkStrength(decrypted)
                    == PasswordStrengthUtil.Strength.WEAK) {

                alerts.add("Weak password detected for account: "
                        + entry.getAccountName());
            }

            passwordMap.put(decrypted,
                    passwordMap.getOrDefault(decrypted, 0) + 1);

            // Old password check (90 days)
            if (entry.getCreatedAt() != null &&
                    entry.getCreatedAt().isBefore(
                            LocalDateTime.now().minusDays(90))) {

                alerts.add("Old password (90+ days) for account: "
                        + entry.getAccountName());
            }
        }

        passwordMap.forEach((password, count) -> {
            if (count > 1) {
                alerts.add("Password reused in " + count + " accounts");
            }
        });

        if (alerts.isEmpty()) {
            alerts.add("No security risks detected");
        }

        return new SecurityAlertResponse(alerts);
    }

//    ===================================================================================
    private void validateMasterPassword(String username, String masterPassword) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("User not found"));

        if (!passwordEncoder.matches(masterPassword, user.getMasterPassword())) {
            throw new CustomException("Invalid master password");
        }
    }


    public PasswordStrengthResponse checkPasswordStrength(String password) {

        if (password == null || password.isBlank()) {
            throw new CustomException("Password cannot be empty");
        }

        PasswordStrengthUtil.Strength strength =
                PasswordStrengthUtil.checkStrength(password);

        return new PasswordStrengthResponse(strength.name());
    }


}