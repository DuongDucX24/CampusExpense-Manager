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

import com.example.se07101campusexpenses.R;
import com.example.se07101campusexpenses.activities.AddBudgetActivity;
import com.example.se07101campusexpenses.activities.AllBudgetsActivity;
import com.example.se07101campusexpenses.activities.EditBudgetActivity;
import com.example.se07101campusexpenses.database.AppDatabase;
import com.example.se07101campusexpenses.database.BudgetRepository;
import com.example.se07101campusexpenses.database.ExpenseDao;
import com.example.se07101campusexpenses.model.Budget;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BudgetFragment extends Fragment {

    private static final String TAG = "BudgetFragment";
    private final List<Budget> budgetList = new ArrayList<>();
    private final Map<Integer, Double> budgetRemainingMap = new HashMap<>();
    private BudgetRepository budgetRepository;
    private int userId;
    private TextView tvTotalBudget, tvAvailableBudget;
    private LinearLayout emptyStateContainer;
    private LinearLayout contentLayout;
    private LinearLayout budgetsContainer;
    private TextView tvShowMoreBudgets;
    private NumberFormat vndFormat;
    private static final int MAX_ITEMS_TO_SHOW = 3;

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
                    budgetList.clear();
                    budgetRemainingMap.clear();

                    List<Budget> userBudgets = budgetRepository.getBudgetsByUserId(userId);

                    // Sum across all budgets (not only monthly) for the header totals
                    double totalBudgetSum = 0;
                    double totalAvailableSum = 0;

                    ExpenseDao expenseDao = AppDatabase.getInstance(requireContext()).expenseDao();

                    if (userBudgets != null) {
                        budgetList.addAll(userBudgets);
                        for (Budget budget : userBudgets) {
                            String period = budget.getPeriod() != null ? budget.getPeriod() : "Monthly";
                            String[] range = getRangeForPeriod(period);
                            // Use budgetId-based totals
                            Double spent = expenseDao.getTotalByBudgetBetweenDates(budget.getId(), range[0], range[1]);
                            double spentVal = spent != null ? spent : 0d;
                            double remaining = Math.max(0, budget.getAmount() - spentVal);
                            budgetRemainingMap.put(budget.getId(), remaining);

                            // Include every budget in the header totals
                            totalBudgetSum += budget.getAmount();
                            totalAvailableSum += remaining;
                        }
                    }

                    final double finalTotalBudget = totalBudgetSum;
                    final double finalAvailable = Math.max(0, totalAvailableSum);
                    final boolean isEmpty = budgetList.isEmpty();

                    if (isAdded() && getActivity() != null) {
                        requireActivity().runOnUiThread(() -> {
                            try {
                                updateBudgetDisplay(isEmpty);
                                tvTotalBudget.setText(vndFormat.format(finalTotalBudget));
                                tvAvailableBudget.setText(vndFormat.format(finalAvailable));
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
        // Inflate item with parent to ensure correct LayoutParams
        View itemView = getLayoutInflater().inflate(R.layout.item_budget, budgetsContainer, false);
        TextView tvCategory = itemView.findViewById(R.id.tvBudgetCategory);
        TextView tvAmount = itemView.findViewById(R.id.tvBudgetAmount);
        TextView tvPeriod = itemView.findViewById(R.id.tvBudgetPeriod);
        TextView tvDescription = itemView.findViewById(R.id.tvBudgetDescription);
        // Use of getIdentifier is discouraged.
        TextView tvRemaining = itemView.findViewById(R.id.tvBudgetRemaining);
        tvCategory.setText(budget.getName());
        tvAmount.setText(vndFormat.format(budget.getAmount()));
        tvPeriod.setText(budget.getPeriod());
        tvDescription.setText(budget.getDescription() != null ? budget.getDescription() : "");
        Double remaining = budgetRemainingMap.get(budget.getId());
        if (tvRemaining != null) {
            tvRemaining.setText(vndFormat.format(remaining != null ? remaining : budget.getAmount()));
        }
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

    private String[] getRangeForPeriod(String period) {
        if (period == null) return getCurrentMonthRange();
        return switch (period.toLowerCase(Locale.US)) {
            case "daily" -> getCurrentDayRange();
            case "weekly" -> getCurrentWeekRange();
            default -> getCurrentMonthRange();
        };
    }

    private String[] getCurrentDayRange() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        Calendar cal = Calendar.getInstance();
        Date day = cal.getTime();
        String d = sdf.format(day);
        return new String[]{d, d};
    }

    private String[] getCurrentWeekRange() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        Calendar cal = Calendar.getInstance();
        // make Monday the first day of week
        int dow = cal.get(Calendar.DAY_OF_WEEK); // 1=Sunday
        int diffToMonday = (dow == Calendar.SUNDAY) ? 6 : (dow - Calendar.MONDAY);
        cal.add(Calendar.DAY_OF_MONTH, -diffToMonday);
        Date start = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, 6);
        Date end = cal.getTime();
        return new String[]{sdf.format(start), sdf.format(end)};
    }

    private String[] getCurrentMonthRange() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date start = cal.getTime();
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date end = cal.getTime();
        return new String[]{sdf.format(start), sdf.format(end)};
    }
}
