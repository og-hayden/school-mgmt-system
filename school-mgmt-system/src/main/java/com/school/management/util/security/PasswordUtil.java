package com.school.management.util.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for handling password security functions.
 * Includes hashing with SHA-256 and unique salts, verification,
 * random password generation, and secure token generation.
 * NOTE: Uses SHA-256 as a fallback due to issues with integrating BCrypt library.
 * BCrypt is generally preferred for better resistance to brute-force attacks.
 */
public class PasswordUtil {

    private static final Logger LOGGER = Logger.getLogger(PasswordUtil.class.getName());
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH_BYTES = 16; // 16 bytes = 128 bits, a common size
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String PASSWORD_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+<>,.?/";

    /**
     * Private constructor to prevent instantiation.
     */
    private PasswordUtil() {
        throw new IllegalStateException("Utility class should not be instantiated.");
    }

    /**
     * Generates a cryptographically secure random salt.
     *
     * @return A new salt as a byte array.
     */
    public static byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        SECURE_RANDOM.nextBytes(salt);
        LOGGER.log(Level.FINE, "Generated new salt.");
        return salt;
    }

    /**
     * Generates a Base64 encoded string representation of a new salt.
     *
     * @return A Base64 encoded salt string.
     */
    public static String generateSaltBase64() {
        return Base64.getEncoder().encodeToString(generateSalt());
    }

    /**
     * Hashes a plain text password using SHA-256 combined with a provided salt.
     *
     * @param plainPassword The password to hash.
     * @param saltBytes     The salt to use (must not be null or empty).
     * @return The Base64 encoded hash string, or null if an error occurs.
     * @throws IllegalArgumentException if password or salt is null/empty.
     * @throws RuntimeException if SHA-256 algorithm is not available.
     */
    public static String hashPassword(String plainPassword, byte[] saltBytes) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty.");
        }
        if (saltBytes == null || saltBytes.length == 0) {
            throw new IllegalArgumentException("Salt cannot be null or empty.");
        }

        try {
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);

            // Combine salt and password bytes
            // Prepending salt is common, order doesn't strictly matter as long as consistent
            byte[] passwordBytes = plainPassword.getBytes(StandardCharsets.UTF_8);
            byte[] combined = new byte[saltBytes.length + passwordBytes.length];

            System.arraycopy(saltBytes, 0, combined, 0, saltBytes.length);
            System.arraycopy(passwordBytes, 0, combined, saltBytes.length, passwordBytes.length);

            // Hash the combined bytes
            byte[] hashedBytes = md.digest(combined);

            // Encode the hash to Base64 for storage
            String hashedPassword = Base64.getEncoder().encodeToString(hashedBytes);
            LOGGER.log(Level.INFO, "Password hashed successfully using SHA-256.");
            return hashedPassword;

        } catch (NoSuchAlgorithmException e) {
            LOGGER.log(Level.SEVERE, "Fatal: SHA-256 algorithm not found.", e);
            // This should theoretically never happen with SHA-256, but handle defensively
            throw new RuntimeException("Could not find SHA-256 algorithm", e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error hashing password.", e);
            // Return null or throw a custom exception
            return null;
        }
    }

    /**
     * Checks if a plain text password matches a stored hash, using the provided salt.
     *
     * @param plainPassword  The plain text password to check.
     * @param storedHashBase64 The stored Base64 encoded hash.
     * @param saltBase64     The stored Base64 encoded salt.
     * @return true if the password matches the hash, false otherwise.
     */
    public static boolean checkPassword(String plainPassword, String storedHashBase64, String saltBase64) {
        if (plainPassword == null || storedHashBase64 == null || saltBase64 == null) {
             LOGGER.warning("Attempted password check with null inputs.");
            return false;
        }

        try {
            // Decode the stored salt
            byte[] saltBytes = Base64.getDecoder().decode(saltBase64);

            // Hash the entered password with the decoded salt
            String calculatedHash = hashPassword(plainPassword, saltBytes);

            // Compare the calculated hash with the stored hash
            boolean match = calculatedHash != null && calculatedHash.equals(storedHashBase64);
             LOGGER.log(Level.INFO, "Password check performed. Match result: {0}", match);
            return match;

        } catch (IllegalArgumentException e) {
            // Handle potential Base64 decoding errors
             LOGGER.log(Level.WARNING, "Error decoding stored salt or hash during password check.", e);
            return false;
         } catch (RuntimeException e) {
             // Handle potential hashing errors (like missing algorithm)
              LOGGER.log(Level.SEVERE, "Runtime error during password check (e.g., SHA-256 missing).", e);
             return false;
         } catch (Exception e) {
              LOGGER.log(Level.SEVERE, "Unexpected error during password check.", e);
              return false; // Fail safely
         }
    }

    /**
     * Generates a random password of the specified length.
     *
     * @param length The desired length of the password (must be > 0).
     * @return A randomly generated password string.
     * @throws IllegalArgumentException if length is not positive.
     */
    public static String generateRandomPassword(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Password length must be positive.");
        }
        StringBuilder password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = SECURE_RANDOM.nextInt(PASSWORD_CHARS.length());
            password.append(PASSWORD_CHARS.charAt(randomIndex));
        }
        LOGGER.log(Level.INFO, "Generated random password of length {0}.", length);
        return password.toString();
    }

    /**
     * Generates a secure, URL-safe, random token (Base64 encoded).
     *
     * @param byteLength The number of random bytes to generate before encoding (e.g., 32).
     * @return A URL-safe Base64 encoded random token string.
     * @throws IllegalArgumentException if byteLength is not positive.
     */
    public static String generateResetToken(int byteLength) {
         if (byteLength <= 0) {
            throw new IllegalArgumentException("Token byte length must be positive.");
        }
        byte[] randomBytes = new byte[byteLength];
        SECURE_RANDOM.nextBytes(randomBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        LOGGER.log(Level.INFO, "Generated secure random token.");
        return token;
    }

    /**
     * Generates a secure, URL-safe, random token with a default length (32 bytes).
     *
     * @return A URL-safe Base64 encoded random token string.
     */
     public static String generateResetToken() {
         return generateResetToken(32);
     }
} 