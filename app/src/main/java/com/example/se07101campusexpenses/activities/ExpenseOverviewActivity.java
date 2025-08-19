package com.example.se07101campusexpenses.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.se07101campusexpenses.R;
import com.example.se07101campusexpenses.adapter.CategoryBreakdownAdapter;
import com.example.se07101campusexpenses.database.AppDatabase;
import com.example.se07101campusexpenses.database.BudgetRepository;
import com.example.se07101campusexpenses.database.ExpenseRepository;
import com.example.se07101campusexpenses.model.CategorySum;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExpenseOverviewActivity extends AppCompatActivity {

    private static final String TAG = "ExpenseOverviewActivity";

    // Views
    private TextView tvTotalSpending, tvTotalBudget, tvRemainingBudget;
    private TextView tvNoCategoryData;
    private ProgressBar progressBudget;
    private PieChart pieChart;
    private RecyclerView rvCategoryBreakdown;

    // Repositories
    private ExpenseRepository expenseRepository;
    private BudgetRepository budgetRepository;

    // Data
    private int userId;
    private NumberFormat vndFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_overview);

        // Setup toolbar with back button
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.title_expense_overview));
        }

        vndFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        vndFormat.setMaximumFractionDigits(0);

        expenseRepository = new ExpenseRepository(this);
        budgetRepository = new BudgetRepository(this);

        userId = getSharedPreferences("user_prefs", MODE_PRIVATE).getInt("user_id", -1);
        if (userId == -1) {
            Toast.makeText(this, getString(R.string.user_not_logged_in), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();

        loadExpenseData();
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

    private void initializeViews() {
        tvTotalSpending = findViewById(R.id.tvTotalSpending);
        tvTotalBudget = findViewById(R.id.tvTotalBudget);
        tvRemainingBudget = findViewById(R.id.tvRemainingBudget);
        progressBudget = findViewById(R.id.progressBudget);
        tvNoCategoryData = findViewById(R.id.tvNoCategoryData);
        pieChart = findViewById(R.id.pieChart);
        rvCategoryBreakdown = findViewById(R.id.rvCategoryBreakdown);
        rvCategoryBreakdown.setLayoutManager(new LinearLayoutManager(this));
        rvCategoryBreakdown.setNestedScrollingEnabled(false);
    }

    private void loadExpenseData() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                double totalSpending = expenseRepository.getTotalExpensesByUserId(userId);
                double totalBudget = budgetRepository.getTotalBudgetByUserId(userId);
                double remainingBudget = totalBudget - totalSpending;

                List<CategorySum> categoryData = expenseRepository.getExpensesByCategoryForCurrentMonth(userId);

                runOnUiThread(() -> {
                    updateSummary(totalSpending, totalBudget, remainingBudget);
                    updateCategoryBreakdown(categoryData);
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading expense data: " + e.getMessage(), e);
                runOnUiThread(() -> Toast.makeText(ExpenseOverviewActivity.this, getString(R.string.error_loading_data), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void updateSummary(double totalSpending, double totalBudget, double remainingBudget) {
        tvTotalSpending.setText(getString(R.string.total_spending, vndFormat.format(totalSpending)));
        tvTotalBudget.setText(getString(R.string.total_budget, vndFormat.format(totalBudget)));
        tvRemainingBudget.setText(getString(R.string.remaining_budget, vndFormat.format(remainingBudget)));

        if (totalBudget > 0) {
            int progress = (int) ((totalSpending / totalBudget) * 100);
            progressBudget.setProgress(Math.min(progress, 100));
        } else {
            progressBudget.setProgress(0);
        }
    }

    private void updateCategoryBreakdown(List<CategorySum> categoryData) {
        if (categoryData == null || categoryData.isEmpty()) {
            tvNoCategoryData.setVisibility(View.VISIBLE);
            pieChart.setVisibility(View.GONE);
            rvCategoryBreakdown.setVisibility(View.GONE);
            return;
        }

        tvNoCategoryData.setVisibility(View.GONE);
        pieChart.setVisibility(View.VISIBLE);
        rvCategoryBreakdown.setVisibility(View.VISIBLE);

        CategoryBreakdownAdapter adapter = new CategoryBreakdownAdapter(categoryData);
        rvCategoryBreakdown.setAdapter(adapter);

        setupPieChart(categoryData);
    }

    private void setupPieChart(List<CategorySum> categoryData) {
        List<PieEntry> entries = new ArrayList<>();
        for (CategorySum summary : categoryData) {
            if (summary.getSum() > 0) {
                entries.add(new PieEntry((float) summary.getSum(), summary.getCategory()));
            }
        }

        if (entries.isEmpty()) {
            tvNoCategoryData.setVisibility(View.VISIBLE);
            pieChart.setVisibility(View.GONE);
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, getString(R.string.categories));
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText(getString(R.string.expenses_by_category));
        pieChart.setDrawEntryLabels(false);
        pieChart.invalidate();
    }
}
