package com.example.se07101campusexpenses;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.se07101campusexpenses.database.AppDatabase;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.example.se07101campusexpenses.database.BudgetRepository;
import com.example.se07101campusexpenses.model.Expense;
import com.example.se07101campusexpenses.database.ExpenseRepository;
import com.example.se07101campusexpenses.model.CategorySum;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private TextView tvTotalSpending, tvRemainingBudget;
    private LineChart chart;
    private PieChart pieChart;
    private ExpenseRepository expenseRepository;
    private BudgetRepository budgetRepository;
    private int userId;

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

        SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        expenseRepository = new ExpenseRepository(requireContext());
        budgetRepository = new BudgetRepository(requireContext());

        loadDashboardData();
    }

    private void loadDashboardData() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            if (userId == -1) {
                // Handle user not logged in case
                requireActivity().runOnUiThread(() -> {
                    tvTotalSpending.setText("User not logged in");
                    tvRemainingBudget.setText("");
                });
                return;
            }
            // Fetch data in the background
            double totalSpending = expenseRepository.getTotalExpensesByUserId(userId);
            double totalBudget = budgetRepository.getTotalBudgetByUserId(userId); 
            double remainingBudget = totalBudget - totalSpending;
            List<Expense> userExpenses = expenseRepository.getExpensesByUserId(userId);
            List<CategorySum> expensesByCategory = expenseRepository.getCategorySumsByUserId(userId);

            // Update UI on the main thread
            requireActivity().runOnUiThread(() -> {
                updateSummary(totalSpending, remainingBudget);
                if (userExpenses != null) {
                    setupChart(userExpenses);
                }
                if (expensesByCategory != null) {
                    setupPieChart(expensesByCategory);
                }
            });
        });
    }

    private void updateSummary(double totalSpending, double remainingBudget) {
        tvTotalSpending.setText(String.format(Locale.US, "Total Spending: $%.2f", totalSpending));
        tvRemainingBudget.setText(String.format(Locale.US, "Remaining Budget: $%.2f", remainingBudget));
    }

    private void setupChart(List<Expense> expenses) {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < expenses.size(); i++) {
            entries.add(new Entry(i, (float) expenses.get(i).getAmount()));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Expense Trend");
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.invalidate(); // refresh
    }

    private void setupPieChart(List<CategorySum> categorySums) { 
        List<PieEntry> entries = new ArrayList<>();

        for (CategorySum categorySum : categorySums) {
             // Use direct field access for public fields in CategorySum
            entries.add(new PieEntry((float) categorySum.amount, categorySum.category));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Expenses by Category");
        dataSet.setColors(com.github.mikephil.charting.utils.ColorTemplate.MATERIAL_COLORS);
        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.invalidate(); // refresh
    }
}