## Plan: Import Flow Preview & Logging

### Goals
- Let inline commands like "Plus 500k" / "Minus 500k" import successfully.
- Offer a reformat button (Option C: automatic clean-up after every input source) plus a manual preview before commit.
- Log parsing + import outcomes for debugging and support field reports.

### Steps
1. **Parser Overhaul (`ImportActivity`)**
   - Accept inline and block `Plus`/`Minus` markers, supporting `k`, `đ`, `__đ__`, `VND`, commas/periods as separators.
   - Funnel line parsing through a shared helper that returns `ParsedEntry` objects (type, amount, description, source line).
2. **Input Normalization**
   - Implement `reformatInput(text)` that strips timestamps, converts currency tokens, normalizes parentheses, removes duplicate separators, and ensures each logical entry is on its own line.
   - Trigger this automatically after file selection or text paste (Option C), and expose a `Reformat` button for manual reruns.
3. **Preview UX**
   - After parsing, show a modal dialog (RecyclerView) listing valid entries (budgets vs expenses) and inline errors; allow the user to confirm or cancel before data hits Room.
   - Highlight lines that will be skipped with tooltips or badges referencing the error message.
4. **Persistence Execution**
   - On confirmation, iterate parsed entries and call `BudgetRepository.insert` / `ExpenseRepository.addExpense` inside `AppDatabase.databaseWriteExecutor`; keep imported budgets monthly and expenses dated with `new Date()`.
5. **Logging & Telemetry**
   - Add structured `Log.i/w` statements summarizing counts and individual failures; optionally persist a rolling import log (shared prefs or lightweight table) for later inspection.
6. **Validation**
   - Test against provided samples (mixed currencies, timestamps, inline commands) to ensure budgets/expenses land in DB and errors surface clearly.
   - Document quick manual test steps for QA or future regression testing.

