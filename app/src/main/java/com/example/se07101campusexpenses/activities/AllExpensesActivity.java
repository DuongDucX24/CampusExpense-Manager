package com.example.se07101campusexpenses.activities;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
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
import java.util.List;
import java.util.Locale;

public class AllExpensesActivity extends AppCompatActivity {

    private static final String TAG = "AllExpensesActivity";
    private ExpenseAdapter expenseAdapter;
    private List<Expense> allExpenses = new ArrayList<>();
    private ExpenseRepository expenseRepository;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_expenses);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("All Expenses");
        }

        // Get user ID
        userId = getSharedPreferences("user_prefs", Context.MODE_PRIVATE).getInt("user_id", -1);
        expenseRepository = new ExpenseRepository(this);

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

                    runOnUiThread(() -> {
                        expenseAdapter.submitExpenseList(allExpenses);
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading expenses: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    Toast.makeText(AllExpensesActivity.this, "Failed to load expenses", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
