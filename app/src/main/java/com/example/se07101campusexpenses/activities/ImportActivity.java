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
    private static final int MAX_PREVIEW_ITEMS = 50; // Limit preview items to prevent OOM
    private EditText etImportText;
    private Button btnImportFromFile, btnImportFromText, btnReformat;
    private ExpenseRepository expenseRepository;
    private BudgetRepository budgetRepository;
    private int userId;
    private static final String TAG = "ImportActivity";
    private static final Pattern INLINE_COMMAND_PATTERN = Pattern.compile("(?i)^(plus|minus)(?:\\s+(.*))?$");

    private enum EntryType { BUDGET, EXPENSE }

    private static class ParsedEntry {
        final EntryType type;
        final double amount;
        final String description;
        final String sourceLine;

        ParsedEntry(EntryType type, double amount, String description, String sourceLine) {
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
        String cleanedText = cleanText(text);
        List<String> lines = explodeInlineCommands(cleanedText);
        List<ParsedEntry> parsedEntries = new ArrayList<>();
        EntryType currentType = null;

        for (int i = 0; i < lines.size(); i++) {
            String originalLine = lines.get(i);
            String line = originalLine.trim();
            if (line.isEmpty()) {
                continue;
            }

            String lower = line.toLowerCase(Locale.US);
            if (lower.equals("plus")) {
                currentType = EntryType.BUDGET;
                continue;
            } else if (lower.equals("minus")) {
                currentType = EntryType.EXPENSE;
                continue;
            } else if (lower.startsWith("plus ")) {
                currentType = EntryType.BUDGET;
                line = line.substring(5).trim();
            } else if (lower.startsWith("minus ")) {
                currentType = EntryType.EXPENSE;
                line = line.substring(6).trim();
            }

            if (currentType == null) {
                errors.add("Line " + (i + 1) + ": Missing Plus/Minus context");
                continue;
            }

            ParsedEntry entry = parseLine(line, currentType, originalLine, i + 1, errors);
            if (entry != null) {
                parsedEntries.add(entry);
            }
        }
        return parsedEntries;
    }

    private ParsedEntry parseLine(String line, EntryType type, String sourceLine, int lineNumber, List<String> errors) {
        String normalized = normalizeCurrencyTokens(line);
        // Pattern to match amounts like "500k", "5000đ", "5.000", etc. with optional description in parentheses
        Pattern pattern = Pattern.compile("(\\d+[.,\\s]?)+(?:k|đ|d|vnd)?(?:\\s*\\(.*)?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(normalized);
        if (!matcher.find()) {
            errors.add("Line " + lineNumber + ": Could not parse amount in '" + sourceLine + "'");
            return null;
        }

        ParsedAmount amount = extractAmountAndDescription(normalized);
        if (amount == null) {
            errors.add("Line " + lineNumber + ": Invalid amount format in '" + sourceLine + "'");
            return null;
        }

        return new ParsedEntry(type, amount.value, amount.description, sourceLine);
    }

    private static class ParsedAmount {
        final double value;
        final String description;

        ParsedAmount(double value, String description) {
            this.value = value;
            this.description = description;
        }
    }

    private ParsedAmount extractAmountAndDescription(String line) {
        Pattern amountPattern = Pattern.compile("(\\d+(?:[.,\\s]\\d+)*)(k|đ|d|vnd)?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = amountPattern.matcher(line);
        if (!matcher.find()) {
            return null;
        }

        String rawNumber = matcher.group(1).replaceAll("[.,\\s]", "");
        double amount = Double.parseDouble(rawNumber);
        String suffix = matcher.group(2);
        if (suffix != null && suffix.equalsIgnoreCase("k")) {
            amount *= 1000;
        }

        String description = line.substring(matcher.end()).trim();
        if (description.startsWith("(")) {
            description = description.substring(1);
        }
        if (description.endsWith(")")) {
            description = description.substring(0, description.length() - 1);
        }
        return new ParsedAmount(amount, description);
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
                tvAmount.setText(String.format(Locale.getDefault(), "%.0f đ", entry.amount));
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
        for (String line : explodeInlineCommands(cleanText(text))) {
            String normalized = normalizeCurrencyTokens(line.trim());
            if (!normalized.isEmpty()) {
                output.append(normalized).append("\n");
            }
        }
        return output.toString().trim();
    }

    private String normalizeCurrencyTokens(String line) {
        return line
                .replace("__đ__", "đ")
                .replaceAll("(?i)vnd", "đ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String cleanText(String text) {
        StringBuilder cleanedText = new StringBuilder();
        String[] lines = text.split("\n");
        for (String line : lines) {
            String sanitized = line
                    .replaceAll("\\[\\d{2}:\\d{2}\\]", "")
                    .replaceAll("\\u2022", "") // bullet
                    .trim();
            if (!sanitized.isEmpty()) {
                cleanedText.append(sanitized).append("\n");
            }
        }
        return cleanedText.toString();
    }

    private List<String> explodeInlineCommands(String text) {
        List<String> result = new ArrayList<>();
        String[] rawLines = text.split("\n");
        for (String raw : rawLines) {
            String line = raw.trim();
            if (line.isEmpty()) {
                continue;
            }
            Matcher matcher = INLINE_COMMAND_PATTERN.matcher(line);
            if (matcher.matches()) {
                String keyword = capitalizeCommand(matcher.group(1));
                String remainder = matcher.group(2);
                result.add(keyword);
                if (!TextUtils.isEmpty(remainder)) {
                    result.add(remainder.trim());
                }
            } else {
                result.add(line);
            }
        }
        return result;
    }

    private String capitalizeCommand(String keyword) {
        if (TextUtils.isEmpty(keyword)) {
            return keyword;
        }
        String lower = keyword.toLowerCase(Locale.US);
        return lower.substring(0, 1).toUpperCase(Locale.US) + lower.substring(1);
    }
}
