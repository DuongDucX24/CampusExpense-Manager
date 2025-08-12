package com.example.se07101campusexpenses.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.se07101campusexpenses.R;
import com.example.se07101campusexpenses.activities.AddBudgetActivity;
import com.example.se07101campusexpenses.activities.AddExpenseActivity;
import com.example.se07101campusexpenses.activities.ExpenseOverviewActivity;
import com.example.se07101campusexpenses.activities.ExpenseReportActivity;
import com.example.se07101campusexpenses.database.AppDatabase;
import com.example.se07101campusexpenses.database.BudgetRepository;
import com.example.se07101campusexpenses.database.ExpenseRepository;
import com.example.se07101campusexpenses.model.Expense;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private static final int MAX_RECENT_EXPENSES = 3;

    private TextView tvTotalSpending, tvRemainingBudget;
    private ProgressBar progressBudget;
    private LinearLayout recentExpensesContainer;
    private TextView tvNoRecentExpenses;
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
            // Find UI elements
            tvTotalSpending = view.findViewById(R.id.tvTotalSpending);
            tvRemainingBudget = view.findViewById(R.id.tvRemainingBudget);
            progressBudget = view.findViewById(R.id.progressBudget);
            recentExpensesContainer = view.findViewById(R.id.recentExpensesContainer);
            tvNoRecentExpenses = view.findViewById(R.id.tvNoRecentExpenses);

            // Set up quick action buttons
            MaterialButton btnAddExpense = view.findViewById(R.id.btnAddExpense);
            MaterialButton btnAddBudget = view.findViewById(R.id.btnAddBudget);

            btnAddExpense.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), AddExpenseActivity.class);
                startActivity(intent);
            });

            btnAddBudget.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), AddBudgetActivity.class);
                startActivity(intent);
            });

            // Set up report & analysis buttons
            MaterialButton btnExpenseOverview = view.findViewById(R.id.btnExpenseOverview);
            MaterialButton btnExpenseReport = view.findViewById(R.id.btnExpenseReport);

            btnExpenseOverview.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), ExpenseOverviewActivity.class);
                startActivity(intent);
            });

            btnExpenseReport.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(getActivity(), ExpenseReportActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error launching expense report: " + e.getMessage(), e);
                    Toast.makeText(getContext(), "Expense report feature is coming soon", Toast.LENGTH_SHORT).show();
                }
            });

            SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            userId = prefs.getInt("user_id", -1);

            expenseRepository = new ExpenseRepository(requireContext());
            budgetRepository = new BudgetRepository(requireContext());

        } catch (Exception e) {
            Log.e(TAG, "Error in onViewCreated: " + e.getMessage(), e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDashboardData();
    }

    private void loadDashboardData() {
        try {
            AppDatabase.databaseWriteExecutor.execute(() -> {
                try {
                    if (userId == -1) {
                        if (isAdded() && getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                tvTotalSpending.setText("Not logged in");
                                tvRemainingBudget.setText("Not logged in");
                                progressBudget.setProgress(0);
                            });
                        }
                        return;
                    }

                    // Get financial summary data
                    double totalSpending = expenseRepository.getTotalExpensesByUserId(userId);
                    double totalBudget = budgetRepository.getTotalBudgetByUserId(userId);
                    double remainingBudget = totalBudget - totalSpending;

                    // Calculate budget utilization percentage
                    int budgetUtilizationPercent = 0;
                    if (totalBudget > 0) {
                        budgetUtilizationPercent = (int) Math.min(100, (totalSpending / totalBudget) * 100);
                    }

                    // Get recent expenses
                    List<Expense> recentExpenses = expenseRepository.getRecentExpensesByUserId(userId, MAX_RECENT_EXPENSES);

                    // Update UI on the main thread
                    if (isAdded() && getActivity() != null) {
                        final int finalBudgetPercent = budgetUtilizationPercent;
                        final List<Expense> finalRecentExpenses = recentExpenses;

                        getActivity().runOnUiThread(() -> {
                            try {
                                // Update financial summary
                                tvTotalSpending.setText(vndFormat.format(totalSpending));
                                tvRemainingBudget.setText(vndFormat.format(remainingBudget));
                                progressBudget.setProgress(finalBudgetPercent);

                                // Update recent expenses
                                updateRecentExpensesUI(finalRecentExpenses);
                            } catch (Exception e) {
                                Log.e(TAG, "Error updating UI: " + e.getMessage(), e);
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error loading dashboard data: " + e.getMessage(), e);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling dashboard data load: " + e.getMessage(), e);
        }
    }

    /**
     * Updates the UI to display recent expenses
     */
    private void updateRecentExpensesUI(List<Expense> recentExpenses) {
        try {
            // Clear existing expense views
            recentExpensesContainer.removeAllViews();

            if (recentExpenses == null || recentExpenses.isEmpty()) {
                tvNoRecentExpenses.setVisibility(View.VISIBLE);
                return;
            }

            tvNoRecentExpenses.setVisibility(View.GONE);

            // Add recent expenses
            LayoutInflater inflater = LayoutInflater.from(getContext());

            for (Expense expense : recentExpenses) {
                View expenseView = inflater.inflate(R.layout.item_recent_expense, recentExpensesContainer, false);

                TextView tvDescription = expenseView.findViewById(R.id.tvExpenseDescription);
                TextView tvAmount = expenseView.findViewById(R.id.tvExpenseAmount);
                TextView tvCategory = expenseView.findViewById(R.id.tvExpenseCategory);
                TextView tvDate = expenseView.findViewById(R.id.tvExpenseDate);

                if (tvDescription != null) tvDescription.setText(expense.getDescription());
                if (tvAmount != null) tvAmount.setText(vndFormat.format(expense.getAmount()));
                if (tvCategory != null) tvCategory.setText(expense.getCategory());
                if (tvDate != null) tvDate.setText(expense.getDate());

                // Add to container
                recentExpensesContainer.addView(expenseView);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating recent expenses UI: " + e.getMessage(), e);
        }
    }
}
