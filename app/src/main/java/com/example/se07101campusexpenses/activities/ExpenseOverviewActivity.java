package com.example.se07101campusexpenses.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.se07101campusexpenses.R;
import com.example.se07101campusexpenses.adapter.CategoryBreakdownAdapter;
import com.example.se07101campusexpenses.database.AppDatabase;
import com.example.se07101campusexpenses.database.BudgetRepository;
import com.example.se07101campusexpenses.database.ExpenseRepository;
import com.example.se07101campusexpenses.model.CategorySum;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ExpenseOverviewActivity extends AppCompatActivity {

    private static final String TAG = "ExpenseOverviewActivity";

    // Views
    private TextView tvTotalSpending, tvTotalBudget, tvRemainingBudget;
    private TextView tvNoCategoryData, tvNoTrendData;
    private ProgressBar progressBudget;
    private PieChart pieChart;
    private LineChart lineChart;
    private RecyclerView rvCategoryBreakdown;
    private Button btnViewDetailedReport;

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

        vndFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        vndFormat.setMaximumFractionDigits(0);

        expenseRepository = new ExpenseRepository(this);
        budgetRepository = new BudgetRepository(this);

        userId = getSharedPreferences("user_prefs", MODE_PRIVATE).getInt("user_id", -1);
        if (userId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();

        btnViewDetailedReport.setOnClickListener(v -> {
            Intent intent = new Intent(ExpenseOverviewActivity.this, ExpenseReportActivity.class);
            startActivity(intent);
        });

        loadExpenseData();
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
        tvNoTrendData = findViewById(R.id.tvNoTrendData);
        lineChart = findViewById(R.id.lineChart);
        btnViewDetailedReport = findViewById(R.id.btnViewDetailedReport);
    }

    private void loadExpenseData() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                double totalSpending = expenseRepository.getTotalExpensesByUserId(userId);
                double totalBudget = budgetRepository.getTotalBudgetByUserId(userId);
                double remainingBudget = totalBudget - totalSpending;

                List<CategorySum> categoryData = expenseRepository.getExpensesByCategoryForCurrentMonth(userId);
                Map<String, Double> monthlyTotals = expenseRepository.getMonthlyTotalsForLastSixMonths(userId);

                runOnUiThread(() -> {
                    updateSummary(totalSpending, totalBudget, remainingBudget);
                    updateCategoryBreakdown(categoryData);
                    updateTrends(monthlyTotals);
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading expense data: " + e.getMessage(), e);
                runOnUiThread(() -> Toast.makeText(ExpenseOverviewActivity.this, "Error loading data", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void updateSummary(double totalSpending, double totalBudget, double remainingBudget) {
        tvTotalSpending.setText("Total Spending: " + vndFormat.format(totalSpending));
        tvTotalBudget.setText("Total Budget: " + vndFormat.format(totalBudget));
        tvRemainingBudget.setText("Remaining Budget: " + vndFormat.format(remainingBudget));

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

        PieDataSet dataSet = new PieDataSet(entries, "Categories");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText("Expenses by Category");
        pieChart.setDrawEntryLabels(false);
        pieChart.invalidate();
    }

    private void updateTrends(Map<String, Double> monthlyTotals) {
        if (monthlyTotals == null || monthlyTotals.size() < 2) {
            tvNoTrendData.setVisibility(View.VISIBLE);
            lineChart.setVisibility(View.GONE);
            return;
        }

        tvNoTrendData.setVisibility(View.GONE);
        lineChart.setVisibility(View.VISIBLE);

        setupLineChart(monthlyTotals);
    }

    private void setupLineChart(Map<String, Double> monthlyTotals) {
        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        // Process entries in order they appear in the map (chronological if using LinkedHashMap)
        int index = 0;
        for (Map.Entry<String, Double> entry : monthlyTotals.entrySet()) {
            entries.add(new Entry(index, entry.getValue().floatValue()));
            labels.add(entry.getKey());
            index++;
        }

        if (entries.isEmpty()) {
            tvNoTrendData.setVisibility(View.VISIBLE);
            lineChart.setVisibility(View.GONE);
            return;
        }

        // Create dataset with improved styling
        LineDataSet dataSet = new LineDataSet(entries, "Monthly Expenses");
        dataSet.setColor(Color.BLUE);
        dataSet.setLineWidth(2f);
        dataSet.setCircleColor(Color.BLUE);
        dataSet.setCircleRadius(5f);
        dataSet.setDrawCircleHole(true);
        dataSet.setCircleHoleRadius(2.5f);
        dataSet.setValueTextSize(10f);
        dataSet.setDrawValues(true);
        dataSet.setValueTextColor(Color.BLACK);

        // Customize line appearance - use LINEAR mode for actual data representation
        dataSet.setMode(LineDataSet.Mode.LINEAR);

        // Add highlighting for better user interaction
        dataSet.setHighlightEnabled(true);
        dataSet.setHighlightLineWidth(1f);
        dataSet.setHighLightColor(Color.parseColor("#80CCCCCC")); // semi-transparent gray

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        // Configure X-axis with proper labels
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f); // Minimum axis step (interval) is 1
        xAxis.setLabelRotationAngle(45f); // Rotate labels for better readability
        xAxis.setDrawGridLines(false);

        // Configure chart appearance
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(true);
        lineChart.setDrawGridBackground(false);
        lineChart.getAxisRight().setEnabled(false); // Hide right Y-axis
        lineChart.getAxisLeft().setDrawGridLines(true);

        // Add animation
        lineChart.animateXY(1000, 1000);

        // Refresh chart
        lineChart.invalidate();
    }
}
