# Plan: Implement Session Lock with 30-Second Timeout

Add automatic session locking when the app is backgrounded for 30+ seconds, requiring biometric/password re-authentication while preserving the user's login state for quick unlock.

## TL;DR
Track app background time via `ActivityLifecycleCallbacks` in `MainApplication`. On resume, if 30+ seconds elapsed, set a `session_locked` flag and redirect to `LoginActivity` in lock mode—user re-authenticates via biometrics or password without re-entering username. Display toast message explaining the lock reason.

## Implementation Status: ✅ COMPLETED

## Steps Implemented

### 1. Added lifecycle tracking in MainApplication.java
- Implemented `ActivityLifecycleCallbacks` to track when app goes to foreground/background
- Records `last_background_time` in SharedPreferences when activity count reaches 0 (app goes to background)

### 2. Created SessionManager.java utility
- Location: `app/src/main/java/com/example/se07101campusexpenses/util/SessionManager.java`
- Constants: `TIMEOUT_MS = 30000` (30 seconds)
- Methods:
  - `recordBackgroundTime()` - Stores current time when app backgrounds
  - `checkAndLockIfTimeout()` - Returns true if timeout exceeded, locks session
  - `lockSession()` / `unlockSession()` - Manages session_locked flag
  - `isSessionLocked()` - Checks if session is locked
  - `setLockedUsername()` / `getLockedUsername()` - Stores username for lock screen

### 3. Modified LoginActivity.java
- Added lock mode detection: checks if `session_locked=true` and `user_id` exists
- Lock mode features:
  - Pre-fills username (read-only) from stored `locked_username`
  - Hides register button
  - Shows "Session Locked" title
  - Displays toast: "Session locked due to inactivity"
  - Updates login button text to "Unlock"
  - Updates biometric prompt title to "Unlock Session"
- On successful auth (password or biometric):
  - Calls `sessionManager.unlockSession()`
  - Stores username for future lock screens

### 4. Updated activity_login.xml
- Added `tvLoginTitle` TextView that displays "Login" or "Session Locked"

### 5. Added session check to MenuActivity.java onResume()
- Calls `SessionManager.checkAndLockIfTimeout()`
- If locked, redirects to `LoginActivity` with `FLAG_ACTIVITY_CLEAR_TOP`

### 6. Applied session check to all protected activities
Activities updated with SessionManager and onResume session check:
- ImportActivity
- ExportActivity
- AllExpensesActivity
- AllBudgetsActivity
- AddExpenseActivity
- EditExpenseActivity
- AddBudgetActivity
- EditBudgetActivity
- RecurringExpenseActivity

## Files Modified

1. `MainApplication.java` - Added ActivityLifecycleCallbacks
2. `SessionManager.java` - NEW FILE - Session management utility
3. `LoginActivity.java` - Lock mode handling
4. `activity_login.xml` - Added title TextView
5. `MenuActivity.java` - Session timeout check
6. `ImportActivity.java` - Session timeout check
7. `ExportActivity.java` - Session timeout check
8. `AllExpensesActivity.java` - Session timeout check
9. `AllBudgetsActivity.java` - Session timeout check
10. `AddExpenseActivity.java` - Session timeout check
11. `EditExpenseActivity.java` - Session timeout check
12. `AddBudgetActivity.java` - Session timeout check
13. `EditBudgetActivity.java` - Session timeout check
14. `RecurringExpenseActivity.java` - Session timeout check

## User Flow

1. User logs in normally → username stored for future lock screens
2. User backgrounds app for 30+ seconds
3. `MainApplication` records background time
4. User returns to app
5. Any activity's `onResume()` checks timeout
6. If timeout exceeded:
   - Session locked
   - Redirected to LoginActivity in lock mode
   - Toast: "Session locked due to inactivity"
   - Username pre-filled, password/biometric required
7. User authenticates
8. Session unlocked, user continues

## Security Features

- 30-second inactivity timeout (configurable in SessionManager.TIMEOUT_MS)
- Biometric or password required to unlock
- Username preserved for convenience (no need to re-enter)
- Toast notification explains lock reason
- All sensitive screens protected

