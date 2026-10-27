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
    private final LocalDateTime expiresAt;
    private LocalDateTime readAt;
    private int viewsLeft;
    private boolean burned;

    /**
     * Standard constructor: sets expiry time relative to creation time.
     */
    public Note(int id, String token, String owner, String text, int expiryMinutes, int views) {
        this.id = id;
        this.token = token;
        this.owner = owner;
        this.encryptedText = CryptoUtil.encryptAES(text != null ? text.trim() : "");
        this.createdAt = LocalDateTime.now();
        this.expiresAt = this.createdAt.plusMinutes(expiryMinutes);
        this.readAt = null;
        this.viewsLeft = views;
        this.burned = false;
    }

    /**
     * Overloaded constructor: allows explicit timestamps (useful for simulating expired notes).
     */
    public Note(int id, String token, String owner, String text,
                LocalDateTime createdAt, LocalDateTime expiresAt, int views) {
        this.id = id;
        this.token = token;
        this.owner = owner;
        this.encryptedText = CryptoUtil.encryptAES(text != null ? text.trim() : "");
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.readAt = null;
        this.viewsLeft = views;
        this.burned = false;
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

    public boolean isBurned() {
        return burned;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isReadable() {
        return !burned && !isExpired() && viewsLeft > 0;
    }

    public NoteStatus getStatus() {
        if (burned) {
            return NoteStatus.BURNED;
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
    public String read() {
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
