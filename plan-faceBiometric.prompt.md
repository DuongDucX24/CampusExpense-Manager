## Plan: Face Biometric Authentication with Xiaomi HyperOS 2.0 Compatibility

### Status: ✅ IMPLEMENTED

### Problem
On Xiaomi devices running HyperOS 2.0, when **only face unlock is enrolled** (no fingerprint):
- `BiometricManager.canAuthenticate(BIOMETRIC_WEAK)` may return incorrect status codes
- Face unlock doesn't properly register with the Android Biometric framework when fingerprint is removed
- This is a known Xiaomi HyperOS quirk

### Solution
Use `BIOMETRIC_WEAK | DEVICE_CREDENTIAL` which:
1. Allows both weak (face) and strong (fingerprint) biometrics
2. Falls back to device PIN/password/pattern if biometrics fail
3. Has better compatibility with Xiaomi's HyperOS implementation

### Changes Made:

1. **LoginActivity.java**
   - Updated `setAllowedAuthenticators()` to use `BIOMETRIC_WEAK | DEVICE_CREDENTIAL`
   - Removed `setNegativeButtonText()` (not allowed with `DEVICE_CREDENTIAL`)
   - Updated `canAuthenticate()` check to use combined flags
   - Added comment explaining Xiaomi HyperOS 2.0 compatibility

2. **ProfileFragment.java**
   - Updated `setupBiometricPrompt()` to use `BIOMETRIC_WEAK | DEVICE_CREDENTIAL`
   - Removed `setNegativeButtonText()` 
   - Updated `showDeleteConfirmationDialog()` biometric check to use combined flags
   - Added comment explaining Xiaomi HyperOS 2.0 compatibility

3. **ExportActivity.java**
   - Updated `setupBiometricPrompt()` to use `BIOMETRIC_WEAK | DEVICE_CREDENTIAL`
   - Removed `setNegativeButtonText()`
   - Updated `initiateExport()` biometric check to use combined flags
   - Added comment explaining Xiaomi HyperOS 2.0 compatibility

### Build Status: ✅ BUILD SUCCESSFUL

### Testing Notes
- Face recognition should now work on Xiaomi 14T Pro with HyperOS 2.0
- If face recognition fails, device PIN/password/pattern will be shown as fallback
- The biometric prompt will show available biometric methods based on device enrollment
