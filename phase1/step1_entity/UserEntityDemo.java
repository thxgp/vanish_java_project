// ============================================================
//  VANISH  -  One-Time Secret Note Sharing
//  PHASE 1  |  Step 1 : Identify the Entities
//
//  Objective : create the entity classes with plain attributes,
//              assign values and print them (no constructors,
//              no methods yet).
// ============================================================

import java.time.LocalDateTime;

// ---------- Entity 1 : the account holder ----------
class User {
    String username;
    String password;
}

// ---------- Entity 2 : the self-destructing note ----------
class Note {
    int id;
    String token;             // random string that goes in the link
    String owner;             // username of the sender
    String text;              // the secret itself
    LocalDateTime createdAt;
    LocalDateTime expiresAt;
    LocalDateTime readAt;     // stays null until somebody opens it
    int viewsLeft;
    boolean burned;
}

public class UserEntityDemo {
    public static void main(String[] args) {

        User user = new User();
        user.username = "student";
        user.password = "java123";

        System.out.println("===== USER DETAILS =====");
        System.out.println("Username : " + user.username);
        System.out.println("Password : " + user.password);

        Note note = new Note();
        note.id        = 1;
        note.token     = "k7Qm2xR9";
        note.owner     = user.username;
        note.text      = "atm pin 4821";
        note.createdAt = LocalDateTime.now();
        note.expiresAt = note.createdAt.plusMinutes(10);
        note.readAt    = null;
        note.viewsLeft = 1;
        note.burned    = false;

        System.out.println();
        System.out.println("===== NOTE DETAILS =====");
        System.out.println("Note ID    : " + note.id);
        System.out.println("Token      : " + note.token);
        System.out.println("Owner      : " + note.owner);
        System.out.println("Secret     : " + note.text);
        System.out.println("Created At : " + note.createdAt);
        System.out.println("Expires At : " + note.expiresAt);
        System.out.println("Read At    : " + note.readAt);
        System.out.println("Views Left : " + note.viewsLeft);
        System.out.println("Burned     : " + note.burned);

        System.out.println();
        System.out.println("Share link : /vanish/read?t=" + note.token);
    }
}
