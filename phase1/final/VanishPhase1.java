// ============================================================
//  VANISH  -  One-Time Secret Note Sharing
//  PHASE 1  |  Core Application Setup and Design  (deliverable)
//
//  Everything from Steps 1-3 in one runnable console program:
//    - User entity        (attributes, constructor, methods)
//    - Note entity        (overloaded constructors, burn on read)
//    - Validator          (string handling, prints the results)
//    - a scripted run     register -> login -> compose -> read
//
//  No packages, no servlets, no exceptions yet - those are
//  Phase 2. This file only has to compile and run on its own.
// ============================================================

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

// ------------------------------------------------------------
//  Validation : all string handling in one place
// ------------------------------------------------------------
class Validator {

    static final int MIN_USERNAME_LENGTH = 3;
    static final int MAX_USERNAME_LENGTH = 20;
    static final int MIN_PASSWORD_LENGTH = 6;
    static final int MAX_NOTE_LENGTH = 500;
    static final int MIN_EXPIRY_MINUTES = 1;
    static final int MAX_EXPIRY_MINUTES = 1440;
    static final int MAX_VIEWS = 5;

    static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            System.out.println("  rejected : username cannot be empty");
            return false;
        }
        if (username.trim().length() < MIN_USERNAME_LENGTH) {
            System.out.println("  rejected : username must be at least " + MIN_USERNAME_LENGTH + " characters");
            return false;
        }
        if (username.trim().length() > MAX_USERNAME_LENGTH) {
            System.out.println("  rejected : username cannot be longer than " + MAX_USERNAME_LENGTH + " characters");
            return false;
        }
        if (username.contains(" ")) {
            System.out.println("  rejected : username cannot contain spaces");
            return false;
        }
        return true;
    }

    static boolean isValidPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            System.out.println("  rejected : password cannot be empty");
            return false;
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            System.out.println("  rejected : password must be at least " + MIN_PASSWORD_LENGTH + " characters");
            return false;
        }
        return true;
    }

    static boolean isMatchingPassword(String password, String confirmPassword) {
        if (confirmPassword == null || !password.equals(confirmPassword)) {
            System.out.println("  rejected : the two passwords do not match");
            return false;
        }
        return true;
    }

    static boolean isValidNoteText(String text) {
        if (text == null || text.trim().isEmpty()) {
            System.out.println("  rejected : the note cannot be empty");
            return false;
        }
        if (text.trim().length() > MAX_NOTE_LENGTH) {
            System.out.println("  rejected : the note cannot be longer than " + MAX_NOTE_LENGTH + " characters");
            return false;
        }
        return true;
    }

    static boolean isValidExpiry(int minutes) {
        if (minutes < MIN_EXPIRY_MINUTES || minutes > MAX_EXPIRY_MINUTES) {
            System.out.println("  rejected : expiry must be between " + MIN_EXPIRY_MINUTES
                    + " and " + MAX_EXPIRY_MINUTES + " minutes");
            return false;
        }
        return true;
    }

    static boolean isValidViews(int views) {
        if (views < 1 || views > MAX_VIEWS) {
            System.out.println("  rejected : views must be between 1 and " + MAX_VIEWS);
            return false;
        }
        return true;
    }
}

// ------------------------------------------------------------
//  Entity : User
// ------------------------------------------------------------
class User {
    String username;
    String password;
    LocalDateTime registeredAt;

    User(String username, String password) {
        this.username = username.trim();
        this.password = password;
        this.registeredAt = LocalDateTime.now();
    }

    String maskedPassword() {
        return "*".repeat(password.length());
    }

    boolean matches(String username, String password) {
        return this.username.equals(username) && this.password.equals(password);
    }

    void displayUser() {
        System.out.println("Username : " + username);
        System.out.println("Password : " + maskedPassword());
    }
}

// ------------------------------------------------------------
//  Entity : Note
// ------------------------------------------------------------
class Note {
    static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    int id;
    String token;
    String owner;
    String text;
    LocalDateTime createdAt;
    LocalDateTime expiresAt;
    LocalDateTime readAt;
    int viewsLeft;
    boolean burned;

    // Constructor 1 : expiry counted in minutes from now
    Note(int id, String token, String owner, String text, int expiryMinutes, int views) {
        this.id = id;
        this.token = token;
        this.owner = owner;
        this.text = text.trim();
        this.createdAt = LocalDateTime.now();
        this.expiresAt = this.createdAt.plusMinutes(expiryMinutes);
        this.readAt = null;
        this.viewsLeft = views;
        this.burned = false;
    }

    // Constructor 2 (overloaded) : exact timestamps, used to place an
    // older note in the demo so the EXPIRED state can be shown
    Note(int id, String token, String owner, String text,
         LocalDateTime createdAt, LocalDateTime expiresAt, int views) {
        this.id = id;
        this.token = token;
        this.owner = owner;
        this.text = text.trim();
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.readAt = null;
        this.viewsLeft = views;
        this.burned = false;
    }

    boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    boolean isReadable() {
        return !burned && !isExpired() && viewsLeft > 0;
    }

    String status() {
        if (burned) {
            return "BURNED";
        }
        if (isExpired()) {
            return "EXPIRED";
        }
        return "UNREAD";
    }

    String shareLink() {
        return "/vanish/read?t=" + token;
    }

    String read() {
        this.readAt = LocalDateTime.now();
        this.viewsLeft = this.viewsLeft - 1;
        if (this.viewsLeft <= 0) {
            this.burned = true;
        }
        return this.text;
    }

    // One dashboard row. The secret is never part of it.
    String dashboardRow() {
        return String.format("  #%-2d  %-9s  %-7s  views=%d  expires %s  read %s",
                id,
                token,
                status(),
                viewsLeft,
                expiresAt.format(STAMP),
                readAt == null ? "-" : readAt.format(STAMP));
    }
}

// ------------------------------------------------------------
//  The application
// ------------------------------------------------------------
public class VanishPhase1 {

    static ArrayList<User> users = new ArrayList<>();
    static ArrayList<Note> notes = new ArrayList<>();
    static int nextNoteId = 1;

    // Phase 2 replaces these hardcoded tokens with
    // SecureRandom + URL-safe Base64 inside NoteService.
    static String[] demoTokens = {"k7Qm2xR9", "pZ3vTn81", "b9Lw4Qe2"};
    static int nextToken = 0;

    // ---------- accounts ----------
    static User findUser(String username) {
        for (User u : users) {
            if (u.username.equals(username)) {
                return u;
            }
        }
        return null;
    }

    static void register(String username, String password, String confirmPassword) {
        System.out.println("register : username=[" + username + "] password=[" + password
                + "] confirm=[" + confirmPassword + "]");

        if (!Validator.isValidUsername(username)) {
            return;
        }
        if (!Validator.isValidPassword(password)) {
            return;
        }
        if (!Validator.isMatchingPassword(password, confirmPassword)) {
            return;
        }
        if (findUser(username.trim()) != null) {
            System.out.println("  rejected : that username is already taken");
            return;
        }

        users.add(new User(username, password));
        System.out.println("  accepted : account created for " + username.trim());
    }

    static User login(String username, String password) {
        System.out.println("login : username=[" + username + "] password=[" + password + "]");
        User user = findUser(username);
        if (user == null || !user.matches(username, password)) {
            System.out.println("  failed : invalid username or password");
            return null;
        }
        System.out.println("  ok : welcome back, " + user.username);
        return user;
    }

    // ---------- notes ----------
    static Note createNote(User owner, String text, int expiryMinutes, int views) {
        System.out.println("compose : expiry=" + expiryMinutes + "min views=" + views
                + " length=" + (text == null ? 0 : text.length()));

        if (!Validator.isValidNoteText(text)) {
            return null;
        }
        if (!Validator.isValidExpiry(expiryMinutes)) {
            return null;
        }
        if (!Validator.isValidViews(views)) {
            return null;
        }

        Note note = new Note(nextNoteId, demoTokens[nextToken], owner.username, text, expiryMinutes, views);
        nextNoteId = nextNoteId + 1;
        nextToken = nextToken + 1;
        notes.add(note);

        System.out.println("  accepted : note #" + note.id + " created");
        System.out.println("  link     : " + note.shareLink());
        return note;
    }

    static Note findByToken(String token) {
        for (Note n : notes) {
            if (n.token.equals(token)) {
                return n;
            }
        }
        return null;
    }

    // The public read page. In Phase 2 these three branches become
    // NoteNotFoundException / NoteBurnedException / NoteExpiredException.
    static void openLink(String token) {
        System.out.println("GET /vanish/read?t=" + token);
        Note note = findByToken(token);

        if (note == null) {
            System.out.println("  page shows : invalid link, no such note");
            return;
        }
        if (note.burned) {
            System.out.println("  page shows : this note has been burned");
            return;
        }
        if (note.isExpired()) {
            System.out.println("  page shows : this note has expired");
            return;
        }

        System.out.println("  page shows : " + note.read());
        System.out.println("  page shows : this note is now destroyed, the link is dead");
    }

    static void dashboard(User owner) {
        System.out.println("dashboard of " + owner.username);
        int count = 0;
        for (Note n : notes) {
            if (n.owner.equals(owner.username)) {
                System.out.println(n.dashboardRow());
                count = count + 1;
            }
        }
        if (count == 0) {
            System.out.println("  no notes yet");
        }
    }

    static void heading(String title) {
        System.out.println();
        System.out.println("===== " + title + " =====");
    }

    public static void main(String[] args) {

        System.out.println("=========================================");
        System.out.println("  VANISH  -  Phase 1 console walkthrough ");
        System.out.println("=========================================");

        heading("1. REGISTRATION");
        register("ab", "java123", "java123");
        register("student", "123", "123");
        register("student", "java123", "java1234");
        register("student", "java123", "java123");
        register("student", "meow123", "meow123");

        heading("2. LOGIN");
        login("student", "wrongpass");
        User user = login("student", "java123");

        heading("3. LOGGED IN USER");
        user.displayUser();

        heading("4. COMPOSING NOTES");
        createNote(user, "   ", 10, 1);
        createNote(user, "atm pin 4821", 5000, 1);
        Note first = createNote(user, "atm pin 4821", 10, 1);
        createNote(user, "wifi password meow@2026", 60, 1);

        // an older note, already past its expiry, added directly
        Note stale = new Note(nextNoteId, "aX01zzQ7", user.username, "old exam seat number",
                LocalDateTime.now().minusMinutes(30), LocalDateTime.now().minusMinutes(20), 1);
        nextNoteId = nextNoteId + 1;
        notes.add(stale);

        heading("5. DASHBOARD BEFORE ANYBODY READS");
        dashboard(user);

        heading("6. RECIPIENT OPENS THE LINK");
        openLink(first.token);

        heading("7. RECIPIENT REFRESHES THE PAGE");
        openLink(first.token);

        heading("8. SOMEONE OPENS THE EXPIRED NOTE");
        openLink(stale.token);

        heading("9. SOMEONE GUESSES A TOKEN");
        openLink("zzzzzzzz");

        heading("10. DASHBOARD AFTER THE READ");
        dashboard(user);

        System.out.println();
        System.out.println("Phase 1 complete : entities, constructors, methods, validation.");
    }
}
