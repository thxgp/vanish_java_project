// ============================================================
//  VANISH  -  One-Time Secret Note Sharing
//  PHASE 1  |  Step 2 : Class Structure with Constructors
//                       and Methods
//
//  Objective : same entities as Step 1, but the values now go in
//              through constructors and the behaviour lives in
//              methods (displayUser, displayNote, read, status).
// ============================================================

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

class User {
    String username;
    String password;

    // Constructor
    User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Method to display user details
    void displayUser() {
        System.out.println("Username : " + username);
        System.out.println("Password : " + maskedPassword());
    }

    // A password is a secret, so it is never printed in readable form
    String maskedPassword() {
        return new String(new char[password.length()]).replace("\0", "*");
    }

    // Method used later by the login check
    boolean matches(String username, String password) {
        return this.username.equals(username) && this.password.equals(password);
    }
}

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

    // Constructor : expiry given in minutes from now
    Note(int id, String token, String owner, String text, int expiryMinutes, int views) {
        this.id = id;
        this.token = token;
        this.owner = owner;
        this.text = text;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = this.createdAt.plusMinutes(expiryMinutes);
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

    // Burn on read : hand over the secret, then destroy the note
    String read() {
        this.readAt = LocalDateTime.now();
        this.viewsLeft = this.viewsLeft - 1;
        if (this.viewsLeft <= 0) {
            this.burned = true;
        }
        return this.text;
    }

    // The sender's dashboard row : shows the state, never the secret
    void displayNote() {
        System.out.println("Note ID    : " + id);
        System.out.println("Owner      : " + owner);
        System.out.println("Link       : " + shareLink());
        System.out.println("Created At : " + createdAt.format(STAMP));
        System.out.println("Expires At : " + expiresAt.format(STAMP));
        System.out.println("Read At    : " + (readAt == null ? "not opened yet" : readAt.format(STAMP)));
        System.out.println("Views Left : " + viewsLeft);
        System.out.println("Status     : " + status());
    }
}

public class UserStructureDemo {
    public static void main(String[] args) {

        User user = new User("student", "java123");

        System.out.println("===== USER DETAILS =====");
        user.displayUser();

        Note note = new Note(1, "k7Qm2xR9", user.username, "atm pin 4821", 10, 1);

        System.out.println();
        System.out.println("===== NOTE CREATED =====");
        note.displayNote();

        System.out.println();
        System.out.println("===== RECIPIENT OPENS THE LINK =====");
        if (note.isReadable()) {
            System.out.println("Secret     : " + note.read());
            System.out.println("(the note has now been destroyed)");
        }

        System.out.println();
        System.out.println("===== SENDER'S DASHBOARD AFTER THE READ =====");
        note.displayNote();

        System.out.println();
        System.out.println("===== RECIPIENT REFRESHES THE PAGE =====");
        if (note.isReadable()) {
            System.out.println("Secret     : " + note.read());
        } else {
            System.out.println("This note has been burned.");
        }
    }
}
