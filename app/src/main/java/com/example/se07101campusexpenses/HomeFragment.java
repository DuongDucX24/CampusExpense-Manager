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
import com.example.se07101campusexpenses.model.Expense;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
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
        vndFormat.setMaximumFractionDigits(0);
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

            SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            userId = prefs.getInt("user_id", -1);

            expenseRepository = new ExpenseRepository(requireContext());
            budgetRepository = new BudgetRepository(requireContext());

            setupLineChartListener();
        } catch (Exception e) {
            Log.e(TAG, "Error in onViewCreated: " + e.getMessage(), e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDashboardData();
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
                                tvTotalSpending.setText(R.string.user_not_logged_in);
                                tvRemainingBudget.setText("");
                            });
                        }
                        return;
                    }
                    double totalSpending = expenseRepository.getTotalExpensesByUserId(userId);
                    double totalBudget = budgetRepository.getTotalBudgetByUserId(userId);
                    double remainingBudget = totalBudget - totalSpending;
                    List<Expense> userExpenses = expenseRepository.getExpensesByUserId(userId);

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (!isAdded() || getView() == null) {
                                Log.w(TAG, "Fragment not attached or view destroyed, skipping UI update.");
                                return;
                            }
                            updateSummary(totalSpending, remainingBudget);
                            if (userExpenses != null) {
                                setupChart(userExpenses);
                            } else if (chart == null) {
                                Log.w(TAG, "Line chart is null, cannot setup.");
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error in background task of loadDashboardData: " + e.getMessage(), e);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            tvTotalSpending.setText(R.string.error_loading_data);
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
            tvTotalSpending.setText(getString(R.string.total_spending, vndFormat.format(totalSpending)));
            tvRemainingBudget.setText(getString(R.string.remaining_budget, vndFormat.format(remainingBudget)));
        } catch (Exception e) {
            Log.e(TAG, "Error in updateSummary: " + e.getMessage(), e);
        }
    }

    private void setupChart(List<Expense> expenses) {
        if (getView() == null || !isAdded() || chart == null) {
            Log.w(TAG, "setupChart: View, fragment, or chart is null/not ready.");
            return;
        }
        try {
            List<Entry> entries = new ArrayList<>();
            if (expenses != null) {
                for (int i = 0; i < expenses.size(); i++) {
                    if (expenses.get(i) != null) {
                        entries.add(new Entry(i, (float) expenses.get(i).getAmount()));
                    }
                }
            }

            LineDataSet dataSet = getLineDataSet(entries);

            chart.setData(new LineData(dataSet));
            chart.invalidate();
            Log.i(TAG, "LineChart setup complete.");
        } catch (Exception e) {
            Log.e(TAG, "Error in setupChart: " + e.getMessage(), e);
        }
    }

    @NonNull
    private LineDataSet getLineDataSet(List<Entry> entries) {
        LineDataSet dataSet = new LineDataSet(entries, "Expense Trend");
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (vndFormat == null) {
                    Log.e(TAG, "vndFormat is null in LineChart ValueFormatter!");
                    return String.valueOf(value);
                }
                return vndFormat.format(value);
            }
        });
        return dataSet;
    }

}
