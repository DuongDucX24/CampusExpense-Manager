## Plan: Full Codebase Audit and Critical Fixes

### Status: ✅ COMPLETED - BUILD SUCCESSFUL

### Issues Found and Fixed:

1.  **CRITICAL: Regex Pattern Error in ImportActivity.java (Line 194)**
    - **Problem:** Unmatched closing `)` in regex pattern `"(\\d+[.,\\s]?)+(?:k|đ|d|vnd)?(?:(?:\\s*\\(.*))?)"` 
    - **Fix:** Changed to `"(\\d+[.,\\s]?)+(?:k|đ|d|vnd)?(?:\\s*\\(.*)?"`
    - **Impact:** This was causing compilation failure and app crashes on the preview button.

2.  **Escape Sequence Warning (Line 351)**
    - **Problem:** `\s` used outside of text blocks in `replaceAll("\s+", " ")`
    - **Fix:** Changed to `replaceAll("\\s+", " ")`

3.  **Unused Imports Cleaned Up**
    - Removed: `android.content.DialogInterface`, `android.view.ViewGroup`, `androidx.annotation.NonNull`

### Verification:
- ✅ Dependency Verification: `ConstraintLayout` and `CardView` are correctly declared
- ✅ Layout Integrity: All XML layouts have correct IDs and constraints
- ✅ Build Test: `./gradlew.bat :app:assembleDebug` completed successfully (33 tasks, 14 executed)

### Remaining Warnings (non-blocking):
- Room model constructors (User.java, Budget.java, CategorySum.java) - cosmetic warnings
- Deprecated `startActivityForResult` API - can be updated to modern Activity Result API later
- Some hardcoded strings in layouts - can be moved to strings.xml later

