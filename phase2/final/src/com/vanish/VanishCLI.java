package com.vanish;

import com.vanish.exception.AuthenticationException;
import com.vanish.exception.InvalidInputException;
import com.vanish.exception.NoteBurnedException;
import com.vanish.exception.NoteExpiredException;
import com.vanish.exception.NoteNotFoundException;
import com.vanish.exception.UserAlreadyExistsException;
import com.vanish.model.Note;
import com.vanish.model.User;
import com.vanish.service.NoteService;
import com.vanish.service.UserService;
import com.vanish.service.impl.NoteServiceImpl;
import com.vanish.service.impl.UserServiceImpl;

import java.util.List;
import java.util.Scanner;

/**
 * Interactive Command Line Interface for Vanish (Phase 2).
 * Allows live demonstration of all user operations, input validations,
 * custom exceptions, and self-destructing secret note mechanics.
 */
public class VanishCLI {

    private static final UserService userService = new UserServiceImpl();
    private static final NoteService noteService = new NoteServiceImpl();
    private static final Scanner scanner = new Scanner(System.in);

    private static User currentUser = null;

    public static void main(String[] args) {
        System.out.println("============================================================");
        System.out.println("       VANISH: Self-Destructing Notes (Phase 2 CLI)         ");
        System.out.println("   Demonstrating: Packages, Interfaces & Custom Exceptions  ");
        System.out.println("============================================================");

        boolean running = true;
        while (running) {
            printMenu();
            System.out.print("Select an option [1-8]: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    handleRegister();
                    break;
                case "2":
                    handleLogin();
                    break;
                case "3":
                    handleLogout();
                    break;
                case "4":
                    handleComposeNote();
                    break;
                case "5":
                    handleReadNote();
                    break;
                case "6":
                    handleViewDashboard();
                    break;
                case "7":
                    System.out.println("\n[Running automated end-to-end test suite...]");
                    VanishPhase2.main(new String[]{});
                    break;
                case "8":
                    System.out.println("\nExiting Vanish. Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Please choose between 1 and 8.");
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n------------------------------------------------------------");
        if (currentUser != null) {
            System.out.println("LOGGED IN AS: " + currentUser.getUsername().toUpperCase());
        } else {
            System.out.println("STATUS: Not logged in (Guest)");
        }
        System.out.println("------------------------------------------------------------");
        System.out.println("1. Register new user        (tests InvalidInput / UserAlreadyExists)");
        System.out.println("2. Login                    (tests AuthenticationException)");
        System.out.println("3. Logout");
        System.out.println("4. Compose secret note      (tests text / expiry / view validation)");
        System.out.println("5. Read / Burn note link    (tests NoteNotFound / Burned / Expired)");
        System.out.println("6. View User Dashboard      (shows live note statuses)");
        System.out.println("7. Run Automated Demo Suite (executes all test cases automatically)");
        System.out.println("8. Exit");
        System.out.println("------------------------------------------------------------");
    }

    private static void handleRegister() {
        System.out.println("\n--- [1] User Registration ---");
        System.out.print("Enter username (3-20 chars, no spaces): ");
        String username = scanner.nextLine();

        System.out.print("Enter password (min 8 chars, uppercase, lowercase, number, special char): ");
        String password = scanner.nextLine();

        System.out.print("Confirm password: ");
        String confirmPassword = scanner.nextLine();

        try {
            User registered = userService.register(username, password, confirmPassword);
            System.out.println("[SUCCESS] Account created for '" + registered.getUsername() + "'!");
            System.out.println("  Unique Salt Generated  : " + registered.getSalt());
            System.out.println("  Stored SHA-256 Hash    : " + registered.getPasswordHash());
            currentUser = registered;
            System.out.println("[INFO] You are now automatically logged in as: " + currentUser.getUsername());
        } catch (InvalidInputException e) {
            System.out.println("[CAUGHT InvalidInputException] " + e.getMessage());
        } catch (UserAlreadyExistsException e) {
            System.out.println("[CAUGHT UserAlreadyExistsException] " + e.getMessage());
        }
    }

    private static void handleLogin() {
        System.out.println("\n--- [2] User Login ---");
        System.out.print("Enter username: ");
        String username = scanner.nextLine();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        try {
            currentUser = userService.authenticate(username, password);
            System.out.println("[SUCCESS] Welcome back, " + currentUser.getUsername() + "!");
        } catch (InvalidInputException e) {
            System.out.println("[CAUGHT InvalidInputException] " + e.getMessage());
        } catch (AuthenticationException e) {
            System.out.println("[CAUGHT AuthenticationException] " + e.getMessage());
        }
    }

    private static void handleLogout() {
        if (currentUser == null) {
            System.out.println("\n[INFO] No active session to log out from.");
            return;
        }
        System.out.println("\n[SUCCESS] User '" + currentUser.getUsername() + "' logged out.");
        currentUser = null;
    }

    private static void handleComposeNote() {
        if (currentUser == null) {
            System.out.println("\n[ERROR] You must be logged in to compose a secret note. Please login first (Option 2).");
            return;
        }

        System.out.println("\n--- [4] Compose Secret Note ---");
        System.out.print("Enter your secret message (e.g. password, OTP, PIN): ");
        String text = scanner.nextLine();

        System.out.print("Enter expiry time in minutes (1 - 1440): ");
        String expiryInput = scanner.nextLine().trim();

        System.out.print("Enter permitted view count before burning (1 - 5): ");
        String viewsInput = scanner.nextLine().trim();

        int expiry;
        int views;
        try {
            expiry = Integer.parseInt(expiryInput);
            views = Integer.parseInt(viewsInput);
        } catch (NumberFormatException e) {
            System.out.println("[CAUGHT InvalidInputException] Expiry and view count must be valid numbers.");
            return;
        }

        try {
            Note note = noteService.createNote(currentUser.getUsername(), text, expiry, views);
            System.out.println("[SUCCESS] Secret Note #" + note.getId() + " created!");
            System.out.println("  Secret Token : " + note.getToken());
            System.out.println("  Shareable Link: " + note.shareLink());
            System.out.println("  AES-256 Cipher: " + note.getCiphertextPreview());
            System.out.println("  Permitted Views: " + note.getViewsLeft());
            System.out.println("  Expires In    : " + expiry + " minute(s)");
        } catch (InvalidInputException e) {
            System.out.println("[CAUGHT InvalidInputException] " + e.getMessage());
        }
    }

    private static void handleReadNote() {
        System.out.println("\n--- [5] Read / Burn Secret Note Link (Recipient Side) ---");
        System.out.print("Enter Note Token (or paste token from share link): ");
        String token = scanner.nextLine().trim();

        // If the user pastes full link '/vanish/read?t=xyz', extract token
        if (token.contains("t=")) {
            token = token.substring(token.indexOf("t=") + 2);
        }

        try {
            String secret = noteService.readNote(token);
            System.out.println("\n==================================================");
            System.out.println(" >>> SECRET REVEALED: \"" + secret + "\" <<<");
            System.out.println("==================================================");
            System.out.println("[INFO] Note view counter decremented. If view limit was reached, note is now BURNED.");
        } catch (NoteNotFoundException e) {
            System.out.println("[CAUGHT NoteNotFoundException] " + e.getMessage());
        } catch (NoteBurnedException e) {
            System.out.println("[CAUGHT NoteBurnedException] " + e.getMessage());
        } catch (NoteExpiredException e) {
            System.out.println("[CAUGHT NoteExpiredException] " + e.getMessage());
        }
    }

    private static void handleViewDashboard() {
        if (currentUser == null) {
            System.out.println("\n[ERROR] You must be logged in to view your dashboard. Please login first (Option 2).");
            return;
        }

        System.out.println("\n--- [6] Sender Dashboard for: " + currentUser.getUsername() + " ---");
        List<Note> notes = noteService.getNotesByOwner(currentUser.getUsername());
        if (notes.isEmpty()) {
            System.out.println("No notes created yet. Try composing one (Option 4)!");
            return;
        }

        System.out.printf("%-5s %-12s %-10s %-9s %-22s %-20s\n", "ID", "Token", "Status", "Views", "Expires At", "Read At");
        System.out.println("----------------------------------------------------------------------------------");
        for (Note n : notes) {
            System.out.println(n.dashboardRow());
        }
    }
}
