package com.vanish.exception;

/**
 * Thrown when login authentication fails due to invalid credentials.
 */
public class AuthenticationException extends VanishException {
    public AuthenticationException(String message) {
        super(message);
    }
}
