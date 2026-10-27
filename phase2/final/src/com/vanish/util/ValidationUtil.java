package com.vanish.util;

import com.vanish.exception.InvalidInputException;

/**
 * Centralized utility class for string handling and input validation.
 * Enforces business constraints and throws InvalidInputException on failure.
 */
public final class ValidationUtil {

    public static final int MIN_USERNAME_LENGTH = 3;
    public static final int MAX_USERNAME_LENGTH = 20;
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_NOTE_LENGTH = 500;
    public static final int MIN_EXPIRY_MINUTES = 1;
    public static final int MAX_EXPIRY_MINUTES = 1440;
    public static final int MIN_VIEWS = 1;
    public static final int MAX_VIEWS = 5;

    // Special characters pattern: @#$%^&*()_+-=[]{};':"\|,.<>/?
    private static final String SPECIAL_CHARACTERS = "!@#$%^&*()_+-=[]{};':\"|,.<>/?";

    private ValidationUtil() {
        // Prevent instantiation
    }

    public static void validateUsername(String username) throws InvalidInputException {
        if (username == null || username.trim().isEmpty()) {
            throw new InvalidInputException("Username cannot be empty or whitespace only.");
        }
        String trimmed = username.trim();
        if (trimmed.length() < MIN_USERNAME_LENGTH) {
            throw new InvalidInputException("Username must be at least " + MIN_USERNAME_LENGTH + " characters long.");
        }
        if (trimmed.length() > MAX_USERNAME_LENGTH) {
            throw new InvalidInputException("Username cannot exceed " + MAX_USERNAME_LENGTH + " characters.");
        }
        if (username.contains(" ")) {
            throw new InvalidInputException("Username cannot contain spaces.");
        }
    }

    public static void validatePassword(String password) throws InvalidInputException {
        if (password == null || password.trim().isEmpty()) {
            throw new InvalidInputException("Password cannot be empty or whitespace only.");
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new InvalidInputException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters long.");
        }

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUpper = true;
            } else if (Character.isLowerCase(c)) {
                hasLower = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else if (SPECIAL_CHARACTERS.indexOf(c) >= 0) {
                hasSpecial = true;
            }
        }

        if (!hasUpper) {
            throw new InvalidInputException("Password must contain at least one uppercase letter (A-Z).");
        }
        if (!hasLower) {
            throw new InvalidInputException("Password must contain at least one lowercase letter (a-z).");
        }
        if (!hasDigit) {
            throw new InvalidInputException("Password must contain at least one number (0-9).");
        }
        if (!hasSpecial) {
            throw new InvalidInputException("Password must contain at least one special character (!@#$%^&*...).");
        }
    }

    public static void validatePasswordMatch(String password, String confirmPassword) throws InvalidInputException {
        if (confirmPassword == null || !password.equals(confirmPassword)) {
            throw new InvalidInputException("Password and confirm password do not match.");
        }
    }

    public static void validateNoteText(String text) throws InvalidInputException {
        if (text == null || text.trim().isEmpty()) {
            throw new InvalidInputException("Note text cannot be empty or whitespace only.");
        }
        if (text.trim().length() > MAX_NOTE_LENGTH) {
            throw new InvalidInputException("Note text cannot exceed " + MAX_NOTE_LENGTH + " characters.");
        }
    }

    public static void validateExpiry(int minutes) throws InvalidInputException {
        if (minutes < MIN_EXPIRY_MINUTES || minutes > MAX_EXPIRY_MINUTES) {
            throw new InvalidInputException("Expiry must be between " + MIN_EXPIRY_MINUTES + " and " + MAX_EXPIRY_MINUTES + " minutes.");
        }
    }

    public static void validateViews(int views) throws InvalidInputException {
        if (views < MIN_VIEWS || views > MAX_VIEWS) {
            throw new InvalidInputException("View count must be between " + MIN_VIEWS + " and " + MAX_VIEWS + ".");
        }
    }
}
