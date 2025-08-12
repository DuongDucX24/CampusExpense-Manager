package com.example.se07101campusexpenses.security;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordUtils {

    private static final int SALT_SIZE = 16; // 16 bytes salt
    private static final int HASH_SIZE = 32; // 32 bytes hash
    private static final int ITERATIONS = 10000; // Iteration count
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    /**
     * Hashes a password with a randomly generated salt.
     *
     * @param password The password to hash.
     * @return A string containing the salt and hash, separated by a colon.
     */
    public static String hashPassword(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_SIZE];
        random.nextBytes(salt);

        byte[] hash = pbkdf2(password.toCharArray(), salt, ITERATIONS, HASH_SIZE);

        return toHex(salt) + ":" + toHex(hash);
    }

    /**
     * Verifies a password against a stored hash.
     *
     * @param password       The password to verify.
     * @param storedPassword The stored password hash (including the salt).
     * @return True if the password is correct, false otherwise.
     */
    public static boolean verifyPassword(String password, String storedPassword) throws NoSuchAlgorithmException, InvalidKeySpecException {
        if (storedPassword == null || !storedPassword.contains(":")) {
            return false;
        }
        String[] parts = storedPassword.split(":");
        if (parts.length != 2) {
            return false;
        }
        byte[] salt = fromHex(parts[0]);
        byte[] hash = fromHex(parts[1]);

        byte[] testHash = pbkdf2(password.toCharArray(), salt, ITERATIONS, HASH_SIZE);

        return Arrays.equals(hash, testHash);
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int bytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeySpec spec = new PBEKeySpec(password, salt, iterations, bytes * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
        return skf.generateSecret(spec).getEncoded();
    }

    private static String toHex(byte[] array) {
        StringBuilder sb = new StringBuilder(array.length * 2);
        for (byte b : array) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static byte[] fromHex(String hex) {
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }
}

