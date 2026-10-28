package com.vanish.model;

/**
 * Enumeration representing the lifecycle state of a secret note.
 */
public enum NoteStatus {
    PENDING,   // waiting for admin approval; link not live yet
    UNREAD,
    BURNED,
    EXPIRED,
    REJECTED   // admin refused delivery; ciphertext purged
}
