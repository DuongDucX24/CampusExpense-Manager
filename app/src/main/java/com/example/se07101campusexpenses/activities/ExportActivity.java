package com.example.se07101campusexpenses.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.example.se07101campusexpenses.R;
import com.example.se07101campusexpenses.database.AppDatabase;
import com.example.se07101campusexpenses.database.BudgetDao;
import com.example.se07101campusexpenses.database.ExpenseDao;
import com.example.se07101campusexpenses.database.RecurringExpenseDao;
import com.example.se07101campusexpenses.database.UserRepository;
import com.example.se07101campusexpenses.model.Budget;
import com.example.se07101campusexpenses.model.Expense;
import com.example.se07101campusexpenses.model.RecurringExpense;
import com.example.se07101campusexpenses.model.User;
import com.example.se07101campusexpenses.security.PasswordUtils;
import com.example.se07101campusexpenses.util.SessionManager;

import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;

public class ExportActivity extends AppCompatActivity {

    private static final int CREATE_FILE_REQUEST_CODE = 100;

    private RadioGroup rgExportFormat;
    private RadioButton rbFormatText, rbFormatCsv;
    private Spinner spDatePreset;
    private LinearLayout llCustomDateRange;
    private Button btnStartDate, btnEndDate, btnPreview, btnExport, btnBack;
    private EditText etSearchFilter;
    private CheckBox cbIncludeRecurring;
    private TextView tvExportSummary;

    private ExpenseDao expenseDao;
    private BudgetDao budgetDao;
    private RecurringExpenseDao recurringExpenseDao;
    private UserRepository userRepository;
    private int userId;

    private final SimpleDateFormat isoFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private final SimpleDateFormat displayFmt = new SimpleDateFormat("d/M/yyyy", Locale.US);

    private String startDate, endDate;
    private List<Expense> filteredExpenses = new ArrayList<>();
    private List<Budget> filteredBudgets = new ArrayList<>();
    private List<RecurringExpense> recurringExpenses = new ArrayList<>();

    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private SessionManager sessionManager;

    private String pendingExportContent;
    private String pendingMimeType;
    private String pendingFileExtension;

    private static final String[] DATE_PRESETS = {
            "This Week", "This Month", "This Year", "All Time", "Custom"
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);
        setContentView(R.layout.activity_export);

        initViews();
        initDatabase();
        setupListeners();
        setupBiometricPrompt();
        setupDatePresetSpinner();

        // Set default date range to "All Time"
        spDatePreset.setSelection(3);
    }

    private void initViews() {
        rgExportFormat = findViewById(R.id.rgExportFormat);
        rbFormatText = findViewById(R.id.rbFormatText);
        rbFormatCsv = findViewById(R.id.rbFormatCsv);
        spDatePreset = findViewById(R.id.spDatePreset);
        llCustomDateRange = findViewById(R.id.llCustomDateRange);
        btnStartDate = findViewById(R.id.btnStartDate);
        btnEndDate = findViewById(R.id.btnEndDate);
        btnPreview = findViewById(R.id.btnPreview);
        btnExport = findViewById(R.id.btnExport);
        btnBack = findViewById(R.id.btnBack);
        etSearchFilter = findViewById(R.id.etSearchFilter);
        cbIncludeRecurring = findViewById(R.id.cbIncludeRecurring);
        tvExportSummary = findViewById(R.id.tvExportSummary);
    }

    private void initDatabase() {
        AppDatabase db = AppDatabase.getInstance(this);
        expenseDao = db.expenseDao();
        budgetDao = db.budgetDao();
        recurringExpenseDao = db.recurringExpenseDao();
        userRepository = new UserRepository(this);
        userId = getSharedPreferences("user_prefs", MODE_PRIVATE).getInt("user_id", -1);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnStartDate.setOnClickListener(v -> showDatePicker(true));
        btnEndDate.setOnClickListener(v -> showDatePicker(false));

        btnPreview.setOnClickListener(v -> showPreviewDialog());
        btnExport.setOnClickListener(v -> initiateExport());

        etSearchFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                updateSummary();
            }
        });

        cbIncludeRecurring.setOnCheckedChangeListener((buttonView, isChecked) -> updateSummary());

        rgExportFormat.setOnCheckedChangeListener((group, checkedId) -> updateSummary());
    }

    private void setupDatePresetSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, DATE_PRESETS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDatePreset.setAdapter(adapter);

        spDatePreset.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = DATE_PRESETS[position];
                if (selected.equals("Custom")) {
                    llCustomDateRange.setVisibility(View.VISIBLE);
                    // Set default custom dates if not set
                    if (startDate == null) {
                        Calendar cal = Calendar.getInstance();
                        cal.add(Calendar.MONTH, -1);
                        startDate = isoFmt.format(cal.getTime());
                        btnStartDate.setText(displayFmt.format(cal.getTime()));
                    }
                    if (endDate == null) {
                        endDate = isoFmt.format(new Date());
                        btnEndDate.setText(displayFmt.format(new Date()));
                    }
                } else {
                    llCustomDateRange.setVisibility(View.GONE);
                    setPresetDateRange(selected);
                }
                updateSummary();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setPresetDateRange(String preset) {
        Calendar cal = Calendar.getInstance();
        endDate = isoFmt.format(cal.getTime());

        switch (preset) {
            case "This Week":
                cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
                startDate = isoFmt.format(cal.getTime());
                break;
            case "This Month":
                cal.set(Calendar.DAY_OF_MONTH, 1);
                startDate = isoFmt.format(cal.getTime());
                break;
            case "This Year":
                cal.set(Calendar.DAY_OF_YEAR, 1);
                startDate = isoFmt.format(cal.getTime());
                break;
            case "All Time":
                startDate = "1970-01-01";
                break;
        }
    }

    private void showDatePicker(boolean isStartDate) {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth);
                    String dateStr = isoFmt.format(selected.getTime());
                    String displayStr = displayFmt.format(selected.getTime());
                    if (isStartDate) {
                        startDate = dateStr;
                        btnStartDate.setText(displayStr);
                    } else {
                        endDate = dateStr;
                        btnEndDate.setText(displayStr);
                    }
                    updateSummary();
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void setupBiometricPrompt() {
        Executor executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                proceedWithExport();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(ExportActivity.this, "Biometric authentication failed.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(ExportActivity.this, "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
            }
        });

        // For Xiaomi HyperOS 2.0 compatibility, use BIOMETRIC_WEAK with DEVICE_CREDENTIAL fallback
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Authentication Required")
                .setSubtitle("Confirm your identity with fingerprint or face recognition")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK | BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .build();
    }

    private void updateSummary() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            // Get expenses
            List<Expense> allExpenses = expenseDao.getExpensesBetweenDatesForUser(startDate, endDate, userId);
            List<Budget> allBudgets = budgetDao.getBudgetsByUserId(userId);
            List<RecurringExpense> allRecurring = recurringExpenseDao.getAllRecurringExpensesByUserId(userId);

            // Apply text filter
            String filter = etSearchFilter.getText().toString().toLowerCase().trim();
            filteredExpenses = new ArrayList<>();
            filteredBudgets = new ArrayList<>();

            for (Expense e : allExpenses) {
                if (filter.isEmpty() ||
                        (e.getDescription() != null && e.getDescription().toLowerCase().contains(filter)) ||
                        (e.getCategory() != null && e.getCategory().toLowerCase().contains(filter))) {
                    filteredExpenses.add(e);
                }
            }

            for (Budget b : allBudgets) {
                if (filter.isEmpty() ||
                        (b.getName() != null && b.getName().toLowerCase().contains(filter)) ||
                        (b.getDescription() != null && b.getDescription().toLowerCase().contains(filter))) {
                    filteredBudgets.add(b);
                }
            }

            recurringExpenses = allRecurring;

            int expenseCount = filteredExpenses.size();
            int budgetCount = filteredBudgets.size();
            int recurringCount = cbIncludeRecurring.isChecked() ? recurringExpenses.size() : 0;

            String format = rbFormatText.isChecked() ? "Text" : "CSV";

            runOnUiThread(() -> {
                String summary = String.format(Locale.US,
                        "Format: %s\n%d budgets, %d expenses%s",
                        format, budgetCount, expenseCount,
                        recurringCount > 0 ? ", " + recurringCount + " recurring" : "");
                tvExportSummary.setText(summary);
            });
        });
    }

    private void showPreviewDialog() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            String content = generateExportContent();
            runOnUiThread(() -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Export Preview");

                ScrollView scrollView = new ScrollView(this);
                TextView textView = new TextView(this);
                textView.setPadding(32, 32, 32, 32);
                textView.setText(content);
                textView.setTextIsSelectable(true);
                scrollView.addView(textView);

                builder.setView(scrollView);
                builder.setPositiveButton("Close", null);
                builder.show();
            });
        });
    }

    private void initiateExport() {
        // Show authentication dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Authentication Required");
        builder.setMessage("Please authenticate to export your data.");

        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setHint("Enter password");
        builder.setView(input);

        builder.setPositiveButton("Confirm", (dialog, which) -> {
            String password = input.getText().toString();
            verifyPasswordAndExport(password);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        // Check if biometric or device credential authentication is available
        BiometricManager biometricManager = BiometricManager.from(this);
        int canAuthenticate = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_WEAK | BiometricManager.Authenticators.DEVICE_CREDENTIAL);
        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            builder.setNeutralButton("Use Biometrics", (dialog, which) -> biometricPrompt.authenticate(promptInfo));
        }

        builder.show();
    }

    private void verifyPasswordAndExport(String password) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            User user = userRepository.getUserById(userId);
            if (user != null) {
                try {
                    if (PasswordUtils.verifyPassword(password, user.getPassword())) {
                        runOnUiThread(this::proceedWithExport);
                    } else {
                        runOnUiThread(() -> Toast.makeText(this, "Incorrect password.", Toast.LENGTH_SHORT).show());
                    }
                } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(this, "Error verifying password.", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void proceedWithExport() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            pendingExportContent = generateExportContent();
            boolean isTextFormat = rbFormatText.isChecked();
            pendingMimeType = isTextFormat ? "text/plain" : "text/csv";
            pendingFileExtension = isTextFormat ? ".txt" : ".csv";

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            String filename = "export_" + timestamp + pendingFileExtension;

            runOnUiThread(() -> {
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType(pendingMimeType);
                intent.putExtra(Intent.EXTRA_TITLE, filename);
                startActivityForResult(intent, CREATE_FILE_REQUEST_CODE);
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sessionManager.checkAndLockIfTimeout()) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CREATE_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                writeExportToFile(uri);
            }
        }
    }

    private void writeExportToFile(Uri uri) {
        try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
            if (outputStream != null && pendingExportContent != null) {
                outputStream.write(pendingExportContent.getBytes());
                Toast.makeText(this, "Export successful!", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to write file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String generateExportContent() {
        if (rbFormatText.isChecked()) {
            return generateTextExport();
        } else {
            return generateCsvExport();
        }
    }

    private String generateTextExport() {
        StringBuilder sb = new StringBuilder();
        sb.append("// CampusExpense Manager Export\n");
        sb.append("// Date: ").append(isoFmt.format(new Date())).append("\n");
        sb.append("// Date Range: ").append(startDate).append(" to ").append(endDate).append("\n\n");

        // Export budgets
        if (!filteredBudgets.isEmpty()) {
            sb.append("// === BUDGETS ===\n");
            for (Budget b : filteredBudgets) {
                String amountStr = formatAmountForExport(b.getAmount());
                String desc = b.getDescription() != null && !b.getDescription().isEmpty()
                        ? b.getDescription() : b.getName();
                sb.append("Plus ").append(amountStr).append(" (").append(desc).append("\n");
            }
            sb.append("\n");
        }

        // Export expenses
        if (!filteredExpenses.isEmpty()) {
            sb.append("// === EXPENSES ===\n");
            for (Expense e : filteredExpenses) {
                String amountStr = formatAmountForExport(e.getAmount());
                String desc = e.getDescription() != null && !e.getDescription().isEmpty()
                        ? e.getDescription() : e.getCategory();
                sb.append("Minus ").append(amountStr).append(" (").append(desc).append("\n");
            }
            sb.append("\n");
        }

        // Export recurring expenses as comments
        if (cbIncludeRecurring.isChecked() && !recurringExpenses.isEmpty()) {
            sb.append("// === RECURRING EXPENSES ===\n");
            for (RecurringExpense r : recurringExpenses) {
                String amountStr = formatAmountForExport(r.getAmount());
                sb.append("// Recurring ").append(r.getFrequency()).append(": ")
                        .append(amountStr).append(" (").append(r.getDescription()).append(")\n");
            }
        }

        return sb.toString();
    }

    private String generateCsvExport() {
        StringBuilder sb = new StringBuilder();
        sb.append("Type,Amount,Description,Category,Date,Period,IsRecurring\n");

        // Export budgets
        for (Budget b : filteredBudgets) {
            sb.append("Budget,");
            sb.append(b.getAmount()).append(",");
            sb.append(escapeCsv(b.getDescription())).append(",");
            sb.append(escapeCsv(b.getName())).append(",");
            sb.append(","); // No date for budgets
            sb.append(escapeCsv(b.getPeriod())).append(",");
            sb.append("false\n");
        }

        // Export expenses
        for (Expense e : filteredExpenses) {
            sb.append("Expense,");
            sb.append(e.getAmount()).append(",");
            sb.append(escapeCsv(e.getDescription())).append(",");
            sb.append(escapeCsv(e.getCategory())).append(",");
            sb.append(e.getDate() != null ? e.getDate() : "").append(",");
            sb.append(","); // No period for expenses
            sb.append(e.isRecurring() ? "true" : "false").append("\n");
        }

        // Export recurring expenses
        if (cbIncludeRecurring.isChecked()) {
            for (RecurringExpense r : recurringExpenses) {
                sb.append("Recurring,");
                sb.append(r.getAmount()).append(",");
                sb.append(escapeCsv(r.getDescription())).append(",");
                sb.append(escapeCsv(r.getCategory())).append(",");
                sb.append(r.getStartDate() != null ? r.getStartDate() : "").append(",");
                sb.append(escapeCsv(r.getFrequency())).append(",");
                sb.append("true\n");
            }
        }

        return sb.toString();
    }

    private String formatAmountForExport(double amount) {
        if (amount >= 1000 && amount % 1000 == 0) {
            return ((long) amount / 1000) + "k";
        } else if (amount >= 1000) {
            // Format with k suffix but include decimals
            double kAmount = amount / 1000.0;
            if (kAmount == Math.floor(kAmount)) {
                return ((long) kAmount) + "k";
            } else {
                return String.format(Locale.US, "%.1fk", kAmount);
            }
        } else {
            return ((long) amount) + "đ";
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}

