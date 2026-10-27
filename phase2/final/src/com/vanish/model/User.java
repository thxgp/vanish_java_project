package com.vanish.model;

import com.vanish.util.CryptoUtil;

import java.time.LocalDateTime;

/**
 * Represents a registered user in the Vanish application.
 * Follows encapsulation principles: raw passwords are never stored in memory;
 * only cryptographically random salts and salted SHA-256 hashes are retained.
 */
public class User {
    private final String username;
    private final String salt;
    private final String passwordHash;
    private final LocalDateTime registeredAt;
    private final Role role;

    public User(String username, String rawPassword) {
        this(username, rawPassword, Role.USER);
    }

    public User(String username, String rawPassword, Role role) {
        this.username = username != null ? username.trim() : "";
        this.salt = CryptoUtil.generateSalt();
        this.passwordHash = CryptoUtil.hashPassword(rawPassword, this.salt);
        this.registeredAt = LocalDateTime.now();
        this.role = role != null ? role : Role.USER;
    }

    public User(String username, String salt, String passwordHash, LocalDateTime registeredAt) {
        this.username = username != null ? username.trim() : "";
        this.salt = salt;
        this.passwordHash = passwordHash;
        this.registeredAt = registeredAt != null ? registeredAt : LocalDateTime.now();
        this.role = Role.USER;
    }

    public Role getRole() {
        return role;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public String getUsername() {
        return username;
    }

    public String getSalt() {
        return salt;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public String maskedPassword() {
        return "******** [Salted SHA-256: " + passwordHash.substring(0, Math.min(10, passwordHash.length())) + "...]";
    }

    public boolean matches(String username, String password) {
        if (username == null || password == null) {
            return false;
        }
        return this.username.equalsIgnoreCase(username.trim())
                && CryptoUtil.verifyPassword(password, this.salt, this.passwordHash);
    }

    public void displayUser() {
        System.out.println("Username      : " + username);
        System.out.println("Password Hash : " + passwordHash);
        System.out.println("Unique Salt   : " + salt);
        System.out.println("Registered At : " + registeredAt);
    }
}
