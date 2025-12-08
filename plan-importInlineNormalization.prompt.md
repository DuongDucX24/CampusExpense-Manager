## Plan: Import Inline Normalization

Update the import parser and reformat pipeline in `app/src/main/java/com/example/se07101campusexpenses/activities/ImportActivity.java` so inline `Plus/Minus` tokens normalize into separate logical lines, ensure the reformat button and preview flow use the new helper, then document and run build/tests to confirm UI wiring and parsing work end to end.

### Steps
1. Analyze current `parseText`/`reformatInput` flows in `app/src/main/java/com/example/se07101campusexpenses/activities/ImportActivity.java` to map how Plus/Minus context propagates and where inline tokens slip through.
2. Design a normalization helper (e.g., `splitInlineCommands`) that rewrites lines like `Plus 500k (rent)` into `Plus` + amount lines before parsing, and integrate it inside `reformatInput` and any auto-clean path.
3. Adjust parser logic so when inline tokens remain (e.g., user skipped reformat), `parseText` still detects `Plus/Minus` prefixes on the same line and assigns the correct `EntryType`.
4. Update `activity_import.xml` (if needed) to confirm the reformat button is wired/visible, then ensure `btnReformat` invokes the enhanced normalization flow and triggers a lightweight toast/log for user feedback.
5. Run `./gradlew lintDebug` and `./gradlew testDebugUnitTest` (or equivalent) plus a manual import preview smoke test plan to verify UI wiring, parsing, and normalization; capture steps/results for future regressions.

### Further Considerations
1. Should inline normalization also auto-insert blank separators between entries, or keep original spacing? Option A strict split / Option B preserve user spacing.
2. Any sample import files we must include in tests to cover `k`, `đ`, parentheses, or timestamps?

