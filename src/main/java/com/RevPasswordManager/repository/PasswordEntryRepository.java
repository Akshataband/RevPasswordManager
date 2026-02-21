package com.RevPasswordManager.repository;

import com.RevPasswordManager.entities.PasswordEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface PasswordEntryRepository
        extends JpaRepository<PasswordEntry, Long>,
        JpaSpecificationExecutor<PasswordEntry> {

    // Basic listing
    List<PasswordEntry> findByUserId(Long userId);

    // Favorites
    List<PasswordEntry> findByUserIdAndFavoriteTrue(Long userId);
}