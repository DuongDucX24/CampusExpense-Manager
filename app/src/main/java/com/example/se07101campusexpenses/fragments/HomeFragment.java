package com.example.se07101campusexpenses.fragments;

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

import com.example.se07101campusexpenses.R;
import com.example.se07101campusexpenses.database.AppDatabase;
import com.example.se07101campusexpenses.database.BudgetRepository;
import com.example.se07101campusexpenses.database.ExpenseRepository;

import java.text.NumberFormat;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    private TextView tvTotalSpending, tvRemainingBudget;
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

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (!isAdded() || getView() == null) {
                                Log.w(TAG, "Fragment not attached or view destroyed, skipping UI update.");
                                return;
                            }
                            updateSummary(totalSpending, remainingBudget);
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
}
