package com.vanish.exception;

/**
 * Thrown when user or note input validation fails (e.g. empty strings, out-of-range values).
 */
public class InvalidInputException extends VanishException {
    public InvalidInputException(String message) {
        super(message);
    }
}
