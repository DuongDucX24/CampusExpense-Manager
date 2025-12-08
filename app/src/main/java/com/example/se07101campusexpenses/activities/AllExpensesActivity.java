package com.example.se07101campusexpenses.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.se07101campusexpenses.R;
import com.example.se07101campusexpenses.adapter.ExpenseAdapter;
import com.example.se07101campusexpenses.database.AppDatabase;
import com.example.se07101campusexpenses.database.ExpenseRepository;
import com.example.se07101campusexpenses.model.Expense;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class AllExpensesActivity extends AppCompatActivity {

    private static final String TAG = "AllExpensesActivity";
    private static final String PREF_EXPENSE_SORT = "expense_sort_order";

    // Sort order constants
    private static final int SORT_AMOUNT_LOW_HIGH = 0;
    private static final int SORT_AMOUNT_HIGH_LOW = 1;
    private static final int SORT_NAME_A_Z = 2;
    private static final int SORT_NAME_Z_A = 3;

    private ExpenseAdapter expenseAdapter;
    private final List<Expense> allExpenses = new ArrayList<>();
    private List<Expense> filteredExpenses = new ArrayList<>();
    private ExpenseRepository expenseRepository;
    private int userId;
    private int currentSortOrder = SORT_AMOUNT_HIGH_LOW;
    private EditText etSearchExpense;
    private Spinner spinnerSortExpense;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_expenses);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.title_all_expenses));
        }

        // Get user ID
        userId = getSharedPreferences("user_prefs", Context.MODE_PRIVATE).getInt("user_id", -1);
        expenseRepository = new ExpenseRepository(this);

        // Load saved sort preference
        SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        currentSortOrder = prefs.getInt(PREF_EXPENSE_SORT, SORT_AMOUNT_HIGH_LOW);

        // Initialize views
        etSearchExpense = findViewById(R.id.etSearchExpense);
        spinnerSortExpense = findViewById(R.id.spinnerSortExpense);

        // Setup sort spinner
        setupSortSpinner();

        // Setup search
        setupSearch();

        // Set up RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerViewAllExpenses);
        expenseAdapter = new ExpenseAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(expenseAdapter);

        // Set up click listener
        expenseAdapter.setOnItemClickListener(expense -> {
            // Handle expense item click if needed
        });

        // Load all expenses
        loadAllExpenses();
    }

    private void setupSortSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.sort_options,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSortExpense.setAdapter(adapter);
        spinnerSortExpense.setSelection(currentSortOrder);

        spinnerSortExpense.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != currentSortOrder) {
                    currentSortOrder = position;
                    saveSortPreference();
                    sortExpenses();
                    expenseAdapter.submitExpenseList(filteredExpenses);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupSearch() {
        etSearchExpense.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filterExpenses(s.toString());
            }
        });
    }

    private void saveSortPreference() {
        getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                .edit()
                .putInt(PREF_EXPENSE_SORT, currentSortOrder)
                .apply();
    }

    private void sortExpenses() {
        switch (currentSortOrder) {
            case SORT_AMOUNT_LOW_HIGH:
                Collections.sort(filteredExpenses, (a, b) -> Double.compare(a.getAmount(), b.getAmount()));
                break;
            case SORT_AMOUNT_HIGH_LOW:
                Collections.sort(filteredExpenses, (a, b) -> Double.compare(b.getAmount(), a.getAmount()));
                break;
            case SORT_NAME_A_Z:
                Collections.sort(filteredExpenses, (a, b) ->
                        a.getDescription().compareToIgnoreCase(b.getDescription()));
                break;
            case SORT_NAME_Z_A:
                Collections.sort(filteredExpenses, (a, b) ->
                        b.getDescription().compareToIgnoreCase(a.getDescription()));
                break;
        }
    }

    private void filterExpenses(String query) {
        if (query.isEmpty()) {
            filteredExpenses.clear();
            filteredExpenses.addAll(allExpenses);
        } else {
            String lowerCaseQuery = query.toLowerCase(Locale.ROOT);
            filteredExpenses = allExpenses.stream()
                    .filter(expense ->
                            expense.getDescription().toLowerCase(Locale.ROOT).contains(lowerCaseQuery) ||
                                    expense.getCategory().toLowerCase(Locale.ROOT).contains(lowerCaseQuery) ||
                                    String.valueOf((long) expense.getAmount()).contains(query))
                    .collect(Collectors.toList());
        }
        sortExpenses();
        expenseAdapter.submitExpenseList(filteredExpenses);
    }

    private void loadAllExpenses() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // Get current month's bounds
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                // Set to first day of month
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                String startDate = dateFormat.format(calendar.getTime());

                // Set to last day of month
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                String endDate = dateFormat.format(calendar.getTime());

                // Get expenses for current month
                List<Expense> expenses = expenseRepository.getExpensesBetweenDatesForUser(startDate, endDate, userId);

                if (expenses != null) {
                    allExpenses.clear();
                    allExpenses.addAll(expenses);
                    filteredExpenses.clear();
                    filteredExpenses.addAll(expenses);

                    runOnUiThread(() -> {
                        sortExpenses();
                        expenseAdapter.submitExpenseList(filteredExpenses);
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading expenses: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    Toast.makeText(AllExpensesActivity.this, getString(R.string.error_loading_data), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAllExpenses();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
