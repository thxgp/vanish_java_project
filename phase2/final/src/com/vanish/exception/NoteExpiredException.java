package com.vanish.exception;

/**
 * Thrown when trying to access a note whose expiry deadline has passed.
 */
public class NoteExpiredException extends VanishException {
    public NoteExpiredException(String message) {
        super(message);
    }
}
