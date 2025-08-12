package com.example.se07101campusexpenses.activities;

import android.os.Bundle;
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

import java.text.NumberFormat; // Added import
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExpenseReportActivity extends AppCompatActivity {

    private EditText etStartDate, etEndDate;
    private RecyclerView rvReport;
    private TextView tvReportTotal;
    private ExpenseRepository expenseRepository;
    private NumberFormat vndFormat; // Added for currency formatting

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_report);

        vndFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN")); // Initialize formatter
        vndFormat.setMaximumFractionDigits(0); // VND usually doesn't show decimals

        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        Button btnGenerateReport = findViewById(R.id.btnGenerateReport);
        rvReport = findViewById(R.id.rvReport);
        tvReportTotal = findViewById(R.id.tvReportTotal);

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
        tvReportTotal.setText("Total Expenses: " + vndFormat.format(total)); // Use VND format

    }


}
