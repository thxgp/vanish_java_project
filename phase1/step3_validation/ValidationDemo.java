// ============================================================
//  VANISH  -  One-Time Secret Note Sharing
//  PHASE 1  |  Step 3 : Basic Validation Logic (string handling)
//
//  Objective : check the input the user types before any object
//              is created - empty fields, length limits, spaces,
//              confirm-password match and expiry range.
//
//  In Phase 2 each of these "return false" branches becomes a
//  thrown InvalidInputException instead.
// ============================================================

public class ValidationDemo {

    static final int MIN_USERNAME_LENGTH = 3;
    static final int MAX_USERNAME_LENGTH = 20;
    static final int MIN_PASSWORD_LENGTH = 6;
    static final int MAX_NOTE_LENGTH = 500;
    static final int MIN_EXPIRY_MINUTES = 1;
    static final int MAX_EXPIRY_MINUTES = 1440;   // 24 hours
    static final int MAX_VIEWS = 5;

    // ---------- username ----------
    static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            System.out.println("  reason : username cannot be empty");
            return false;
        }
        if (username.trim().length() < MIN_USERNAME_LENGTH) {
            System.out.println("  reason : username must be at least " + MIN_USERNAME_LENGTH + " characters");
            return false;
        }
        if (username.trim().length() > MAX_USERNAME_LENGTH) {
            System.out.println("  reason : username cannot be longer than " + MAX_USERNAME_LENGTH + " characters");
            return false;
        }
        if (username.contains(" ")) {
            System.out.println("  reason : username cannot contain spaces");
            return false;
        }
        return true;
    }

    // ---------- password ----------
    static boolean isValidPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            System.out.println("  reason : password cannot be empty");
            return false;
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            System.out.println("  reason : password must be at least " + MIN_PASSWORD_LENGTH + " characters");
            return false;
        }
        return true;
    }

    static boolean isMatchingPassword(String password, String confirmPassword) {
        if (confirmPassword == null || !password.equals(confirmPassword)) {
            System.out.println("  reason : the two passwords do not match");
            return false;
        }
        return true;
    }

    // ---------- note text ----------
    static boolean isValidNoteText(String text) {
        if (text == null || text.trim().isEmpty()) {
            System.out.println("  reason : the note cannot be empty");
            return false;
        }
        if (text.trim().length() > MAX_NOTE_LENGTH) {
            System.out.println("  reason : the note cannot be longer than " + MAX_NOTE_LENGTH + " characters");
            return false;
        }
        return true;
    }

    // ---------- expiry ----------
    static boolean isValidExpiry(int minutes) {
        if (minutes < MIN_EXPIRY_MINUTES || minutes > MAX_EXPIRY_MINUTES) {
            System.out.println("  reason : expiry must be between " + MIN_EXPIRY_MINUTES
                    + " and " + MAX_EXPIRY_MINUTES + " minutes");
            return false;
        }
        return true;
    }

    // ---------- views ----------
    static boolean isValidViews(int views) {
        if (views < 1 || views > MAX_VIEWS) {
            System.out.println("  reason : views must be between 1 and " + MAX_VIEWS);
            return false;
        }
        return true;
    }

    // ---------- small helpers so main() stays readable ----------
    static void verdict(boolean valid) {
        System.out.println("  result : " + (valid ? "ACCEPTED" : "REJECTED"));
        System.out.println();
    }

    static void checkUsername(String username) {
        System.out.println("username [" + username + "]");
        verdict(isValidUsername(username));
    }

    static void checkPassword(String password) {
        System.out.println("password [" + password + "]");
        verdict(isValidPassword(password));
    }

    static void checkConfirm(String password, String confirmPassword) {
        System.out.println("password [" + password + "] confirm [" + confirmPassword + "]");
        verdict(isValidPassword(password) && isMatchingPassword(password, confirmPassword));
    }

    static void checkNoteText(String text) {
        int shown = Math.min(text == null ? 0 : text.length(), 30);
        String preview = text == null ? "null" : text.substring(0, shown);
        System.out.println("note [" + preview + "] length=" + (text == null ? 0 : text.length()));
        verdict(isValidNoteText(text));
    }

    static void checkExpiry(int minutes) {
        System.out.println("expiry [" + minutes + " minutes]");
        verdict(isValidExpiry(minutes));
    }

    static void checkViews(int views) {
        System.out.println("views [" + views + "]");
        verdict(isValidViews(views));
    }

    public static void main(String[] args) {

        System.out.println("===== USERNAME VALIDATION =====");
        checkUsername("");
        checkUsername("   ");
        checkUsername("ab");
        checkUsername("win dro");
        checkUsername("student");

        System.out.println("===== PASSWORD VALIDATION =====");
        checkPassword("");
        checkPassword("123");
        checkPassword("java123");

        System.out.println("===== CONFIRM PASSWORD =====");
        checkConfirm("java123", "java1234");
        checkConfirm("java123", "java123");

        System.out.println("===== NOTE TEXT VALIDATION =====");
        checkNoteText("");
        checkNoteText("a".repeat(501));
        checkNoteText("atm pin 4821");

        System.out.println("===== EXPIRY VALIDATION =====");
        checkExpiry(0);
        checkExpiry(5000);
        checkExpiry(10);

        System.out.println("===== VIEWS VALIDATION =====");
        checkViews(0);
        checkViews(9);
        checkViews(1);
    }
}
