package com.vanish.exception;

/**
 * Thrown when trying to access a note that has already been read and burned.
 */
public class NoteBurnedException extends VanishException {
    public NoteBurnedException(String message) {
        super(message);
    }
}
