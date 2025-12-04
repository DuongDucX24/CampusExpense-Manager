package com.example.se07101campusexpenses.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class BudgetFragment extends Fragment {

    private static final String TAG = "BudgetFragment";
    private static final String PREF_BUDGET_SORT = "budget_sort_order";

    // Sort order constants
    private static final int SORT_AMOUNT_LOW_HIGH = 0;
    private static final int SORT_AMOUNT_HIGH_LOW = 1;
    private static final int SORT_NAME_A_Z = 2;
    private static final int SORT_NAME_Z_A = 3;

    private final List<Budget> allBudgets = new ArrayList<>();
    private List<Budget> filteredBudgets = new ArrayList<>();
    private final Map<Integer, Double> budgetRemainingMap = new HashMap<>();
    private BudgetRepository budgetRepository;
    private int userId;
    private int currentSortOrder = SORT_AMOUNT_HIGH_LOW;
    private TextView tvTotalBudget, tvAvailableBudget;
    private LinearLayout emptyStateContainer;
    private LinearLayout contentLayout;
    private LinearLayout budgetsContainer;
    private EditText etSearchBudget;
    private Spinner spinnerSortBudget;
    private Button btnViewAllBudgets;
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
        return inflater.inflate(R.layout.fragment_budget, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            // Find views from the layout
            FloatingActionButton fabAddBudget = view.findViewById(R.id.fabAddBudget);
            tvTotalBudget = view.findViewById(R.id.tvTotalBudget);
            tvAvailableBudget = view.findViewById(R.id.tvAvailableBudget);
            emptyStateContainer = view.findViewById(R.id.emptyStateContainer);
            contentLayout = view.findViewById(R.id.contentLayout);
            budgetsContainer = view.findViewById(R.id.budgetsContainer);
            etSearchBudget = view.findViewById(R.id.etSearchBudget);
            spinnerSortBudget = view.findViewById(R.id.spinnerSortBudget);
            btnViewAllBudgets = view.findViewById(R.id.btnViewAllBudgets);

            userId = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE).getInt("user_id", -1);
            budgetRepository = new BudgetRepository(requireContext());

            // Load saved sort preference
            SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            currentSortOrder = prefs.getInt(PREF_BUDGET_SORT, SORT_AMOUNT_HIGH_LOW);

            // Setup sort spinner
            setupSortSpinner();

            // Set up View All button
            btnViewAllBudgets.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), AllBudgetsActivity.class);
                startActivity(intent);
            });

            // Set up search functionality
            etSearchBudget.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    filterBudgets(s.toString());
                }
            });

            // Use the FAB for adding budget
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

    private void setupSortSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.sort_options,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSortBudget.setAdapter(adapter);
        spinnerSortBudget.setSelection(currentSortOrder);

        spinnerSortBudget.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != currentSortOrder) {
                    currentSortOrder = position;
                    saveSortPreference();
                    sortBudgets();
                    updateBudgetDisplay();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void saveSortPreference() {
        requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                .edit()
                .putInt(PREF_BUDGET_SORT, currentSortOrder)
                .apply();
    }

    private void sortBudgets() {
        switch (currentSortOrder) {
            case SORT_AMOUNT_LOW_HIGH:
                Collections.sort(filteredBudgets, (a, b) -> Double.compare(a.getAmount(), b.getAmount()));
                break;
            case SORT_AMOUNT_HIGH_LOW:
                Collections.sort(filteredBudgets, (a, b) -> Double.compare(b.getAmount(), a.getAmount()));
                break;
            case SORT_NAME_A_Z:
                Collections.sort(filteredBudgets, (a, b) ->
                    a.getName().compareToIgnoreCase(b.getName()));
                break;
            case SORT_NAME_Z_A:
                Collections.sort(filteredBudgets, (a, b) ->
                    b.getName().compareToIgnoreCase(a.getName()));
                break;
        }
    }

    private void filterBudgets(String query) {
        if (query.isEmpty()) {
            filteredBudgets.clear();
            filteredBudgets.addAll(allBudgets);
        } else {
            String lowerCaseQuery = query.toLowerCase(Locale.ROOT);
            filteredBudgets = allBudgets.stream()
                    .filter(budget ->
                        budget.getName().toLowerCase(Locale.ROOT).contains(lowerCaseQuery) ||
                        (budget.getDescription() != null &&
                         budget.getDescription().toLowerCase(Locale.ROOT).contains(lowerCaseQuery)) ||
                        String.valueOf((long) budget.getAmount()).contains(query))
                    .collect(Collectors.toList());
        }
        sortBudgets();
        updateBudgetDisplay();
        toggleEmptyState(filteredBudgets.isEmpty());
    }

    @Override
    public void onResume() {
        super.onResume();
        loadBudgets();
    }

    private void loadBudgets() {
        try {
            AppDatabase.databaseWriteExecutor.execute(() -> {
                try {
                    allBudgets.clear();
                    filteredBudgets.clear();
                    budgetRemainingMap.clear();

                    List<Budget> userBudgets = budgetRepository.getBudgetsByUserId(userId);

                    double totalBudgetSum = 0;
                    double totalAvailableSum = 0;

                    ExpenseDao expenseDao = AppDatabase.getInstance(requireContext()).expenseDao();

                    if (userBudgets != null) {
                        allBudgets.addAll(userBudgets);
                        filteredBudgets.addAll(userBudgets);
                        for (Budget budget : userBudgets) {
                            String period = budget.getPeriod() != null ? budget.getPeriod() : "Monthly";
                            String[] range = getRangeForPeriod(period);
                            Double spent = expenseDao.getTotalByBudgetBetweenDates(budget.getId(), range[0], range[1]);
                            double spentVal = spent != null ? spent : 0d;
                            double remaining = Math.max(0, budget.getAmount() - spentVal);
                            budgetRemainingMap.put(budget.getId(), remaining);

                            totalBudgetSum += budget.getAmount();
                            totalAvailableSum += remaining;
                        }
                    }

                    final double finalTotalBudget = totalBudgetSum;
                    final double finalAvailable = Math.max(0, totalAvailableSum);
                    final boolean isEmpty = allBudgets.isEmpty();

                    if (isAdded() && getActivity() != null) {
                        requireActivity().runOnUiThread(() -> {
                            try {
                                sortBudgets();
                                updateBudgetDisplay();
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

    private void updateBudgetDisplay() {
        budgetsContainer.removeAllViews();

        if (!filteredBudgets.isEmpty()) {
            int itemsToShow = Math.min(filteredBudgets.size(), MAX_ITEMS_TO_SHOW);

            for (int i = 0; i < itemsToShow; i++) {
                Budget budget = filteredBudgets.get(i);
                View budgetItemView = createBudgetItemView(budget);
                budgetsContainer.addView(budgetItemView);
            }
        }
    }

    private View createBudgetItemView(Budget budget) {
        View itemView = getLayoutInflater().inflate(R.layout.item_budget, budgetsContainer, false);
        TextView tvCategory = itemView.findViewById(R.id.tvBudgetCategory);
        TextView tvAmount = itemView.findViewById(R.id.tvBudgetAmount);
        TextView tvPeriod = itemView.findViewById(R.id.tvBudgetPeriod);
        TextView tvDescription = itemView.findViewById(R.id.tvBudgetDescription);
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
        int dow = cal.get(Calendar.DAY_OF_WEEK);
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
