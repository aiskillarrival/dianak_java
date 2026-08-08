package com.ers.util;

import at.favre.lib.crypto.bcrypt.BCrypt;

/**
 * Utility for hashing and verifying passwords using BCrypt.
 */
public class PasswordUtil {

    private static final int BCRYPT_COST = 12;

    /**
     * Hashes a plaintext password using BCrypt.
     */
    public static String hash(String plaintext) {
        return BCrypt.withDefaults().hashToString(BCRYPT_COST, plaintext.toCharArray());
    }

    /**
     * Verifies a plaintext password against a BCrypt hash.
     */
    public static boolean verify(String plaintext, String hash) {
        var result = BCrypt.verifyer().verify(plaintext.toCharArray(), hash);
        return result.verified;
    }
}
