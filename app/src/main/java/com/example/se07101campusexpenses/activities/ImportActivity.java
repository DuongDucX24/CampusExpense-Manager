package com.example.se07101campusexpenses.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImportActivity extends AppCompatActivity {

    private static final int READ_REQUEST_CODE = 42;
    private EditText etImportText;
    private Button btnImportFromFile, btnImportFromText;
    private ExpenseRepository expenseRepository;
    private BudgetRepository budgetRepository;
    private int userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);

        etImportText = findViewById(R.id.etImportText);
        btnImportFromFile = findViewById(R.id.btnImportFromFile);
        btnImportFromText = findViewById(R.id.btnImportFromText);

        expenseRepository = new ExpenseRepository(this);
        budgetRepository = new BudgetRepository(this);
        userId = getSharedPreferences("user_prefs", MODE_PRIVATE).getInt("user_id", -1);

        btnImportFromFile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("text/plain");
            startActivityForResult(intent, READ_REQUEST_CODE);
        });

        btnImportFromText.setOnClickListener(v -> {
            String text = etImportText.getText().toString();
            if (!text.isEmpty()) {
                parseAndImport(text);
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
                    parseAndImport(content);
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

    private void parseAndImport(String text) {
        String cleanedText = cleanText(text);
        String[] lines = cleanedText.split("\n");
        boolean isPlus = false;
        boolean isMinus = false;
        StringBuilder errors = new StringBuilder();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;

            if (line.equalsIgnoreCase("Plus")) {
                isPlus = true;
                isMinus = false;
                continue;
            } else if (line.equalsIgnoreCase("Minus")) {
                isPlus = false;
                isMinus = true;
                continue;
            }

            boolean success = false;
            if (isPlus) {
                success = importBudget(line);
            } else if (isMinus) {
                success = importExpense(line);
            }

            if (!success) {
                errors.append("Error on line ").append(i + 1).append(": ").append(lines[i]).append("\n");
            }
        }

        if (errors.length() > 0) {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Import Errors")
                    .setMessage(errors.toString())
                    .setPositiveButton("OK", null)
                    .show();
        } else {
            Toast.makeText(this, "Import completed successfully", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private boolean importBudget(String line) {
        Pattern pattern = Pattern.compile("(\\d+(?:[.,]\\d+)*)k(?:\\s*\\((.*)\\))?");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            String amountStr = matcher.group(1).replaceAll("[.,]", "");
            double amount = Double.parseDouble(amountStr) * 1000;
            String description = matcher.group(2) != null ? matcher.group(2).trim() : "";

            Budget budget = new Budget();
            budget.setUserId(userId);
            budget.setName(description);
            budget.setAmount(amount);
            budget.setPeriod("Monthly"); // Default period
            budgetRepository.insert(budget);
            return true;
        } else {
            pattern = Pattern.compile("(\\d+(?:[.,]\\d+)*)đ(?:\\s*\\((.*)\\))?");
            matcher = pattern.matcher(line);
            if (matcher.find()) {
                String amountStr = matcher.group(1).replaceAll("[.,]", "");
                double amount = Double.parseDouble(amountStr);
                String description = matcher.group(2) != null ? matcher.group(2).trim() : "";

                Budget budget = new Budget();
                budget.setUserId(userId);
                budget.setName(description);
                budget.setAmount(amount);
                budget.setPeriod("Monthly"); // Default period
                budgetRepository.insert(budget);
                return true;
            }
        }
        return false;
    }

    private boolean importExpense(String line) {
        Pattern pattern = Pattern.compile("(\\d+(?:[.,]\\d+)*)k(?:\\s*\\((.*)\\))?");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            String amountStr = matcher.group(1).replaceAll("[.,]", "");
            double amount = Double.parseDouble(amountStr) * 1000;
            String description = matcher.group(2) != null ? matcher.group(2).trim() : "";

            Expense expense = new Expense();
            expense.setUserId(userId);
            expense.setAmount(amount);
            expense.setDescription(description);
            expense.setCategory("Imported"); // Default category
            // All imported expenses will have the current date
            expense.setDate(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()));
            expenseRepository.addExpense(expense);
            return true;
        } else {
            pattern = Pattern.compile("(\\d+(?:[.,]\\d+)*)đ(?:\\s*\\((.*)\\))?");
            matcher = pattern.matcher(line);
            if (matcher.find()) {
                String amountStr = matcher.group(1).replaceAll("[.,]", "");
                double amount = Double.parseDouble(amountStr);
                String description = matcher.group(2) != null ? matcher.group(2).trim() : "";

                Expense expense = new Expense();
                expense.setUserId(userId);
                expense.setAmount(amount);
                expense.setDescription(description);
                expense.setCategory("Imported"); // Default category
                // All imported expenses will have the current date
                expense.setDate(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()));
                expenseRepository.addExpense(expense);
                return true;
            }
        }
        return false;
    }

    private String cleanText(String text) {
        StringBuilder cleanedText = new StringBuilder();
        String[] lines = text.split("\n");
        for (String line : lines) {
            // Remove timestamps like [16:55]
            line = line.replaceAll("\\[\\d{2}:\\d{2}\\]", "").trim();
            cleanedText.append(line).append("\n");
        }
        return cleanedText.toString();
    }
}
