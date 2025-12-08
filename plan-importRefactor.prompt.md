## Plan: Comprehensive Import Function Refactor

### Status: ✅ IMPLEMENTED - BUILD SUCCESSFUL

---

### Parsing Rules Implemented

| Rule | Implementation |
|------|----------------|
| **Sign Inheritance** | Entries without `Plus`/`Minus` inherit the sign from the previous entry |
| **First Entry Validation** | First entry MUST have explicit `Plus` or `Minus` - throws error if missing |
| **Date** | Default to current import date (December 4, 2025) |
| **Currency Parsing** | `5k` = 5,000 • `5000đ` = 5,000 • `5.083đ` = 5,083 • `5.5k` = 5,500 • `12.771.000đ` = 12,771,000 |
| **Separators** | Both `.` and `,` are thousands separators (no decimals) |
| **Description** | Text inside `(...)` - can be unclosed `(desc` or closed `(desc)` |
| **Empty Description** | Entries without `(` have empty description |
| **Remove Timestamps** | Strips `[HHMM]` patterns like `[1235]`, `[0555]` |
| **Remove Image Files** | Strips patterns like `20250903_155805.jpg` |
| **Remove Standalone Brackets** | Skips lines that are only `[xxxx]` entries |
| **Category** | All imports use "Imported" category |

---

### Regex Patterns Used

```java
// Timestamp removal - matches [1234], [0555], etc.
Pattern TIMESTAMP_PATTERN = Pattern.compile("\\[\\d{4}\\]");

// Image filename removal - matches 20250903_155805.jpg
Pattern IMAGE_PATTERN = Pattern.compile("\\d{8}_\\d{6}\\.(jpg|png|jpeg)", Pattern.CASE_INSENSITIVE);

// Standalone bracket line detection - matches lines with only [xxxx] entries
Pattern STANDALONE_BRACKET_PATTERN = Pattern.compile("^(\\[\\d+\\]\\s*)+$");

// Amount extraction - supports k, đ, d suffixes with . and , as thousands sep
Pattern AMOUNT_PATTERN = Pattern.compile("^([\\d.,]+)(k|đ|d)?", Pattern.CASE_INSENSITIVE);

// Plus/Minus detection
Pattern SIGN_PATTERN = Pattern.compile("(?i)^(plus|minus)\\s+(.*)$");
```

---

### Methods Implemented

1. **`cleanInput(String text)`** - Removes timestamps, image filenames, bullet points, standalone brackets
2. **`parseText(String text, List<String> errors)`** - Main parser with sign inheritance and first entry validation
3. **`parseLine(String line, EntryType type, String sourceLine, int lineNumber, List<String> errors)`** - Parses individual entries
4. **`extractDescription(String line)`** - Extracts text from parentheses (open or closed)
5. **`cleanAmountString(String amountPart)`** - Removes non-amount text before parsing
6. **`parseAmount(String amountStr)`** - Converts amount string to long value

---

### Sample Data Parsing

Input:
```
Plus 75k
100k
Minus 59k
2k (bicycle keeping fee
12.771.000đ (Money for Semester 5
```

Output:
| Amount | Type | Description |
|--------|------|-------------|
| 75,000 | Budget | (empty) |
| 100,000 | Budget | (empty) |
| 59,000 | Expense | (empty) |
| 2,000 | Expense | bicycle keeping fee |
| 12,771,000 | Expense | Money for Semester 5 |

---

### Error Handling

| Error Case | Message |
|------------|---------|
| First entry without sign | "Line 1: First entry must have explicit Plus or Minus" |
| Invalid amount format | "Line X: Could not parse amount in 'original line'" |
| Zero amount | Skipped silently |

---

### Files Modified

1. **ImportActivity.java** - Complete refactor of parsing logic

