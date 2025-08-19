package com.example.se07101campusexpenses.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
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

import java.util.ArrayList;
import java.util.List;

public class AllBudgetsActivity extends AppCompatActivity {

    private static final String TAG = "AllBudgetsActivity";
    private BudgetRVAdapter budgetAdapter;
    private List<Budget> allBudgets = new ArrayList<>();
    private BudgetRepository budgetRepository;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_budgets);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // Use string resource for title
            getSupportActionBar().setTitle(getString(R.string.title_all_budgets));
        }

        // Get user ID
        userId = getSharedPreferences("user_prefs", Context.MODE_PRIVATE).getInt("user_id", -1);
        budgetRepository = new BudgetRepository(this);

        // Set up RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerViewAllBudgets);
        budgetAdapter = new BudgetRVAdapter(allBudgets, this);
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

    private void loadAllBudgets() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // Get all budgets for the current user
                List<Budget> budgets = budgetRepository.getBudgetsByUserId(userId);

                if (budgets != null) {
                    allBudgets.clear();
                    allBudgets.addAll(budgets);

                    runOnUiThread(() -> {
                        // Use DiffUtil-based update
                        budgetAdapter.setBudgets(allBudgets);

                        // Show/hide empty message
                        View emptyView = findViewById(R.id.emptyBudgetsMessage);
                        if (emptyView != null) {
                            emptyView.setVisibility(allBudgets.isEmpty() ? View.VISIBLE : View.GONE);
                        }
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
        loadAllBudgets();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Use OnBackPressedDispatcher to avoid deprecation
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
