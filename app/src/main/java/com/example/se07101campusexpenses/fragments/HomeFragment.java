package com.example.se07101campusexpenses.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.example.se07101campusexpenses.database.AppDatabase;
import com.example.se07101campusexpenses.database.BudgetRepository;
import com.example.se07101campusexpenses.database.ExpenseRepository;

import java.text.NumberFormat;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    private TextView tvTotalSpending, tvRemainingBudget;
    private ProgressBar progressBudget;
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

            // Set up quick action buttons using regular Button instead of MaterialButton
            Button btnAddExpense = view.findViewById(R.id.btnAddExpense);
            Button btnAddBudget = view.findViewById(R.id.btnAddBudget);

            btnAddExpense.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), AddExpenseActivity.class);
                startActivity(intent);
            });

            btnAddBudget.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), AddBudgetActivity.class);
                startActivity(intent);
            });

            // Set up report & analysis buttons
            Button btnExpenseOverview = view.findViewById(R.id.btnExpenseOverview);

            btnExpenseOverview.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), ExpenseOverviewActivity.class);
                startActivity(intent);
            });

            // Get current user ID
            userId = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE).getInt("user_id", -1);

            // Initialize repositories
            expenseRepository = new ExpenseRepository(requireContext());
            budgetRepository = new BudgetRepository(requireContext());

            // Load financial data
            loadFinancialSummary();

        } catch (Exception e) {
            Log.e(TAG, "Error in HomeFragment setup: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error initializing dashboard", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFinancialSummary(); // Refresh data when returning to the fragment
    }

    private void loadFinancialSummary() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                double totalExpenses = expenseRepository.getTotalExpensesByUserId(userId);
                double totalBudget = budgetRepository.getTotalBudgetByUserId(userId);
                double remainingBudget = totalBudget - totalExpenses;
                int budgetUtilizationPercentage = totalBudget > 0
                    ? (int) ((totalExpenses / totalBudget) * 100)
                    : 0;

                // Ensure utilization percentage doesn't exceed 100%
                budgetUtilizationPercentage = Math.min(budgetUtilizationPercentage, 100);

                final double finalTotalExpenses = totalExpenses;
                final double finalRemainingBudget = remainingBudget;
                final int finalUtilizationPercentage = budgetUtilizationPercentage;

                // Update UI on the main thread if fragment is still attached
                if (isAdded() && getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        try {
                            tvTotalSpending.setText(vndFormat.format(finalTotalExpenses));
                            tvRemainingBudget.setText(vndFormat.format(finalRemainingBudget));
                            progressBudget.setProgress(finalUtilizationPercentage);
                        } catch (Exception e) {
                            Log.e(TAG, "Error updating UI: " + e.getMessage(), e);
                        }
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading financial summary: " + e.getMessage(), e);
            }
        });
    }
}
