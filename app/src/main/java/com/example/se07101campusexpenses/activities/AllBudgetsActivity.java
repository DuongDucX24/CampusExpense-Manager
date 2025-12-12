package com.example.se07101campusexpenses.activities;

import android.content.Context;
import android.content.Intent;
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
import com.example.se07101campusexpenses.adapter.BudgetRVAdapter;
import com.example.se07101campusexpenses.database.AppDatabase;
import com.example.se07101campusexpenses.database.BudgetRepository;
import com.example.se07101campusexpenses.model.Budget;
import com.example.se07101campusexpenses.util.SessionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class AllBudgetsActivity extends AppCompatActivity {

    private static final String TAG = "AllBudgetsActivity";
    private static final String PREF_BUDGET_SORT = "budget_sort_order";

    // Sort order constants
    private static final int SORT_AMOUNT_LOW_HIGH = 0;
    private static final int SORT_AMOUNT_HIGH_LOW = 1;
    private static final int SORT_NAME_A_Z = 2;
    private static final int SORT_NAME_Z_A = 3;

    private BudgetRVAdapter budgetAdapter;
    private final List<Budget> allBudgets = new ArrayList<>();
    private List<Budget> filteredBudgets = new ArrayList<>();
    private BudgetRepository budgetRepository;
    private int userId;
    private int currentSortOrder = SORT_AMOUNT_HIGH_LOW;
    private EditText etSearchBudget;
    private Spinner spinnerSortBudget;
    private View emptyView;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);
        setContentView(R.layout.activity_all_budgets);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.title_all_budgets));
        }

        // Get user ID
        userId = getSharedPreferences("user_prefs", Context.MODE_PRIVATE).getInt("user_id", -1);
        budgetRepository = new BudgetRepository(this);

        // Load saved sort preference
        SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        currentSortOrder = prefs.getInt(PREF_BUDGET_SORT, SORT_AMOUNT_HIGH_LOW);

        // Initialize views
        etSearchBudget = findViewById(R.id.etSearchBudget);
        spinnerSortBudget = findViewById(R.id.spinnerSortBudget);
        emptyView = findViewById(R.id.emptyBudgetsMessage);

        // Setup sort spinner
        setupSortSpinner();

        // Setup search
        setupSearch();

        // Set up RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerViewAllBudgets);
        budgetAdapter = new BudgetRVAdapter(filteredBudgets, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(budgetAdapter);

        // Set up click listener
        budgetAdapter.setOnClickListener((position, budget) -> {
            Intent intent = new Intent(AllBudgetsActivity.this, EditBudgetActivity.class);
            intent.putExtra("budget", budget);
            startActivity(intent);
        });

        // Load all budgets
        loadAllBudgets();
    }

    private void setupSortSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.sort_options,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSortBudget.setAdapter(adapter);
        spinnerSortBudget.setSelection(currentSortOrder);

        spinnerSortBudget.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != currentSortOrder) {
                    currentSortOrder = position;
                    saveSortPreference();
                    sortBudgets();
                    budgetAdapter.setBudgets(filteredBudgets);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupSearch() {
        etSearchBudget.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filterBudgets(s.toString());
            }
        });
    }

    private void saveSortPreference() {
        getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                .edit()
                .putInt(PREF_BUDGET_SORT, currentSortOrder)
                .apply();
    }

    private void sortBudgets() {
        switch (currentSortOrder) {
            case SORT_AMOUNT_LOW_HIGH:
                Collections.sort(filteredBudgets, (a, b) -> Double.compare(a.getAmount(), b.getAmount()));
                break;
            case SORT_AMOUNT_HIGH_LOW:
                Collections.sort(filteredBudgets, (a, b) -> Double.compare(b.getAmount(), a.getAmount()));
                break;
            case SORT_NAME_A_Z:
                Collections.sort(filteredBudgets, (a, b) ->
                    a.getName().compareToIgnoreCase(b.getName()));
                break;
            case SORT_NAME_Z_A:
                Collections.sort(filteredBudgets, (a, b) ->
                    b.getName().compareToIgnoreCase(a.getName()));
                break;
        }
    }

    private void filterBudgets(String query) {
        if (query.isEmpty()) {
            filteredBudgets.clear();
            filteredBudgets.addAll(allBudgets);
        } else {
            String lowerCaseQuery = query.toLowerCase(Locale.ROOT);
            filteredBudgets = allBudgets.stream()
                    .filter(budget ->
                        budget.getName().toLowerCase(Locale.ROOT).contains(lowerCaseQuery) ||
                        (budget.getDescription() != null &&
                         budget.getDescription().toLowerCase(Locale.ROOT).contains(lowerCaseQuery)) ||
                        String.valueOf((long) budget.getAmount()).contains(query))
                    .collect(Collectors.toList());
        }
        sortBudgets();
        budgetAdapter.setBudgets(filteredBudgets);
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (emptyView != null) {
            emptyView.setVisibility(filteredBudgets.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private void loadAllBudgets() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // Get all budgets for the current user
                List<Budget> budgets = budgetRepository.getBudgetsByUserId(userId);

                if (budgets != null) {
                    allBudgets.clear();
                    allBudgets.addAll(budgets);
                    filteredBudgets.clear();
                    filteredBudgets.addAll(budgets);

                    runOnUiThread(() -> {
                        sortBudgets();
                        budgetAdapter.setBudgets(filteredBudgets);
                        updateEmptyState();
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading budgets: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    Toast.makeText(AllBudgetsActivity.this, getString(R.string.error_loading_budgets), Toast.LENGTH_SHORT).show();
                });
            }
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
            return;
        }
        loadAllBudgets();
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
