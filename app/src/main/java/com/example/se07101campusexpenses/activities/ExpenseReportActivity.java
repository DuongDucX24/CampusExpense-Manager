package com.example.se07101campusexpenses.activities;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.se07101campusexpenses.R;
import com.example.se07101campusexpenses.adapter.ExpenseReportAdapter;
import com.example.se07101campusexpenses.model.Expense;
import com.example.se07101campusexpenses.model.CategorySum; // Import CategorySum
import com.example.se07101campusexpenses.database.ExpenseRepository;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.NumberFormat; // Added import
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ExpenseReportActivity extends AppCompatActivity {

    private EditText etStartDate, etEndDate;
    private RecyclerView rvReport;
    private TextView tvReportTotal;
    private TextView tvNoCategoryData;
    private PieChart reportPieChart;
    private ExpenseRepository expenseRepository;
    private NumberFormat vndFormat; // Added for currency formatting
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
    private int userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_report);

        vndFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN")); // Initialize formatter
        vndFormat.setMaximumFractionDigits(0); // VND usually doesn't show decimals

        userId = getSharedPreferences("user_prefs", MODE_PRIVATE).getInt("user_id", -1);

        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        Button btnGenerateReport = findViewById(R.id.btnGenerateReport);
        rvReport = findViewById(R.id.rvReport);
        tvReportTotal = findViewById(R.id.tvReportTotal);
        Button btnBack = findViewById(R.id.btnBackReport);
        reportPieChart = findViewById(R.id.reportPieChart);

        // Add TextView for "No Category Data" message
        tvNoCategoryData = new TextView(this);
        tvNoCategoryData.setText("No category data available for the selected period");
        tvNoCategoryData.setTextSize(16);
        tvNoCategoryData.setTextColor(Color.GRAY);
        tvNoCategoryData.setVisibility(View.GONE);
        ((android.view.ViewGroup) reportPieChart.getParent()).addView(tvNoCategoryData);

        // Set default dates (current month)
        setDefaultDates();

        // Set up date pickers
        etStartDate.setOnClickListener(v -> showDatePickerDialog(etStartDate));
        etEndDate.setOnClickListener(v -> showDatePickerDialog(etEndDate));

        expenseRepository = new ExpenseRepository(this); // Application context might be better for repository
        rvReport.setLayoutManager(new LinearLayoutManager(this));

        btnGenerateReport.setOnClickListener(v -> generateReport());

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Generate report with default dates when activity starts
        generateReport();
    }

    private void setDefaultDates() {
        Calendar calendar = Calendar.getInstance();

        // Set end date to today
        etEndDate.setText(dateFormat.format(calendar.getTime()));

        // Set start date to first day of current month
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        etStartDate.setText(dateFormat.format(calendar.getTime()));
    }

    private void showDatePickerDialog(final EditText editText) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Parse existing date if present
        try {
            if (!editText.getText().toString().isEmpty()) {
                Date date = dateFormat.parse(editText.getText().toString());
                if (date != null) {
                    c.setTime(date);
                    year = c.get(Calendar.YEAR);
                    month = c.get(Calendar.MONTH);
                    day = c.get(Calendar.DAY_OF_MONTH);
                }
            }
        } catch (Exception e) {
            // Use current date if parsing fails
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String selectedDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1;
                    editText.setText(selectedDate);

                    // Auto-generate report when date changes
                    generateReport();
                }, year, month, day);
        datePickerDialog.show();
    }

    private void generateReport() {
        String startDate = etStartDate.getText().toString();
        String endDate = etEndDate.getText().toString();

        if (startDate.isEmpty() || endDate.isEmpty()) {
            return;
        }

        List<Expense> expenses = expenseRepository.getExpensesBetweenDates(startDate, endDate);

        // Filter expenses by the current user
        List<Expense> userExpenses = new ArrayList<>();
        for (Expense expense : expenses) {
            if (expense.getUserId() == userId) {
                userExpenses.add(expense);
            }
        }

        ExpenseReportAdapter adapter = new ExpenseReportAdapter(userExpenses);
        rvReport.setAdapter(adapter);

        double total = 0;
        for (Expense expense : userExpenses) {
            total += expense.getAmount();
        }
        tvReportTotal.setText("Total Expenses: " + vndFormat.format(total)); // Use VND format

        // Generate and display category breakdown
        generateCategoryBreakdown(userExpenses);
    }

    /**
     * Generates and displays the category breakdown chart
     * @param expenses List of expenses to analyze
     */
    private void generateCategoryBreakdown(List<Expense> expenses) {
        if (expenses == null || expenses.isEmpty()) {
            showNoCategoryDataMessage();
            return;
        }

        // Group expenses by category and sum amounts
        Map<String, Double> categorySums = new HashMap<>();
        for (Expense expense : expenses) {
            String category = expense.getCategory();
            if (category != null && !category.isEmpty()) {
                Double currentSum = categorySums.getOrDefault(category, 0.0);
                categorySums.put(category, currentSum + expense.getAmount());
            }
        }

        if (categorySums.isEmpty()) {
            showNoCategoryDataMessage();
            return;
        }

        // Create chart entries
        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : categorySums.entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
        }

        // Configure and display the pie chart
        setupPieChart(entries);
    }

    /**
     * Sets up the pie chart with the provided entries
     * @param entries PieEntries representing category data
     */
    private void setupPieChart(List<PieEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            showNoCategoryDataMessage();
            return;
        }

        tvNoCategoryData.setVisibility(View.GONE);
        reportPieChart.setVisibility(View.VISIBLE);

        PieDataSet dataSet = new PieDataSet(entries, "Expenses by Category");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);
        dataSet.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return vndFormat.format(value);
            }
        });

        PieData pieData = new PieData(dataSet);
        reportPieChart.setData(pieData);
        reportPieChart.getDescription().setEnabled(false);
        reportPieChart.setCenterText("Expenses by Category");
        reportPieChart.setDrawEntryLabels(false);

        // Enable the legend to show category names
        reportPieChart.getLegend().setEnabled(true);
        reportPieChart.getLegend().setWordWrapEnabled(true);

        // Add animation
        reportPieChart.animateY(1000);

        // Refresh chart
        reportPieChart.invalidate();
    }

    /**
     * Shows a message when no category data is available
     */
    private void showNoCategoryDataMessage() {
        reportPieChart.setVisibility(View.GONE);
        tvNoCategoryData.setVisibility(View.VISIBLE);
    }
}
