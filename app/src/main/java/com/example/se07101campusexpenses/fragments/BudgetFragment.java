package com.example.se07101campusexpenses.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.se07101campusexpenses.R;
import com.example.se07101campusexpenses.activities.AddBudgetActivity;
import com.example.se07101campusexpenses.activities.EditBudgetActivity;
import com.example.se07101campusexpenses.adapter.BudgetRVAdapter;
import com.example.se07101campusexpenses.database.AppDatabase;
import com.example.se07101campusexpenses.database.BudgetRepository;
import com.example.se07101campusexpenses.model.Budget;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BudgetFragment extends Fragment {

    private static final String TAG = "BudgetFragment";
    private BudgetRVAdapter budgetRVAdapter;
    private final List<Budget> budgetList = new ArrayList<>();
    private BudgetRepository budgetRepository;
    private int userId;
    private TextView tvTotalBudget, tvBudgetCount;
    private LinearLayout emptyStateBudget;
    private ConstraintLayout contentLayout;
    private NumberFormat vndFormat;

    public BudgetFragment() {
        // Required empty public constructor
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vndFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        vndFormat.setMaximumFractionDigits(0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_budget, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            // Find views from the new layout
            FloatingActionButton fabAddBudget = view.findViewById(R.id.fabAddBudget);
            RecyclerView budgetRv = view.findViewById(R.id.rvBudget);
            tvTotalBudget = view.findViewById(R.id.tvTotalBudget);
            tvBudgetCount = view.findViewById(R.id.tvBudgetCount);
            emptyStateBudget = view.findViewById(R.id.emptyStateBudget);
            contentLayout = view.findViewById(R.id.contentLayout);

            userId = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE).getInt("user_id", -1);
            budgetRepository = new BudgetRepository(requireContext());

            budgetRVAdapter = new BudgetRVAdapter(budgetList, getContext());
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
            budgetRv.setLayoutManager(linearLayoutManager);
            budgetRv.setAdapter(budgetRVAdapter);

            // Corrected lambda to match OnClickListener interface (int position, Budget budget)
            budgetRVAdapter.setOnClickListener((position, budget) -> {
                Intent intent = new Intent(getActivity(), EditBudgetActivity.class);
                intent.putExtra("budget", budget);
                startActivity(intent);
            });

            // Use the FAB instead of the button
            fabAddBudget.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), AddBudgetActivity.class);
                startActivity(intent);
            });

            // Load budgets initially
            loadBudgets();

        } catch (Exception e) {
            Log.e(TAG, "Error setting up BudgetFragment: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error initializing budget view", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadBudgets();  // Refresh data when returning to this fragment
    }

    private void loadBudgets() {
        try {
            AppDatabase.databaseWriteExecutor.execute(() -> {
                try {
                    // Clear the current list
                    budgetList.clear();

                    // Get updated budgets for the current user
                    List<Budget> userBudgets = budgetRepository.getBudgetsByUserId(userId);

                    // Calculate the total budget amount
                    double totalBudget = 0;
                    if (userBudgets != null) {
                        budgetList.addAll(userBudgets);
                        for (Budget budget : userBudgets) {
                            totalBudget += budget.getAmount();
                        }
                    }

                    // Get final reference for lambda
                    final double finalTotalBudget = totalBudget;
                    final boolean isEmpty = budgetList.isEmpty();

                    // Make sure fragment is still attached before updating UI
                    if (isAdded() && getActivity() != null) {
                        requireActivity().runOnUiThread(() -> {
                            try {
                                // Update UI
                                budgetRVAdapter.notifyDataSetChanged();

                                // Update summary data
                                tvTotalBudget.setText(vndFormat.format(finalTotalBudget));
                                tvBudgetCount.setText(String.valueOf(budgetList.size()));

                                // Toggle visibility based on data presence
                                toggleEmptyState(isEmpty);

                            } catch (Exception e) {
                                Log.e(TAG, "Error updating UI: " + e.getMessage(), e);
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error loading budgets: " + e.getMessage(), e);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error executing budget load: " + e.getMessage(), e);
        }
    }

    private void toggleEmptyState(boolean isEmpty) {
        if (isEmpty) {
            emptyStateBudget.setVisibility(View.VISIBLE);
            contentLayout.setVisibility(View.GONE);
        } else {
            emptyStateBudget.setVisibility(View.GONE);
            contentLayout.setVisibility(View.VISIBLE);
        }
    }
}