package com.example.se07101campusexpenses;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.se07101campusexpenses.database.AppDatabase;
import com.example.se07101campusexpenses.database.BudgetRepository;
import com.example.se07101campusexpenses.database.ExpenseRepository;
import com.example.se07101campusexpenses.model.CategorySum;
import com.example.se07101campusexpenses.model.Expense;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    private TextView tvTotalSpending, tvRemainingBudget;
    private LineChart chart;
    private PieChart pieChart;
    private ExpenseRepository expenseRepository;
    private BudgetRepository budgetRepository;
    private int userId;
    private NumberFormat vndFormat;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vndFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        vndFormat.setMaximumFractionDigits(0); // VND usually doesn't show decimals
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            tvTotalSpending = view.findViewById(R.id.tvTotalSpending);
            tvRemainingBudget = view.findViewById(R.id.tvRemainingBudget);
            chart = view.findViewById(R.id.chart);
            pieChart = view.findViewById(R.id.pieChart);

            SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            userId = prefs.getInt("user_id", -1);

            expenseRepository = new ExpenseRepository(requireContext());
            budgetRepository = new BudgetRepository(requireContext());

            setupLineChartListener();
            // loadDashboardData(); // Moved to onResume
        } catch (Exception e) {
            Log.e(TAG, "Error in onViewCreated: " + e.getMessage(), e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDashboardData(); // Load/refresh data when fragment is resumed
    }

    private void setupLineChartListener() {
        if (chart == null) return;
        chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                try {
                    Log.i(TAG, "LineChart value selected: " + e.toString());
                } catch (Exception ex) {
                    Log.e(TAG, "Error in onValueSelected: " + ex.getMessage(), ex);
                }
            }

            @Override
            public void onNothingSelected() {
                try {
                    Log.i(TAG, "LineChart nothing selected.");
                } catch (Exception ex) {
                    Log.e(TAG, "Error in onNothingSelected: " + ex.getMessage(), ex);
                }
            }
        });
    }

    private void loadDashboardData() {
        try {
            AppDatabase.databaseWriteExecutor.execute(() -> {
                try {
                    if (userId == -1) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                tvTotalSpending.setText("User not logged in");
                                tvRemainingBudget.setText("");
                            });
                        }
                        return;
                    }
                    double totalSpending = expenseRepository.getTotalExpensesByUserId(userId);
                    double totalBudget = budgetRepository.getTotalBudgetByUserId(userId);
                    double remainingBudget = totalBudget - totalSpending;
                    List<Expense> userExpenses = expenseRepository.getExpensesByUserId(userId);
                    List<CategorySum> expensesByCategory = expenseRepository.getCategorySumsByUserId(userId);

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            updateSummary(totalSpending, remainingBudget);
                            if (userExpenses != null) {
                                setupChart(userExpenses);
                            }
                            if (expensesByCategory != null) {
                                setupPieChart(expensesByCategory);
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error in background task of loadDashboardData: " + e.getMessage(), e);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            tvTotalSpending.setText("Error loading data");
                            tvRemainingBudget.setText("");
                        });
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error executing loadDashboardData: " + e.getMessage(), e);
        }
    }

    private void updateSummary(double totalSpending, double remainingBudget) {
        try {
            tvTotalSpending.setText("Total Spending: " + vndFormat.format(totalSpending));
            tvRemainingBudget.setText("Remaining Budget: " + vndFormat.format(remainingBudget));
        } catch (Exception e) {
            Log.e(TAG, "Error in updateSummary: " + e.getMessage(), e);
        }
    }

    private void setupChart(List<Expense> expenses) {
        try {
            if (chart == null || expenses == null) {
                Log.w(TAG, "setupChart: Chart or expenses is null.");
                return;
            }
            List<Entry> entries = new ArrayList<>();
            for (int i = 0; i < expenses.size(); i++) {
                if (expenses.get(i) != null) {
                    entries.add(new Entry(i, (float) expenses.get(i).getAmount()));
                }
            }

            if (entries.isEmpty()) {
                Log.i(TAG, "No entries to display in LineChart.");
                chart.clear();
                chart.invalidate();
                return;
            }

            LineDataSet dataSet = new LineDataSet(entries, "Expense Trend");
            dataSet.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return vndFormat.format(value);
                }
            });

            LineData lineData = new LineData(dataSet);
            chart.setData(lineData);

            YAxis leftAxis = chart.getAxisLeft();
            leftAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return vndFormat.format(value);
                }
            });
            chart.getAxisRight().setEnabled(false); // Disable right axis

            chart.getDescription().setEnabled(false);
            chart.invalidate();
            Log.i(TAG, "LineChart setup complete with " + entries.size() + " entries.");
        } catch (Exception e) {
            Log.e(TAG, "Error in setupChart: " + e.getMessage(), e);
        }
    }

    private void setupPieChart(List<CategorySum> categorySums) {
        try {
            if (pieChart == null || categorySums == null) {
                Log.w(TAG, "setupPieChart: PieChart or categorySums is null.");
                return;
            }
            List<PieEntry> entries = new ArrayList<>();
            for (CategorySum categorySum : categorySums) {
                if (categorySum != null) {
                    String label = categorySum.category != null ? categorySum.category : ""; // Ensure label is not null
                    entries.add(new PieEntry((float) categorySum.amount, label));
                }
            }

            if (entries.isEmpty()) {
                Log.i(TAG, "No entries to display in PieChart.");
                pieChart.clear();
                pieChart.invalidate();
                return;
            }

            PieDataSet dataSet = new PieDataSet(entries, "Expenses by Category");
            dataSet.setColors(com.github.mikephil.charting.utils.ColorTemplate.MATERIAL_COLORS);
            dataSet.setValueFormatter(new ValueFormatter() { // Format slice values as VND
                @Override
                public String getFormattedValue(float value) {
                    return vndFormat.format(value);
                }
            });
            dataSet.setValueTextSize(12f);


            PieData pieData = new PieData(dataSet);
            pieChart.setData(pieData);
            pieChart.getDescription().setEnabled(false);
            // pieChart.setUsePercentValues(true); // If you want percents, disable the VND formatter for values
            pieChart.setEntryLabelTextSize(10f);
            pieChart.invalidate();
            Log.i(TAG, "PieChart setup complete with " + entries.size() + " entries.");
        } catch (Exception e) {
            Log.e(TAG, "Error in setupPieChart: " + e.getMessage(), e);
        }
    }
}
