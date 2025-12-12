package com.example.se07101campusexpenses.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.se07101campusexpenses.R;
import com.example.se07101campusexpenses.database.AppDatabase;
import com.example.se07101campusexpenses.database.BudgetDao;
import com.example.se07101campusexpenses.database.ExpenseDao;
import com.example.se07101campusexpenses.database.ExpenseRepository;
import com.example.se07101campusexpenses.model.Budget;
import com.example.se07101campusexpenses.model.Expense;
import com.example.se07101campusexpenses.util.FormatUtils;
import com.example.se07101campusexpenses.util.SessionManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddExpenseActivity extends AppCompatActivity {

    private EditText edtExpenseDescription, edtExpenseAmount, edtExpenseDate;
    private EditText edtRecurringStart, edtRecurringEnd;
    private Spinner spinnerExpenseCategory, spBudgetSelect;
    private CheckBox cbRecurring;
    private ExpenseRepository expenseRepository;
    private int userId;

    private final SimpleDateFormat displayFmt = new SimpleDateFormat("d/M/yyyy", Locale.US);
    private final SimpleDateFormat isoFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    private final List<Budget> availableBudgets = new ArrayList<>();
    private final List<Budget> filteredBudgets = new ArrayList<>();
    private ArrayAdapter<String> budgetAdapter;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);
        setContentView(R.layout.activity_add_expense);

        expenseRepository = new ExpenseRepository(this);
        userId = getSharedPreferences("user_prefs", MODE_PRIVATE).getInt("user_id", -1);

        edtExpenseDescription = findViewById(R.id.edtExpenseDescription);
        edtExpenseAmount = findViewById(R.id.edtExpenseAmount);
        edtExpenseDate = findViewById(R.id.edtExpenseDate);
        spinnerExpenseCategory = findViewById(R.id.spinnerExpenseCategory);
        spBudgetSelect = findViewById(R.id.spBudgetSelect);
        cbRecurring = findViewById(R.id.cbRecurring);
        edtRecurringStart = findViewById(R.id.edtRecurringStart);
        edtRecurringEnd = findViewById(R.id.edtRecurringEnd);
        Button btnSaveExpense = findViewById(R.id.btnSaveExpense);
        Button btnBackExpense = findViewById(R.id.btnBackExpense);

        // Category Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.expense_categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerExpenseCategory.setAdapter(adapter);

        // Budget spinner adapter
        budgetAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        budgetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spBudgetSelect.setAdapter(budgetAdapter);

        // Apply dot-grouping formatter to amount
        FormatUtils.applyDotGroupingFormatter(edtExpenseAmount);

        // Date Pickers
        edtExpenseDate.setOnClickListener(v -> showDatePickerDialog(edtExpenseDate));
        edtRecurringStart.setOnClickListener(v -> showDatePickerDialog(edtRecurringStart));
        edtRecurringEnd.setOnClickListener(v -> showDatePickerDialog(edtRecurringEnd));

        cbRecurring.setOnCheckedChangeListener((buttonView, isChecked) -> {
            edtRecurringStart.setVisibility(isChecked ? android.view.View.VISIBLE : android.view.View.GONE);
            edtRecurringEnd.setVisibility(isChecked ? android.view.View.VISIBLE : android.view.View.GONE);
            // Hide single-date when recurring is on
            edtExpenseDate.setVisibility(isChecked ? android.view.View.GONE : android.view.View.VISIBLE);
            filterBudgetsForInputs();
        });

        edtExpenseAmount.addTextChangedListener(simpleWatcher);
        edtExpenseDate.addTextChangedListener(simpleWatcher);
        edtRecurringStart.addTextChangedListener(simpleWatcher);

        btnSaveExpense.setOnClickListener(v -> saveExpense());
        btnBackExpense.setOnClickListener(v -> finish());

        // Load budgets initially
        loadBudgets();
    }

    private final TextWatcher simpleWatcher = new TextWatcher() {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override public void afterTextChanged(Editable s) { filterBudgetsForInputs(); }
    };

    private void showDatePickerDialog(final EditText editText) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String selectedDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1;
                    editText.setText(selectedDate);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void loadBudgets() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            BudgetDao budgetDao = AppDatabase.getInstance(getApplicationContext()).budgetDao();
            List<Budget> userBudgets = budgetDao.getBudgetsByUserId(userId);
            availableBudgets.clear();
            if (userBudgets != null) availableBudgets.addAll(userBudgets);
            runOnUiThread(this::filterBudgetsForInputs);
        });
    }

    private void filterBudgetsForInputs() {
        String amountStr = edtExpenseAmount.getText().toString().trim();
        // Choose date source based on recurring
        String dateDisp = cbRecurring.isChecked() ? edtRecurringStart.getText().toString().trim() : edtExpenseDate.getText().toString().trim();
        budgetAdapter.clear();
        filteredBudgets.clear();
        if (TextUtils.isEmpty(amountStr) || TextUtils.isEmpty(dateDisp)) {
            budgetAdapter.notifyDataSetChanged();
            return;
        }
        // Strip grouping dots before parse
        String amountPlain = FormatUtils.stripGrouping(amountStr);
        double amount;
        try { amount = Double.parseDouble(amountPlain); } catch (NumberFormatException e) { return; }
        String dateIso = toIsoDate(dateDisp);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            ExpenseDao expenseDao = AppDatabase.getInstance(getApplicationContext()).expenseDao();
            List<String> names = new ArrayList<>();
            for (Budget b : availableBudgets) {
                String[] range = rangeForPeriodOnDate(b.getPeriod(), dateIso);
                Double spent = expenseDao.getTotalByBudgetBetweenDates(b.getId(), range[0], range[1]);
                double remaining = b.getAmount() - (spent != null ? spent : 0d);
                if (remaining >= amount) {
                    filteredBudgets.add(b);
                    names.add(b.getName() + " (" + b.getPeriod() + ")");
                }
            }
            runOnUiThread(() -> {
                budgetAdapter.clear();
                budgetAdapter.addAll(names);
                budgetAdapter.notifyDataSetChanged();
            });
        });
    }

    private void saveExpense() {
        String description = edtExpenseDescription.getText().toString().trim();
        String amountStr = edtExpenseAmount.getText().toString().trim();
        String dateDisplay = cbRecurring.isChecked() ? edtRecurringStart.getText().toString().trim() : edtExpenseDate.getText().toString().trim();
        String category = spinnerExpenseCategory.getSelectedItem() != null ? spinnerExpenseCategory.getSelectedItem().toString() : "";

        if (TextUtils.isEmpty(description) || TextUtils.isEmpty(amountStr) || TextUtils.isEmpty(dateDisplay) || TextUtils.isEmpty(category)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (spBudgetSelect.getAdapter() == null || spBudgetSelect.getAdapter().getCount() == 0) {
            Toast.makeText(this, "No budget can cover this expense. Adjust amount or date.", Toast.LENGTH_LONG).show();
            return;
        }

        String amountPlain = com.example.se07101campusexpenses.util.FormatUtils.stripGrouping(amountStr);
        double amount;
        try { amount = Double.parseDouble(amountPlain); } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount format", Toast.LENGTH_SHORT).show();
            return;
        }

        int pos = spBudgetSelect.getSelectedItemPosition();
        if (pos < 0 || pos >= filteredBudgets.size()) {
            Toast.makeText(this, "Please select a budget", Toast.LENGTH_SHORT).show();
            return;
        }
        Budget selectedBudget = filteredBudgets.get(pos);

        boolean isRecurring = cbRecurring.isChecked();
        String recStartIso = null, recEndIso = null;
        if (isRecurring) {
            String startDisp = edtRecurringStart.getText().toString().trim();
            String endDisp = edtRecurringEnd.getText().toString().trim();
            if (TextUtils.isEmpty(startDisp) || TextUtils.isEmpty(endDisp)) {
                Toast.makeText(this, "Please select recurring start and end dates", Toast.LENGTH_SHORT).show();
                return;
            }
            recStartIso = toIsoDate(startDisp);
            recEndIso = toIsoDate(endDisp);
            if (!isStartBeforeOrEqual(recStartIso, recEndIso)) {
                Toast.makeText(this, "Recurring start date must not be after end date", Toast.LENGTH_LONG).show();
                return;
            }
        }

        Expense expense = new Expense();
        expense.setDescription(description);
        expense.setAmount(amount);
        expense.setDate(toIsoDate(dateDisplay));
        expense.setCategory(category);
        expense.setUserId(userId);
        expense.setRecurring(isRecurring);
        expense.setRecurringStartDate(recStartIso);
        expense.setRecurringEndDate(recEndIso);
        expense.setBudgetId(selectedBudget.getId());

        AppDatabase.databaseWriteExecutor.execute(() -> {
            expenseRepository.addExpense(expense);
            runOnUiThread(() -> {
                Toast.makeText(this, "Expense saved successfully", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    private String toIsoDate(String input) {
        try { return isoFmt.format(displayFmt.parse(input)); } catch (ParseException e) { return input; }
    }

    private boolean isStartBeforeOrEqual(String startIso, String endIso) {
        try { return !isoFmt.parse(startIso).after(isoFmt.parse(endIso)); } catch (ParseException e) { return true; }
    }

    private String[] rangeForPeriodOnDate(String period, String isoDate) {
        Calendar cal = Calendar.getInstance();
        try { cal.setTime(isoFmt.parse(isoDate)); } catch (ParseException ignored) { }
        SimpleDateFormat sdf = isoFmt;
        if (period == null) period = "Monthly";
        switch (period.toLowerCase(Locale.US)) {
            case "daily": {
                String d = sdf.format(cal.getTime());
                return new String[]{d, d};
            }
            case "weekly": {
                int dow = cal.get(Calendar.DAY_OF_WEEK); // 1=Sunday
                int diffToMonday = (dow == Calendar.SUNDAY) ? -6 : (Calendar.MONDAY - dow);
                cal.add(Calendar.DAY_OF_MONTH, diffToMonday);
                String start = sdf.format(cal.getTime());
                cal.add(Calendar.DAY_OF_MONTH, 6);
                String end = sdf.format(cal.getTime());
                return new String[]{start, end};
            }
            case "monthly":
            default: {
                cal.set(Calendar.DAY_OF_MONTH, 1);
                String start = sdf.format(cal.getTime());
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                String end = sdf.format(cal.getTime());
                return new String[]{start, end};
            }
        }
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
}
