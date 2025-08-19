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

    // Keep the latest observed totals
    private double currentTotalExpenses = 0.0;
    private double currentTotalBudget = 0.0;

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

            // Set up quick action buttons
            Button btnAddExpense = view.findViewById(R.id.btnAddExpense);
            Button btnAddBudget = view.findViewById(R.id.btnAddBudget);
            Button btnExpenseOverview = view.findViewById(R.id.btnExpenseOverview);

            btnAddExpense.setOnClickListener(v -> startActivity(new Intent(getActivity(), AddExpenseActivity.class)));
            btnAddBudget.setOnClickListener(v -> startActivity(new Intent(getActivity(), AddBudgetActivity.class)));
            btnExpenseOverview.setOnClickListener(v -> startActivity(new Intent(getActivity(), ExpenseOverviewActivity.class)));

            // Get current user ID
            userId = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE).getInt("user_id", -1);

            // Initialize repositories
            expenseRepository = new ExpenseRepository(requireContext());
            budgetRepository = new BudgetRepository(requireContext());

            // Observe totals reactively so the dashboard updates immediately on data changes
            budgetRepository.observeTotalBudgetByUserId(userId).observe(getViewLifecycleOwner(), totalBudget -> {
                currentTotalBudget = totalBudget != null ? totalBudget : 0.0;
                updateSummaryUI();
            });

            expenseRepository.observeTotalExpensesByUserId(userId).observe(getViewLifecycleOwner(), totalExpenses -> {
                currentTotalExpenses = totalExpenses != null ? totalExpenses : 0.0;
                updateSummaryUI();
            });

            // Optional: initial one-shot load as a fallback (observers will quickly update)
            loadFinancialSummary();

        } catch (Exception e) {
            Log.e(TAG, "Error in HomeFragment setup: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error initializing dashboard", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Observers will refresh automatically; keep fallback to ensure UI isn't stale
        loadFinancialSummary();
    }

    private void updateSummaryUI() {
        double remainingBudget = currentTotalBudget - currentTotalExpenses;
        int utilization = currentTotalBudget > 0 ? (int) Math.min(100, (currentTotalExpenses / currentTotalBudget) * 100) : 0;
        tvTotalSpending.setText(vndFormat.format(currentTotalExpenses));
        tvRemainingBudget.setText(vndFormat.format(remainingBudget));
        progressBudget.setProgress(utilization);
    }

    private void loadFinancialSummary() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                double totalExpenses = expenseRepository.getTotalExpensesByUserId(userId);
                double totalBudget = budgetRepository.getTotalBudgetByUserId(userId);

                if (isAdded() && getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        currentTotalExpenses = totalExpenses;
                        currentTotalBudget = totalBudget;
                        updateSummaryUI();
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading financial summary: " + e.getMessage(), e);
            }
        });
    }
}
