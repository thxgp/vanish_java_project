package com.vanish.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Cryptographic utility providing:
 *  1. Salted SHA-256 hashing for user passwords.
 *  2. AES-256-GCM authenticated encryption for secret note text in RAM.
 */
public final class CryptoUtil {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int SALT_BYTES = 16;
    private static final int GCM_IV_BYTES = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;

    // Ephemeral AES-256 Master Key generated at application startup for RAM encryption
    private static final SecretKey AES_KEY;

    static {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256, RANDOM);
            AES_KEY = keyGen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("AES-256 algorithm not available in this JVM environment", e);
        }
    }

    private CryptoUtil() {
        // Prevent instantiation
    }

    // =========================================================================
    //  PASSWORD HASHING (Salted SHA-256)
    // =========================================================================

    /**
     * Generates a cryptographically random salt.
     */
    public static String generateSalt() {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Computes the SHA-256 hash of a password combined with a unique salt.
     *
     * @param password raw user password
     * @param saltBase64 Base64-encoded salt
     * @return Base64-encoded hash
     */
    public static String hashPassword(String password, String saltBase64) {
        if (password == null || saltBase64 == null) {
            throw new IllegalArgumentException("Password and salt cannot be null");
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] salt = Base64.getDecoder().decode(saltBase64);
            md.update(salt);
            byte[] hashed = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Verifies a raw password against stored salt and expected hash in constant time.
     */
    public static boolean verifyPassword(String password, String saltBase64, String expectedHash) {
        if (password == null || saltBase64 == null || expectedHash == null) {
            return false;
        }
        String calculated = hashPassword(password, saltBase64);
        // Constant-time check to prevent timing attacks
        return MessageDigest.isEqual(
                calculated.getBytes(StandardCharsets.UTF_8),
                expectedHash.getBytes(StandardCharsets.UTF_8)
        );
    }

    // =========================================================================
    //  SYMMETRIC ENCRYPTION (AES-256-GCM) FOR SECRET NOTES
    // =========================================================================

    /**
     * Encrypts plaintext using AES-256-GCM.
     * Generates a fresh 12-byte IV for every encryption call.
     * Returns Base64 string containing [12 bytes IV + ciphertext with authentication tag].
     */
    public static String encryptAES(String plainText) {
        if (plainText == null) {
            return null;
        }
        try {
            byte[] iv = new byte[GCM_IV_BYTES];
            RANDOM.nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.ENCRYPT_MODE, AES_KEY, spec);

            byte[] cipherBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            ByteBuffer buffer = ByteBuffer.allocate(iv.length + cipherBytes.length);
            buffer.put(iv);
            buffer.put(cipherBytes);

            return Base64.getEncoder().encodeToString(buffer.array());
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting note text with AES-256-GCM", e);
        }
    }

    /**
     * Decrypts an AES-256-GCM Base64-encoded ciphertext payload.
     */
    public static String decryptAES(String cipherTextBase64) {
        if (cipherTextBase64 == null) {
            return null;
        }
        try {
            byte[] combined = Base64.getDecoder().decode(cipherTextBase64);
            ByteBuffer buffer = ByteBuffer.wrap(combined);

            byte[] iv = new byte[GCM_IV_BYTES];
            buffer.get(iv);

            byte[] cipherBytes = new byte[buffer.remaining()];
            buffer.get(cipherBytes);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.DECRYPT_MODE, AES_KEY, spec);

            byte[] decrypted = cipher.doFinal(cipherBytes);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting note text with AES-256-GCM", e);
        }
    }
}
