package com.example.se07101campusexpenses;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.example.se07101campusexpenses.database.BudgetRepository;
import com.example.se07101campusexpenses.database.Expense;
import com.example.se07101campusexpenses.database.ExpenseRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private TextView tvTotalSpending, tvRemainingBudget;
    private LineChart chart;
    private PieChart pieChart;
    private ExpenseRepository expenseRepository;
    private BudgetRepository budgetRepository;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvTotalSpending = view.findViewById(R.id.tvTotalSpending);
        tvRemainingBudget = view.findViewById(R.id.tvRemainingBudget);
        chart = view.findViewById(R.id.chart);
        pieChart = view.findViewById(R.id.pieChart);

        expenseRepository = new ExpenseRepository(requireContext());
        budgetRepository = new BudgetRepository(requireActivity().getApplication());

        updateSummary();
        setupChart();
        setupPieChart();
    }

    private void updateSummary() {
        double totalSpending = expenseRepository.getTotalExpenses();
        double totalBudget = budgetRepository.getTotalBudget();
        double remainingBudget = totalBudget - totalSpending;

        tvTotalSpending.setText(String.format(Locale.US, "Total Spending: $%.2f", totalSpending));
        tvRemainingBudget.setText(String.format(Locale.US, "Remaining Budget: $%.2f", remainingBudget));
    }

    private void setupChart() {
        List<Entry> entries = new ArrayList<>();
        List<Expense> expenses = expenseRepository.getAllExpenses();
        // Simple example: chart of expense amounts over time (index)
        for (int i = 0; i < expenses.size(); i++) {
            entries.add(new Entry(i, (float) expenses.get(i).getAmount()));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Expense Trend");
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.invalidate(); // refresh
    }

    private void setupPieChart() {
        List<PieEntry> entries = new ArrayList<>();
        List<Expense> expenses = expenseRepository.getExpensesByCategory();

        for (Expense expense : expenses) {
            entries.add(new PieEntry((float) expense.getAmount(), expense.getCategory()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Expenses by Category");
        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.invalidate(); // refresh
    }
}