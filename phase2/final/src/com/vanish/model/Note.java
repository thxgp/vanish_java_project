package com.vanish.model;

import com.vanish.util.CryptoUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a self-destructing secret note.
 * Protects secret content by encrypting text with AES-256-GCM in memory.
 * Upon exhaustion of allowed views, the ciphertext is purged completely from memory.
 */
public class Note {
    public static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private final int id;
    private final String token;
    private final String owner;
    private String encryptedText;
    private final LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private final int expiryMinutes;
    private LocalDateTime readAt;
    private int viewsLeft;
    private boolean burned;
    private boolean approved;
    private boolean rejected;

    /**
     * Standard constructor: note starts PENDING; the expiry clock restarts when the admin approves it.
     */
    public Note(int id, String token, String owner, String text, int expiryMinutes, int views) {
        this.id = id;
        this.token = token;
        this.owner = owner;
        this.encryptedText = CryptoUtil.encryptAES(text != null ? text.trim() : "");
        this.createdAt = LocalDateTime.now();
        this.expiryMinutes = expiryMinutes;
        this.expiresAt = this.createdAt.plusMinutes(expiryMinutes);
        this.readAt = null;
        this.viewsLeft = views;
        this.burned = false;
    }

    /**
     * Overloaded constructor: allows explicit timestamps (useful for simulating expired notes).
     * Such notes are treated as already approved.
     */
    public Note(int id, String token, String owner, String text,
                LocalDateTime createdAt, LocalDateTime expiresAt, int views) {
        this.id = id;
        this.token = token;
        this.owner = owner;
        this.encryptedText = CryptoUtil.encryptAES(text != null ? text.trim() : "");
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.expiryMinutes = (int) java.time.Duration.between(createdAt, expiresAt).toMinutes();
        this.readAt = null;
        this.viewsLeft = views;
        this.burned = false;
        this.approved = true;
    }

    /** Admin releases the note: link goes live and the expiry clock starts now. */
    public synchronized void approve() {
        if (approved || rejected || burned) {
            return;
        }
        this.approved = true;
        this.expiresAt = LocalDateTime.now().plusMinutes(expiryMinutes);
    }

    /** Admin refuses delivery: ciphertext is purged and the link is dead. */
    public synchronized void reject() {
        if (approved || burned) {
            return;
        }
        this.rejected = true;
        this.encryptedText = null;
    }

    public synchronized boolean isPending() {
        return !approved && !rejected && !burned;
    }

    public synchronized boolean isRejected() {
        return rejected;
    }

    public int getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public String getOwner() {
        return owner;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public int getViewsLeft() {
        return viewsLeft;
    }

    public synchronized boolean isBurned() {
        return burned;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public synchronized boolean isReadable() {
        return approved && !burned && !isExpired() && viewsLeft > 0;
    }

    public synchronized NoteStatus getStatus() {
        if (rejected) {
            return NoteStatus.REJECTED;
        }
        if (burned) {
            return NoteStatus.BURNED;
        }
        if (!approved) {
            return NoteStatus.PENDING;
        }
        if (isExpired()) {
            return NoteStatus.EXPIRED;
        }
        return NoteStatus.UNREAD;
    }

    public String shareLink() {
        return "/vanish/read?t=" + token;
    }

    /**
     * Returns the raw AES ciphertext preview (safe to show in security demos).
     */
    public String getCiphertextPreview() {
        if (encryptedText == null) {
            return "[PURGED/BURNED FROM RAM]";
        }
        return encryptedText.length() > 24
                ? encryptedText.substring(0, 24) + "..."
                : encryptedText;
    }

    /**
     * Reads the secret content, logs the read timestamp, and decrements views.
     * Decrypts AES-256 ciphertext on-the-fly.
     * Burns the note and purges ciphertext from memory once view count reaches zero.
     */
    public synchronized String read() {
        this.readAt = LocalDateTime.now();
        this.viewsLeft--;
        String decryptedSecret = CryptoUtil.decryptAES(this.encryptedText);
        if (this.viewsLeft <= 0) {
            this.burned = true;
            this.encryptedText = null; // Purge ciphertext from RAM completely
        }
        return decryptedSecret;
    }

    /**
     * Admin force-burn: purges the ciphertext without revealing it and kills the link.
     */
    public synchronized void burn() {
        this.viewsLeft = 0;
        this.burned = true;
        this.encryptedText = null;
    }

    /**
     * Generates a safe dashboard row representation without exposing the secret text.
     */
    public String dashboardRow() {
        return String.format("  #%-2d  %-10s  %-7s  views=%d  expires %s  read %s",
                id,
                token,
                getStatus(),
                viewsLeft,
                expiresAt.format(STAMP),
                readAt == null ? "-" : readAt.format(STAMP));
    }
}
