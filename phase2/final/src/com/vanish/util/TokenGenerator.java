package com.vanish.util;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Generates cryptographically secure, URL-safe random tokens for secret notes.
 */
public final class TokenGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final int DEFAULT_BYTE_LENGTH = 6; // Produces ~8 URL-safe characters

    private TokenGenerator() {
        // Prevent instantiation
    }

    /**
     * Generates an 8-character URL-safe random token.
     */
    public static String generateToken() {
        byte[] bytes = new byte[DEFAULT_BYTE_LENGTH];
        RANDOM.nextBytes(bytes);
        return URL_ENCODER.encodeToString(bytes);
    }
}
