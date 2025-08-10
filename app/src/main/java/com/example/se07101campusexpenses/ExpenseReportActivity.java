package com.example.se07101campusexpenses;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.se07101campusexpenses.adapter.ExpenseReportAdapter;
import com.example.se07101campusexpenses.model.Expense;
import com.example.se07101campusexpenses.model.CategorySum; // Import CategorySum
import com.example.se07101campusexpenses.database.ExpenseRepository;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExpenseReportActivity extends AppCompatActivity {

    private EditText etStartDate, etEndDate;
    private RecyclerView rvReport;
    private TextView tvReportTotal;
    private PieChart reportPieChart;
    private ExpenseRepository expenseRepository;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_report);

        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        Button btnGenerateReport = findViewById(R.id.btnGenerateReport);
        rvReport = findViewById(R.id.rvReport);
        tvReportTotal = findViewById(R.id.tvReportTotal);
        reportPieChart = findViewById(R.id.reportPieChart);

        expenseRepository = new ExpenseRepository(this); // Application context might be better for repository
        rvReport.setLayoutManager(new LinearLayoutManager(this));

        btnGenerateReport.setOnClickListener(v -> generateReport());
    }

    private void generateReport() {
        String startDate = etStartDate.getText().toString();
        String endDate = etEndDate.getText().toString();

        List<Expense> expenses = expenseRepository.getExpensesBetweenDates(startDate, endDate);
        // TODO: The ExpenseReportAdapter is designed for List<Expense>.
        // If rvReport is meant to show individual expenses, this is fine.
        // If it was meant to show sums by category, a new adapter would be needed.
        ExpenseReportAdapter adapter = new ExpenseReportAdapter(expenses);
        rvReport.setAdapter(adapter);

        double total = 0;
        for (Expense expense : expenses) {
            total += expense.getAmount();
        }
        tvReportTotal.setText(String.format(Locale.US, "Total Expenses: $%.2f", total));

        setupPieChart(startDate, endDate);
    }

    private void setupPieChart(String startDate, String endDate) {
        List<PieEntry> entries = new ArrayList<>();
        // Updated to use CategorySum
        List<CategorySum> categorySums = expenseRepository.getExpensesByCategoryBetweenDates(startDate, endDate);

        for (CategorySum sum : categorySums) {
            entries.add(new PieEntry((float) sum.amount, sum.category));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Expenses by Category");
        // TODO: Add colors to PieDataSet for better visualization
        // dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        PieData pieData = new PieData(dataSet);
        reportPieChart.setData(pieData);
        reportPieChart.invalidate(); // refresh
    }
}
