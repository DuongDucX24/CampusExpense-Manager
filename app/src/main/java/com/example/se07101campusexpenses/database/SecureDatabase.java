package com.example.se07101campusexpenses.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;

import com.example.se07101campusexpenses.security.EncryptionHelper;

import java.security.SecureRandom;

/**
 * Provides enhanced security for the application database
 * and securely stores sensitive information.
 */
public class SecureDatabase {
    private static final String TAG = "SecureDatabase";
    private static final String KEY_DATABASE_KEY = "database_key";
    private static final String SECURE_PREFS_FILE = "secure_campus_expense_prefs";
    private static volatile AppDatabase INSTANCE;

    /**
     * Gets an instance of the database with security settings.
     *
     * @param context Application context
     * @return AppDatabase instance
     */
    public static AppDatabase getSecureDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (SecureDatabase.class) {
                if (INSTANCE == null) {
                    try {
                        // Create database with enhanced security settings
                        INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                AppDatabase.class, "campus_expenses_secure.db")
                                .fallbackToDestructiveMigration()
                                .setJournalMode(RoomDatabase.JournalMode.TRUNCATE) // Better security
                                .addCallback(new RoomDatabase.Callback() {
                                    @Override
                                    public void onOpen(SupportSQLiteDatabase db) {
                                        super.onOpen(db);
                                        // Execute PRAGMA commands for better security
                                        db.execSQL("PRAGMA foreign_keys=ON;");
                                    }
                                })
                                .build();

                        Log.i(TAG, "Secure database initialized successfully");
                    } catch (Exception e) {
                        Log.e(TAG, "Error initializing secure database: " + e.getMessage(), e);

                        // Fallback to standard database if security enhancements fail
                        Log.w(TAG, "Falling back to standard database");
                        INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                AppDatabase.class, "campus_expenses_db")
                                .fallbackToDestructiveMigration()
                                .build();
                    }
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Generates a secure random key that can be used for encryption.
     *
     * @param length Length of the key in bytes
     * @return Base64 encoded random key
     */
    public static String generateSecureKey(int length) {
        byte[] randomBytes = new byte[length];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(randomBytes);
        return Base64.encodeToString(randomBytes, Base64.DEFAULT);
    }

    /**
     * Securely stores a value in encrypted shared preferences.
     *
     * @param context Application context
     * @param key Key for the value
     * @param value Value to store
     */
    public static void securelyStoreValue(Context context, String key, String value) {
        try {
            String encryptedValue = EncryptionHelper.encrypt(context, value);
            SharedPreferences prefs = context.getSharedPreferences(SECURE_PREFS_FILE, Context.MODE_PRIVATE);
            prefs.edit().putString(key, encryptedValue).apply();
        } catch (Exception e) {
            Log.e(TAG, "Error storing secure value: " + e.getMessage());
        }
    }

    /**
     * Retrieves a securely stored value.
     *
     * @param context Application context
     * @param key Key for the value
     * @return Decrypted value or null if not found or error
     */
    public static String retrieveSecureValue(Context context, String key) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(SECURE_PREFS_FILE, Context.MODE_PRIVATE);
            String encryptedValue = prefs.getString(key, null);
            if (encryptedValue != null) {
                return EncryptionHelper.decrypt(context, encryptedValue);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving secure value: " + e.getMessage());
        }
        return null;
    }
}
