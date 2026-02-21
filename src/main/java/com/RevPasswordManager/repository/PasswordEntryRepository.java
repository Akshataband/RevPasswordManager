package com.RevPasswordManager.repository;

import com.RevPasswordManager.entities.PasswordEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PasswordEntryRepository
        extends JpaRepository<PasswordEntry, Long> {

    // ================= BASIC =================

    List<PasswordEntry> findByUserId(Long userId);

    // ================= SEARCH =================

    List<PasswordEntry> findByUserIdAndAccountNameContainingIgnoreCase(
            Long userId, String accountName);

    List<PasswordEntry> findByUserIdAndWebsiteContainingIgnoreCase(
            Long userId, String website);

    List<PasswordEntry> findByUserIdAndUsernameContainingIgnoreCase(
            Long userId, String username);

    // ================= FILTER =================

    List<PasswordEntry> findByUserIdAndCategory(
            Long userId, String category);

    // ================= SORT =================

    List<PasswordEntry> findByUserIdOrderByAccountNameAsc(Long userId);

    List<PasswordEntry> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<PasswordEntry> findByUserIdOrderByUpdatedAtDesc(Long userId);

    // ================= FAVORITES =================

    List<PasswordEntry> findByUserIdAndFavoriteTrue(Long userId);
}