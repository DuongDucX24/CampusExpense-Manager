package com.example.se07101campusexpenses.security;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/**
 * Helper class for encrypting and decrypting sensitive user data.
 * Uses Android KeyStore for secure key management.
 */
public class EncryptionHelper {

    private static final String TAG = "EncryptionHelper";
    private static final String TRANSFORMATION = KeyProperties.KEY_ALGORITHM_AES + "/" +
            KeyProperties.BLOCK_MODE_GCM + "/" +
            KeyProperties.ENCRYPTION_PADDING_NONE;
    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    private static final String ALIAS_KEY = "CampusExpenseKey";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    /**
     * Encrypts the given text using AES-GCM with a key stored in the Android KeyStore.
     *
     * @param context Application context
     * @param text    Plain text to encrypt
     * @return Base64 encoded string of IV + encrypted data, or null if encryption failed
     */
    public static String encrypt(Context context, String text) {
        try {
            // Get or create the encryption key
            final Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            final SecretKey secretKey = getOrCreateSecretKey();
            
            // Initialize the cipher for encryption
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            
            // Get the IV that was randomly generated
            byte[] iv = cipher.getIV();
            
            // Encrypt the text
            byte[] encryptedBytes = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));
            
            // Concatenate IV and encrypted data, then encode as Base64
            byte[] combined = new byte[iv.length + encryptedBytes.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);
            
            return Base64.encodeToString(combined, Base64.DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, "Error encrypting data: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Decrypts the given encrypted text.
     *
     * @param context       Application context
     * @param encryptedText Base64 encoded string of IV + encrypted data
     * @return Decrypted plain text, or null if decryption failed
     */
    public static String decrypt(Context context, String encryptedText) {
        try {
            // Decode the combined IV and encrypted data from Base64
            byte[] combined = Base64.decode(encryptedText, Base64.DEFAULT);
            
            // Extract IV and encrypted data
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encryptedBytes = new byte[combined.length - GCM_IV_LENGTH];
            
            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(combined, GCM_IV_LENGTH, encryptedBytes, 0, encryptedBytes.length);
            
            // Initialize cipher for decryption
            final Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            final SecretKey secretKey = getOrCreateSecretKey();
            final GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
            
            // Decrypt the data
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            Log.e(TAG, "Error decrypting data: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Gets or creates a secret key for encryption/decryption.
     *
     * @return SecretKey from the Android KeyStore
     */
    private static SecretKey getOrCreateSecretKey() throws KeyStoreException,
            CertificateException, IOException, NoSuchAlgorithmException,
            UnrecoverableEntryException, NoSuchProviderException,
            InvalidAlgorithmParameterException {
        
        // Load the Android KeyStore
        final KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
        keyStore.load(null);
        
        // Check if our key already exists
        if (keyStore.containsAlias(ALIAS_KEY)) {
            // Retrieve existing key
            final KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry) keyStore.getEntry(
                    ALIAS_KEY, null);
            return secretKeyEntry.getSecretKey();
        } else {
            // Create a new key if one doesn't exist
            final KeyGenerator keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE);
            
            keyGenerator.init(new KeyGenParameterSpec.Builder(
                    ALIAS_KEY,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build());
            
            return keyGenerator.generateKey();
        }
    }
}
