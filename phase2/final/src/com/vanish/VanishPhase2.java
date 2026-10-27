package com.vanish;

import com.vanish.exception.AuthenticationException;
import com.vanish.exception.InvalidInputException;
import com.vanish.exception.NoteBurnedException;
import com.vanish.exception.NoteExpiredException;
import com.vanish.exception.NoteNotFoundException;
import com.vanish.exception.NotePendingException;
import com.vanish.exception.UserAlreadyExistsException;
import com.vanish.model.Note;
import com.vanish.model.User;
import com.vanish.service.NoteService;
import com.vanish.service.UserService;
import com.vanish.service.impl.NoteServiceImpl;
import com.vanish.service.impl.UserServiceImpl;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ============================================================
 *  VANISH  -  One-Time Secret Note Sharing
 *  PHASE 2  |  Structured Programming, Reliability & Cryptography (CLO2)
 *
 *  Deliverable demonstrating:
 *    - Modular package organization (model, service, store, exception, util)
 *    - Interface-driven architecture (UserService, NoteService)
 *    - Strong exception handling (try-catch for domain exceptions)
 *    - Strong password validation policy (length, case, digit, special char)
 *    - Salted SHA-256 password hashing (different salts for identical passwords)
 *    - AES-256-GCM authenticated encryption for secret note text in memory
 *    - Thread-safe Singleton MemoryStore (ConcurrentHashMap)
 *    - Cryptographically secure URL-safe tokens (SecureRandom)
 * ============================================================
 */
public class VanishPhase2 {

    private static final UserService userService = new UserServiceImpl();
    private static final NoteService noteService = new NoteServiceImpl();

    private static void heading(String title) {
        System.out.println();
        System.out.println("===== " + title + " =====");
    }

    private static User registerUser(String username, String password, String confirmPassword) {
        System.out.println("register : username=[" + username + "] password=[" + password
                + "] confirm=[" + confirmPassword + "]");
        try {
            User user = userService.register(username, password, confirmPassword);
            System.out.println("  [SUCCESS] Account created for: " + user.getUsername());
            return user;
        } catch (InvalidInputException e) {
            System.out.println("  [REJECTED - InvalidInputException] " + e.getMessage());
            return null;
        } catch (UserAlreadyExistsException e) {
            System.out.println("  [REJECTED - UserAlreadyExistsException] " + e.getMessage());
            return null;
        }
    }

    private static User loginUser(String username, String password) {
        System.out.println("login : username=[" + username + "] password=[" + password + "]");
        try {
            User user = userService.authenticate(username, password);
            System.out.println("  [SUCCESS] Welcome back, " + user.getUsername() + "!");
            return user;
        } catch (InvalidInputException e) {
            System.out.println("  [FAILED - InvalidInputException] " + e.getMessage());
            return null;
        } catch (AuthenticationException e) {
            System.out.println("  [FAILED - AuthenticationException] " + e.getMessage());
            return null;
        }
    }

    private static Note composeNote(User owner, String text, int expiryMinutes, int views) {
        System.out.println("compose : expiry=" + expiryMinutes + "min views=" + views
                + " length=" + (text == null ? 0 : text.length()));
        try {
            Note note = noteService.createNote(owner.getUsername(), text, expiryMinutes, views);
            note.approve(); // console phase has no admin; web (phase 4) routes approval through the admin
            System.out.println("  [SUCCESS] Note #" + note.getId() + " created with token: " + note.getToken());
            System.out.println("  Link: " + note.shareLink());
            System.out.println("  AES-256 In-Memory Ciphertext: " + note.getCiphertextPreview());
            return note;
        } catch (InvalidInputException e) {
            System.out.println("  [REJECTED - InvalidInputException] " + e.getMessage());
            return null;
        }
    }

    private static void openLink(String token) {
        System.out.println("GET /vanish/read?t=" + token);
        try {
            String secret = noteService.readNote(token);
            System.out.println("  [SECRET REVEALED] \"" + secret + "\" (AES-256 decrypted on-the-fly)");
            System.out.println("  [INFO] Note view counter decremented / burned if limit reached.");
        } catch (NoteNotFoundException e) {
            System.out.println("  [ERROR - NoteNotFoundException] " + e.getMessage());
        } catch (NoteBurnedException e) {
            System.out.println("  [ERROR - NoteBurnedException] " + e.getMessage());
        } catch (NoteExpiredException e) {
            System.out.println("  [ERROR - NoteExpiredException] " + e.getMessage());
        } catch (NotePendingException e) {
            System.out.println("  [ERROR - NotePendingException] " + e.getMessage());
        }
    }

    private static void displayDashboard(User owner) {
        System.out.println("Dashboard for user: " + owner.getUsername());
        List<Note> userNotes = noteService.getNotesByOwner(owner.getUsername());
        if (userNotes.isEmpty()) {
            System.out.println("  No notes found.");
            return;
        }
        for (Note n : userNotes) {
            System.out.println(n.dashboardRow());
        }
    }

    public static void main(String[] args) {
        System.out.println("============================================================");
        System.out.println("   VANISH - Phase 2 Structured & Reliable Console Demo      ");
        System.out.println("============================================================");

        heading("1. REGISTRATION & INPUT VALIDATION (Strong Password Policy)");
        // Invalid username (too short)
        registerUser("ab", "JavaPass@123", "JavaPass@123");
        // Weak password (simple 'abcdef' rejection)
        registerUser("student", "abcdef", "abcdef");
        // Weak password (missing special character and uppercase)
        registerUser("student", "secret123", "secret123");
        // Passwords do not match
        registerUser("student", "JavaPass@123", "Mismatch@123");
        // Valid registration with strong password
        User userStudent = registerUser("student", "JavaPass@123", "JavaPass@123");
        // Duplicate username attempt
        registerUser("student", "OtherPass@456", "OtherPass@456");

        heading("2. IDENTICAL PASSWORDS TEST (Salted SHA-256 Demonstration)");
        // Registering a second user with the EXACT SAME password to demonstrate salting
        User userAlice = registerUser("alice", "JavaPass@123", "JavaPass@123");
        if (userStudent != null && userAlice != null) {
            System.out.println("\n  [SECURITY PROOF - Unique Salts Protect Identical Passwords]");
            System.out.println("  User 'student' password hash: " + userStudent.getPasswordHash());
            System.out.println("  User 'student' salt         : " + userStudent.getSalt());
            System.out.println("  User 'alice'   password hash: " + userAlice.getPasswordHash());
            System.out.println("  User 'alice'   salt         : " + userAlice.getSalt());
            System.out.println("  Are hashes identical? " + userStudent.getPasswordHash().equals(userAlice.getPasswordHash())
                    + " (Guaranteed false because each user has a unique random salt!)");
        }

        heading("3. USER AUTHENTICATION");
        // Wrong password
        loginUser("student", "WrongPass@999");
        // Non-existent user
        loginUser("unknown_user", "JavaPass@123");
        // Successful login
        User currentUser = loginUser("student", "JavaPass@123");

        heading("4. LOGGED-IN USER DETAILS (Masked Encapsulation)");
        if (currentUser != null) {
            currentUser.displayUser();
        }

        heading("5. COMPOSING SECRET NOTES (AES-256 Encryption)");
        // Empty text rejection
        composeNote(currentUser, "   ", 10, 1);
        // Expiry out of range
        composeNote(currentUser, "Credit card PIN: 9988", 5000, 1);
        // Valid note 1
        Note note1 = composeNote(currentUser, "ATM PIN: 4821", 10, 1);
        // Valid note 2
        Note note2 = composeNote(currentUser, "WiFi Password: SuperSecretWifi@2026", 60, 2);

        // Pre-expired note for testing expiration exception
        try {
            Note staleNote = noteService.createNoteWithTimestamps(
                    currentUser.getUsername(),
                    "Old temporary access token",
                    LocalDateTime.now().minusMinutes(40),
                    LocalDateTime.now().minusMinutes(10),
                    1
            );
            System.out.println("  [TEST SETUP] Stale note created with token: " + staleNote.getToken() + " (already expired)");
        } catch (InvalidInputException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }

        heading("6. SENDER DASHBOARD (Before Reads)");
        displayDashboard(currentUser);

        heading("7. RECIPIENT ACCESSES VALID LINK (Note 1)");
        if (note1 != null) {
            openLink(note1.getToken());
        }

        heading("8. RECIPIENT RE-ACCESSES BURNED LINK (Note 1)");
        if (note1 != null) {
            openLink(note1.getToken());
            System.out.println("  Ciphertext status in RAM: " + note1.getCiphertextPreview());
        }

        heading("9. ACCESSING MULTI-VIEW NOTE (Note 2 - 2 Views Permitted)");
        if (note2 != null) {
            System.out.println("-- First view of Note 2 --");
            openLink(note2.getToken());
            System.out.println("-- Second view of Note 2 (Burns after this) --");
            openLink(note2.getToken());
            System.out.println("-- Third view attempt (Should be burned) --");
            openLink(note2.getToken());
        }

        heading("10. ACCESSING EXPIRED NOTE");
        List<Note> notes = noteService.getNotesByOwner(currentUser.getUsername());
        Note expiredNote = notes.stream().filter(Note::isExpired).findFirst().orElse(null);
        if (expiredNote != null) {
            openLink(expiredNote.getToken());
        }

        heading("11. ACCESSING NON-EXISTENT OR MALICIOUS TOKEN");
        openLink("invalidTokenXYZ99");

        heading("12. SENDER DASHBOARD (After Reads & Destructions)");
        displayDashboard(currentUser);

        System.out.println();
        System.out.println("============================================================");
        System.out.println(" Phase 2 Complete: Packages, Interfaces, Custom Exceptions, ");
        System.out.println(" Salted SHA-256, AES-256, Secure Tokens verified!           ");
        System.out.println("============================================================");
    }
}
