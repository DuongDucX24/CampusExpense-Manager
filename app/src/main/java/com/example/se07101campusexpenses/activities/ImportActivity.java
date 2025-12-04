package com.example.se07101campusexpenses.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.se07101campusexpenses.R;
import com.example.se07101campusexpenses.database.AppDatabase;
import com.example.se07101campusexpenses.database.BudgetRepository;
import com.example.se07101campusexpenses.database.ExpenseRepository;
import com.example.se07101campusexpenses.model.Budget;
import com.example.se07101campusexpenses.model.Expense;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImportActivity extends AppCompatActivity {

    private static final int READ_REQUEST_CODE = 42;
    private static final int MAX_PREVIEW_ITEMS = 50;
    private EditText etImportText;
    private Button btnImportFromFile, btnImportFromText, btnReformat;
    private ExpenseRepository expenseRepository;
    private BudgetRepository budgetRepository;
    private int userId;
    private static final String TAG = "ImportActivity";

    // Regex patterns for cleaning and parsing
    private static final Pattern TIMESTAMP_PATTERN = Pattern.compile("\\[\\d{4}\\]");
    private static final Pattern IMAGE_PATTERN = Pattern.compile("\\d{8}_\\d{6}\\.(jpg|png|jpeg)", Pattern.CASE_INSENSITIVE);
    private static final Pattern STANDALONE_BRACKET_PATTERN = Pattern.compile("^(\\[\\d+\\]\\s*)+$");
    private static final Pattern SIGN_PATTERN = Pattern.compile("(?i)^(plus|minus)\\s+(.*)$");
    private static final Pattern AMOUNT_PATTERN = Pattern.compile("^([\\d.,]+)(k|đ|d)?", Pattern.CASE_INSENSITIVE);

    private enum EntryType { BUDGET, EXPENSE }

    private static class ParsedEntry {
        final EntryType type;
        final long amount;
        final String description;
        final String sourceLine;

        ParsedEntry(EntryType type, long amount, String description, String sourceLine) {
            this.type = type;
            this.amount = amount;
            this.description = description;
            this.sourceLine = sourceLine;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);

        etImportText = findViewById(R.id.etImportText);
        btnImportFromFile = findViewById(R.id.btnImportFromFile);
        btnImportFromText = findViewById(R.id.btnImportFromText);
        btnReformat = findViewById(R.id.btnReformat);

        expenseRepository = new ExpenseRepository(this);
        budgetRepository = new BudgetRepository(this);
        userId = getSharedPreferences("user_prefs", MODE_PRIVATE).getInt("user_id", -1);

        btnReformat.setOnClickListener(v -> {
            String text = etImportText.getText().toString();
            if (!text.isEmpty()) {
                String reformatted = reformatInput(text);
                etImportText.setText(reformatted);
                Toast.makeText(this, "Input reformatted", Toast.LENGTH_SHORT).show();
            }
        });

        btnImportFromFile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("text/plain");
            startActivityForResult(intent, READ_REQUEST_CODE);
        });

        btnImportFromText.setOnClickListener(v -> {
            String text = etImportText.getText().toString();
            if (!text.isEmpty()) {
                beginPreviewFlow(text);
            } else {
                Toast.makeText(this, "Text field is empty", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                try {
                    String content = readTextFromUri(uri);
                    String reformatted = reformatInput(content);
                    etImportText.setText(reformatted);
                    beginPreviewFlow(reformatted);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to read file", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private String readTextFromUri(Uri uri) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
        }
        return stringBuilder.toString();
    }

    private void beginPreviewFlow(String rawText) {
        List<String> errors = new ArrayList<>();
        List<ParsedEntry> entries = parseText(rawText, errors);
        if (entries.isEmpty()) {
            showErrors(errors.isEmpty() ? List.of("No valid entries found") : errors);
            return;
        }
        showPreviewDialog(entries, errors);
    }

    private List<ParsedEntry> parseText(String text, List<String> errors) {
        String cleanedText = cleanInput(text);
        String[] lines = cleanedText.split("\n");
        List<ParsedEntry> parsedEntries = new ArrayList<>();
        EntryType currentType = null;
        boolean isFirstEntry = true;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                continue;
            }

            // Skip standalone bracket entries like [1234]
            if (STANDALONE_BRACKET_PATTERN.matcher(line).matches()) {
                continue;
            }

            // Skip image filenames
            if (IMAGE_PATTERN.matcher(line).find() && !line.contains("(")) {
                continue;
            }

            // Check for Plus/Minus prefix
            String workingLine = line;
            boolean hasExplicitSign = false;

            Matcher signMatcher = SIGN_PATTERN.matcher(line);
            if (signMatcher.matches()) {
                String sign = signMatcher.group(1).toLowerCase(Locale.US);
                currentType = sign.equals("plus") ? EntryType.BUDGET : EntryType.EXPENSE;
                workingLine = signMatcher.group(2).trim();
                hasExplicitSign = true;
            } else if (line.toLowerCase(Locale.US).equals("plus")) {
                currentType = EntryType.BUDGET;
                continue; // Just a sign marker, no amount
            } else if (line.toLowerCase(Locale.US).equals("minus")) {
                currentType = EntryType.EXPENSE;
                continue; // Just a sign marker, no amount
            }

            // First entry must have explicit sign
            if (isFirstEntry && !hasExplicitSign) {
                errors.add("Line " + (i + 1) + ": First entry must have explicit Plus or Minus");
                continue;
            }

            // If no sign and not first, inherit from previous
            if (currentType == null) {
                errors.add("Line " + (i + 1) + ": Missing Plus/Minus context");
                continue;
            }

            // Parse the entry
            ParsedEntry entry = parseLine(workingLine, currentType, line, i + 1, errors);
            if (entry != null) {
                parsedEntries.add(entry);
                isFirstEntry = false;
            }
        }
        return parsedEntries;
    }

    private ParsedEntry parseLine(String line, EntryType type, String sourceLine, int lineNumber, List<String> errors) {
        // Extract description from parentheses (open or closed)
        String description = extractDescription(line);

        // Get the amount part (everything before the first '(' or the whole line)
        int parenIndex = line.indexOf('(');
        String amountPart = parenIndex > 0 ? line.substring(0, parenIndex).trim() : line.trim();

        // Clean the amount part - remove any non-amount text
        amountPart = cleanAmountString(amountPart);

        // Parse the amount
        long amount = parseAmount(amountPart);
        if (amount <= 0) {
            errors.add("Line " + lineNumber + ": Could not parse amount in '" + sourceLine + "'");
            return null;
        }

        return new ParsedEntry(type, amount, description != null ? description : "", sourceLine);
    }

    /**
     * Extracts description from parentheses - handles both (desc) and (desc without closing
     */
    private String extractDescription(String line) {
        int openIndex = line.indexOf('(');
        if (openIndex == -1) {
            return null;
        }

        int closeIndex = line.lastIndexOf(')');
        if (closeIndex > openIndex) {
            return line.substring(openIndex + 1, closeIndex).trim();
        } else {
            // Unclosed bracket - take everything after (
            return line.substring(openIndex + 1).trim();
        }
    }

    /**
     * Cleans the amount string by removing non-amount text
     */
    private String cleanAmountString(String amountPart) {
        // Remove timestamps like [1234]
        amountPart = TIMESTAMP_PATTERN.matcher(amountPart).replaceAll("");

        // Remove extra text after the amount (like "on 07-04-2025")
        Matcher matcher = AMOUNT_PATTERN.matcher(amountPart.trim());
        if (matcher.find()) {
            return matcher.group(0);
        }
        return amountPart.trim();
    }

    /**
     * Parses amount string to long value
     * Handles: 5k, 5.5k, 5000đ, 5.083đ, 12.771.000đ
     * Treats . and , as thousands separators
     */
    private long parseAmount(String amountStr) {
        if (amountStr == null || amountStr.isEmpty()) {
            return 0;
        }

        amountStr = amountStr.toLowerCase(Locale.US).trim();

        // Check for k suffix (multiply by 1000)
        boolean hasK = amountStr.endsWith("k");

        // Remove currency suffixes
        amountStr = amountStr.replaceAll("[đd]$", "");
        if (hasK) {
            amountStr = amountStr.substring(0, amountStr.length() - 1);
        }

        // Remove thousands separators (both . and ,)
        amountStr = amountStr.replace(".", "").replace(",", "");

        if (amountStr.isEmpty()) {
            return 0;
        }

        try {
            long value = Long.parseLong(amountStr);
            if (hasK) {
                value *= 1000;
            }
            return value;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void showPreviewDialog(List<ParsedEntry> entries, List<String> errors) {
        try {
            LayoutInflater inflater = LayoutInflater.from(this);
            View view = inflater.inflate(R.layout.dialog_import_preview, null);
            LinearLayout container = view.findViewById(R.id.previewContainer);
            TextView tvErrors = view.findViewById(R.id.tvPreviewErrors);

            if (container == null) {
                Log.e(TAG, "Preview container is null. Cannot show preview.");
                showErrors(List.of("An unexpected error occurred while preparing the preview."));
                return;
            }

            // Count budgets and expenses
            int budgetCount = 0;
            int expenseCount = 0;
            for (ParsedEntry entry : entries) {
                if (entry.type == EntryType.BUDGET) budgetCount++;
                else expenseCount++;
            }

            // Add summary header for large imports
            if (entries.size() > MAX_PREVIEW_ITEMS) {
                TextView summaryView = new TextView(this);
                summaryView.setText(String.format(Locale.getDefault(),
                    "📊 Import Summary:\n• %d budgets\n• %d expenses\n• %d total entries\n\nShowing first %d items:",
                    budgetCount, expenseCount, entries.size(), MAX_PREVIEW_ITEMS));
                summaryView.setTextSize(14);
                summaryView.setPadding(0, 0, 0, 24);
                container.addView(summaryView);
            }

            // Only show first MAX_PREVIEW_ITEMS to prevent OOM
            int itemsToShow = Math.min(entries.size(), MAX_PREVIEW_ITEMS);
            for (int i = 0; i < itemsToShow; i++) {
                ParsedEntry entry = entries.get(i);
                View row = inflater.inflate(R.layout.item_import_preview, container, false);
                TextView tvType = row.findViewById(R.id.tvPreviewType);
                TextView tvAmount = row.findViewById(R.id.tvPreviewAmount);
                TextView tvDescription = row.findViewById(R.id.tvPreviewDescription);
                tvType.setText(entry.type == EntryType.BUDGET ? "Budget" : "Expense");
                tvAmount.setText(String.format(Locale.getDefault(), "%,d đ", entry.amount));
                tvDescription.setText(TextUtils.isEmpty(entry.description) ? "(No description)" : entry.description);
                container.addView(row);
            }

            // Show how many more items if truncated
            if (entries.size() > MAX_PREVIEW_ITEMS) {
                TextView moreView = new TextView(this);
                moreView.setText(String.format(Locale.getDefault(),
                    "\n... and %d more entries", entries.size() - MAX_PREVIEW_ITEMS));
                moreView.setTextSize(14);
                moreView.setPadding(0, 16, 0, 0);
                container.addView(moreView);
            }

            if (tvErrors != null) {
                if (!errors.isEmpty()) {
                    tvErrors.setVisibility(View.VISIBLE);
                    // Limit error display to first 10 errors
                    int errorsToShow = Math.min(errors.size(), 10);
                    StringBuilder errorText = new StringBuilder();
                    for (int i = 0; i < errorsToShow; i++) {
                        errorText.append(errors.get(i)).append("\n");
                    }
                    if (errors.size() > 10) {
                        errorText.append(String.format(Locale.getDefault(), "... and %d more errors", errors.size() - 10));
                    }
                    tvErrors.setText(errorText.toString().trim());
                } else {
                    tvErrors.setVisibility(View.GONE);
                }
            }

            String dialogTitle = String.format(Locale.getDefault(), "Import Preview (%d items)", entries.size());
            new AlertDialog.Builder(this)
                    .setTitle(dialogTitle)
                    .setView(view)
                    .setPositiveButton("Import All", (dialog, which) -> persistEntries(entries))
                    .setNegativeButton("Cancel", null)
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing preview dialog", e);
            showErrors(List.of("Failed to show preview due to an internal error: " + e.getMessage()));
        }
    }

    private void persistEntries(List<ParsedEntry> entries) {
        // Show progress dialog for large imports
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Importing Data");
        progressDialog.setMessage("Please wait...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(entries.size());
        progressDialog.setProgress(0);
        progressDialog.setCancelable(false);
        progressDialog.show();

        AppDatabase.databaseWriteExecutor.execute(() -> {
            int budgets = 0;
            int expenses = 0;
            int failed = 0;
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String currentDate = dateFormat.format(new Date());

            for (int i = 0; i < entries.size(); i++) {
                ParsedEntry entry = entries.get(i);
                try {
                    if (entry.type == EntryType.BUDGET) {
                        Budget budget = new Budget();
                        budget.setUserId(userId);
                        budget.setName(entry.description);
                        budget.setAmount(entry.amount);
                        budget.setPeriod("Monthly");
                        budgetRepository.insertSync(budget);
                        budgets++;
                    } else {
                        Expense expense = new Expense();
                        expense.setUserId(userId);
                        expense.setAmount(entry.amount);
                        expense.setDescription(entry.description);
                        expense.setCategory("Imported");
                        expense.setDate(currentDate);
                        expenseRepository.addExpenseSync(expense);
                        expenses++;
                    }
                } catch (Exception e) {
                    failed++;
                    Log.w(TAG, "Failed to persist entry: " + entry.sourceLine, e);
                }

                // Update progress every 10 items to reduce UI overhead
                if (i % 10 == 0 || i == entries.size() - 1) {
                    int progress = i + 1;
                    runOnUiThread(() -> {
                        progressDialog.setProgress(progress);
                        progressDialog.setMessage(String.format(Locale.getDefault(),
                            "Importing %d of %d...", progress, entries.size()));
                    });
                }
            }

            int finalBudgets = budgets;
            int finalExpenses = expenses;
            int finalFailed = failed;
            runOnUiThread(() -> {
                progressDialog.dismiss();
                String message = String.format(Locale.getDefault(),
                    "Import complete!\n• %d budgets added\n• %d expenses added",
                    finalBudgets, finalExpenses);
                if (finalFailed > 0) {
                    message += String.format(Locale.getDefault(), "\n• %d failed", finalFailed);
                }
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                Log.i(TAG, "Import complete: budgets=" + finalBudgets + ", expenses=" + finalExpenses + ", failed=" + finalFailed);
                finish();
            });
        });
    }

    private void showErrors(List<String> errors) {
        new AlertDialog.Builder(this)
                .setTitle("Import Errors")
                .setMessage(TextUtils.join("\n", errors))
                .setPositiveButton("OK", null)
                .show();
    }

    private String reformatInput(String text) {
        StringBuilder output = new StringBuilder();
        String cleaned = cleanInput(text);
        String[] lines = cleaned.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty()) {
                output.append(line).append("\n");
            }
        }
        return output.toString().trim();
    }

    /**
     * Cleans input text by removing unwanted patterns
     */
    private String cleanInput(String text) {
        StringBuilder cleanedText = new StringBuilder();
        String[] lines = text.split("\n");

        for (String line : lines) {
            String sanitized = line;

            // Remove timestamps like [1234], [0555], etc.
            sanitized = TIMESTAMP_PATTERN.matcher(sanitized).replaceAll("");

            // Remove image filenames like 20250903_155805.jpg
            sanitized = IMAGE_PATTERN.matcher(sanitized).replaceAll("");

            // Remove bullet points
            sanitized = sanitized.replace("\u2022", "");

            // Trim whitespace
            sanitized = sanitized.trim();

            // Skip empty lines and standalone bracket entries
            if (!sanitized.isEmpty() && !STANDALONE_BRACKET_PATTERN.matcher(sanitized).matches()) {
                cleanedText.append(sanitized).append("\n");
            }
        }
        return cleanedText.toString();
    }
}
