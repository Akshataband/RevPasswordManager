package com.RevPasswordManager.security;

import org.springframework.stereotype.Service;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Service
public class BackupEncryptionService {


        private static final String SECRET = "BackupSecretKey1";// 16 chars

    public String encrypt(String data) {
        try {
            SecretKeySpec key =
                    new SecretKeySpec(SECRET.getBytes(), "AES");

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);

            byte[] encrypted = cipher.doFinal(data.getBytes());

            return Base64.getEncoder().encodeToString(encrypted);

        } catch (Exception e) {
            throw new RuntimeException("Backup encryption failed");
        }
    }

    public String decrypt(String data) {
        try {
            SecretKeySpec key =
                    new SecretKeySpec(SECRET.getBytes(), "AES");

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);

            byte[] decoded =
                    Base64.getDecoder().decode(data);

            return new String(cipher.doFinal(decoded));

        } catch (Exception e) {
            throw new RuntimeException("Backup decryption failed");
        }
    }
}