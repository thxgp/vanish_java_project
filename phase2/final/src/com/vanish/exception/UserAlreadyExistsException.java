package com.vanish.exception;

/**
 * Thrown when trying to register with a username that already exists.
 */
public class UserAlreadyExistsException extends VanishException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
