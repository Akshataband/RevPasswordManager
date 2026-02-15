package service;

import entities.PasswordEntry;
import entities.User;
import org.springframework.stereotype.Service;
import repository.PasswordEntryRepository;
import repository.UserRepository;

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

    public PasswordEntry addPassword(Long userId, PasswordEntry entry) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        entry.setEncryptedPassword(
                encryptionService.encrypt(entry.getEncryptedPassword())
        );

        entry.setUser(user);

        return passwordRepository.save(entry);
    }

    public List<PasswordEntry> getAll(Long userId) {
        return passwordRepository.findByUserId(userId);
    }

    public void delete(Long id) {
        passwordRepository.deleteById(id);
    }
}
