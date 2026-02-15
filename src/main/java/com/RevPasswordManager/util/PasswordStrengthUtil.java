package com.RevPasswordManager.util;
public class PasswordStrengthUtil {

    public static boolean isWeak(String password) {

        if (password.length() < 8) return true;

        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasNumber = password.matches(".*[0-9].*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()].*");

        int score = 0;

        if (hasUpper) score++;
        if (hasLower) score++;
        if (hasNumber) score++;
        if (hasSpecial) score++;

        return score < 3;
    }
}
