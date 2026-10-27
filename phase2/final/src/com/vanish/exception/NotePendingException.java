package com.vanish.exception;

/**
 * Thrown when a recipient opens a note the admin has not approved yet.
 */
public class NotePendingException extends VanishException {
    public NotePendingException(String message) {
        super(message);
    }
}
