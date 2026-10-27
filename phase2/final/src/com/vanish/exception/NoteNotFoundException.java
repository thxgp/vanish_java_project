package com.vanish.exception;

/**
 * Thrown when a note cannot be found matching a given token.
 */
public class NoteNotFoundException extends VanishException {
    public NoteNotFoundException(String message) {
        super(message);
    }
}
