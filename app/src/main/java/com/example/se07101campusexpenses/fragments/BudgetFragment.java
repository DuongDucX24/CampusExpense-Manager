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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.se07101campusexpenses.R;
import com.example.se07101campusexpenses.activities.AddBudgetActivity;
import com.example.se07101campusexpenses.activities.AllBudgetsActivity;
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
    private TextView tvTotalBudget, tvAvailableBudget;
    private LinearLayout emptyStateContainer;
    private LinearLayout contentLayout;
    private LinearLayout budgetsContainer;
    private TextView tvShowMoreBudgets;
    private NumberFormat vndFormat;
    private static final int MAX_ITEMS_TO_SHOW = 4;

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
            tvTotalBudget = view.findViewById(R.id.tvTotalBudget);
            tvAvailableBudget = view.findViewById(R.id.tvAvailableBudget);
            emptyStateContainer = view.findViewById(R.id.emptyStateContainer);
            contentLayout = view.findViewById(R.id.contentLayout);
            budgetsContainer = view.findViewById(R.id.budgetsContainer);
            tvShowMoreBudgets = view.findViewById(R.id.tvShowMoreBudgets);

            userId = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE).getInt("user_id", -1);
            budgetRepository = new BudgetRepository(requireContext());

            // Set up the budgetRVAdapter (we'll use it differently now)
            budgetRVAdapter = new BudgetRVAdapter(budgetList, getContext());

            // Set up click listener for "Show More" button
            tvShowMoreBudgets.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), AllBudgetsActivity.class);
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
                                // Update UI with the new data
                                updateBudgetDisplay(isEmpty);

                                // Update summary data
                                tvTotalBudget.setText(vndFormat.format(finalTotalBudget));
                                tvAvailableBudget.setText(vndFormat.format(finalTotalBudget)); // You might want to subtract expenses here

                                // Toggle visibility based on data presence
                                toggleEmptyState(isEmpty);
                                
                            } catch (Exception e) {
                                Log.e(TAG, "Error updating UI with budget data: " + e.getMessage(), e);
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error loading budgets: " + e.getMessage(), e);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error executing budget loading task: " + e.getMessage(), e);
        }
    }

    private void updateBudgetDisplay(boolean isEmpty) {
        if (!isEmpty) {
            // Clear previous views
            budgetsContainer.removeAllViews();

            // Determine how many items to show
            int itemsToShow = Math.min(budgetList.size(), MAX_ITEMS_TO_SHOW);

            // Add budget items to the container
            for (int i = 0; i < itemsToShow; i++) {
                Budget budget = budgetList.get(i);
                View budgetItemView = createBudgetItemView(budget);
                budgetsContainer.addView(budgetItemView);
            }

            // Show or hide the "Show More" button
            tvShowMoreBudgets.setVisibility(budgetList.size() > MAX_ITEMS_TO_SHOW ? View.VISIBLE : View.GONE);
        }
    }

    private View createBudgetItemView(Budget budget) {
        // Inflate a simple item view for each budget
        View itemView = getLayoutInflater().inflate(R.layout.item_budget, null);

        // Set budget data to the view
        TextView tvCategory = itemView.findViewById(R.id.tvBudgetCategory);
        TextView tvAmount = itemView.findViewById(R.id.tvBudgetAmount);
        TextView tvPeriod = itemView.findViewById(R.id.tvBudgetPeriod);
        TextView tvDescription = itemView.findViewById(R.id.tvBudgetDescription);

        // Populate the views with data
        tvCategory.setText(budget.getName());
        tvAmount.setText(vndFormat.format(budget.getAmount()));
        tvPeriod.setText(budget.getPeriod());
        tvDescription.setText(budget.getDescription() != null ? budget.getDescription() : "");

        // Add click listener to open budget details
        itemView.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditBudgetActivity.class);
            intent.putExtra("budget", budget);
            startActivity(intent);
        });

        return itemView;
    }

    private void toggleEmptyState(boolean isEmpty) {
        if (isEmpty) {
            emptyStateContainer.setVisibility(View.VISIBLE);
            contentLayout.setVisibility(View.GONE);
        } else {
            emptyStateContainer.setVisibility(View.GONE);
            contentLayout.setVisibility(View.VISIBLE);
        }
    }
}
