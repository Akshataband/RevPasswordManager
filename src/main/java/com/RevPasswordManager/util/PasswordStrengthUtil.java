package com.RevPasswordManager.util;

public class PasswordStrengthUtil {

    public enum Strength {
        WEAK,
        MEDIUM,
        STRONG
    }

    public static Strength checkStrength(String password) {

        if (password == null || password.length() < 8) {
            return Strength.WEAK;
        }

        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasNumber = password.matches(".*[0-9].*");
        boolean hasSpecial = password.matches(".*[^a-zA-Z0-9].*");

        int score = 0;

        if (hasUpper) score++;
        if (hasLower) score++;
        if (hasNumber) score++;
        if (hasSpecial) score++;

        if (score <= 2) return Strength.WEAK;
        if (score == 3) return Strength.MEDIUM;

        return Strength.STRONG;
    }
}