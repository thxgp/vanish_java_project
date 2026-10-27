package com.vanish.exception;

/**
 * Base checked exception for all domain-specific errors in the Vanish application.
 */
public class VanishException extends Exception {
    public VanishException(String message) {
        super(message);
    }

    public VanishException(String message, Throwable cause) {
        super(message, cause);
    }
}
