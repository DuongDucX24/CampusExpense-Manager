package com.example.se07101campusexpenses.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manages user session state including automatic session locking
 * after a period of inactivity (app backgrounded for 30+ seconds).
 */
public class SessionManager {
    private static final String PREFS_NAME = "user_prefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_SESSION_LOCKED = "session_locked";
    private static final String KEY_LAST_BACKGROUND_TIME = "last_background_time";
    private static final String KEY_LOCKED_USERNAME = "locked_username";

    // Session timeout: 30 seconds
    public static final long TIMEOUT_MS = 30 * 1000;

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Records the time when app goes to background
     */
    public void recordBackgroundTime() {
        prefs.edit()
            .putLong(KEY_LAST_BACKGROUND_TIME, System.currentTimeMillis())
            .apply();
    }

    /**
     * Checks if session should be locked based on background time
     * @return true if session was locked, false otherwise
     */
    public boolean checkAndLockIfTimeout() {
        long lastBackgroundTime = prefs.getLong(KEY_LAST_BACKGROUND_TIME, 0);
        int userId = prefs.getInt(KEY_USER_ID, -1);

        // Only check timeout if user is logged in and there's a recorded background time
        if (userId != -1 && lastBackgroundTime > 0) {
            long elapsedTime = System.currentTimeMillis() - lastBackgroundTime;
            if (elapsedTime >= TIMEOUT_MS) {
                lockSession();
                return true;
            }
        }
        return false;
    }

    /**
     * Locks the current session
     */
    public void lockSession() {
        prefs.edit()
            .putBoolean(KEY_SESSION_LOCKED, true)
            .apply();
    }

    /**
     * Unlocks the session after successful re-authentication
     */
    public void unlockSession() {
        prefs.edit()
            .putBoolean(KEY_SESSION_LOCKED, false)
            .putLong(KEY_LAST_BACKGROUND_TIME, 0)
            .apply();
    }

    /**
     * Checks if session is currently locked
     */
    public boolean isSessionLocked() {
        return prefs.getBoolean(KEY_SESSION_LOCKED, false);
    }

    /**
     * Checks if user is logged in
     */
    public boolean isLoggedIn() {
        return prefs.getInt(KEY_USER_ID, -1) != -1;
    }

    /**
     * Gets the current user ID
     */
    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }

    /**
     * Stores the username for locked session display
     */
    public void setLockedUsername(String username) {
        prefs.edit()
            .putString(KEY_LOCKED_USERNAME, username)
            .apply();
    }

    /**
     * Gets the stored username for locked session
     */
    public String getLockedUsername() {
        return prefs.getString(KEY_LOCKED_USERNAME, "");
    }

    /**
     * Clears background time when app comes to foreground
     */
    public void clearBackgroundTime() {
        prefs.edit()
            .putLong(KEY_LAST_BACKGROUND_TIME, 0)
            .apply();
    }

    /**
     * Full logout - clears all session data
     */
    public void logout() {
        prefs.edit()
            .remove(KEY_USER_ID)
            .remove(KEY_SESSION_LOCKED)
            .remove(KEY_LAST_BACKGROUND_TIME)
            .remove(KEY_LOCKED_USERNAME)
            .apply();
    }
}

