## Plan: Critical Fixes & Verification

1.  **Fix Profile Layout:** Wrap the `ProfileFragment`'s content in a `ConstraintLayout` inside the `ScrollView` to ensure the logout button remains anchored at the bottom but is always visible and accessible.
2.  **Diagnose and Fix Preview Crash:** Add robust `try-catch` blocks around the layout inflation and view binding in `ImportActivity`'s `showPreviewDialog` and log any exceptions to pinpoint the exact cause of the crash.
3.  **Dependency & Build Validation:** Run a clean build using `./gradlew.bat :app:clean :app:assembleDebug --info` to force a dependency refresh and get verbose output, which will help uncover any other lurking build issues.
4.  **Save Plan:** The plan will be saved to `plan-criticalFixes.prompt.md` for our records.

