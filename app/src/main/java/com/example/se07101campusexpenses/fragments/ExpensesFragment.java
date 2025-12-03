package com.example.se07101campusexpenses.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.se07101campusexpenses.R;
import com.example.se07101campusexpenses.activities.AddExpenseActivity;
import com.example.se07101campusexpenses.activities.AllExpensesActivity;
import com.example.se07101campusexpenses.activities.EditExpenseActivity;
import com.example.se07101campusexpenses.adapter.ExpenseAdapter;
import com.example.se07101campusexpenses.database.AppDatabase;
import com.example.se07101campusexpenses.database.ExpenseRepository;
import com.example.se07101campusexpenses.model.Expense;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ExpensesFragment extends Fragment {

    private static final String TAG = "ExpensesFragment";
    private ExpenseRepository expenseRepository;
    private ExpenseAdapter expenseAdapter;
    private LinearLayout emptyStateContainer;
    private LinearLayout contentLayout;
    private LinearLayout expensesContainer;
    private TextView tvTotalSpent, tvExpenseCount;
    private TextView tvShowMoreExpenses;
    private EditText etSearchExpense;
    private final List<Expense> allExpenses = new ArrayList<>();
    private List<Expense> filteredExpenses = new ArrayList<>();
    private int userId;
    private NumberFormat vndFormat;
    private static final int MAX_ITEMS_TO_SHOW = 4;

    public ExpensesFragment() {
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
        return inflater.inflate(R.layout.fragment_expenses, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            // Find all UI components for new layout
            FloatingActionButton fabAddExpense = view.findViewById(R.id.fabAddExpense);
            emptyStateContainer = view.findViewById(R.id.emptyStateContainer);
            contentLayout = view.findViewById(R.id.contentLayout);
            expensesContainer = view.findViewById(R.id.expensesContainer);
            tvTotalSpent = view.findViewById(R.id.tvTotalSpent);
            tvExpenseCount = view.findViewById(R.id.tvExpenseCount);
            etSearchExpense = view.findViewById(R.id.etSearchExpense);
            tvShowMoreExpenses = view.findViewById(R.id.tvShowMoreExpenses);

            userId = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE).getInt("user_id", -1);
            expenseRepository = new ExpenseRepository(requireContext());

            // Create the adapter for reference (but won't use recyclerView anymore)
            expenseAdapter = new ExpenseAdapter();

            // Set up "Show More" button
            tvShowMoreExpenses.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), AllExpensesActivity.class);
                startActivity(intent);
            });

            // Set up search functionality
            etSearchExpense.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    filterExpenses(s.toString());
                }
            });

            // Setup FAB click listener
            fabAddExpense.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), AddExpenseActivity.class);
                startActivity(intent);
            });

            // Load expenses initially
            loadExpenses();

        } catch (Exception e) {
            Log.e(TAG, "Error setting up ExpensesFragment: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error initializing expenses view", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadExpenses(); // Refresh when returning to the fragment
    }

    private void loadExpenses() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // Get current month's bounds
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                // Set to first day of month
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                String startDate = dateFormat.format(calendar.getTime());

                // Set to last day of month
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                String endDate = dateFormat.format(calendar.getTime());

                // Clear and load new data
                allExpenses.clear();
                filteredExpenses.clear();

                List<Expense> expenses = expenseRepository.getExpensesBetweenDatesForUser(startDate, endDate, userId);
                if (expenses != null) {
                    allExpenses.addAll(expenses);
                    filteredExpenses.addAll(expenses);
                }

                // Calculate total amount
                double totalAmount = 0;
                for (Expense expense : allExpenses) {
                    totalAmount += expense.getAmount();
                }

                final double finalTotalAmount = totalAmount;
                final boolean isEmpty = allExpenses.isEmpty();

                // Update UI on main thread if fragment is still attached
                if (isAdded() && getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        try {
                            // Update expense display with limited items
                            updateExpenseDisplay();

                            // Update summary data
                            tvTotalSpent.setText(vndFormat.format(finalTotalAmount));
                            tvExpenseCount.setText(String.valueOf(allExpenses.size()));

                            // Toggle empty state visibility
                            toggleEmptyState(isEmpty);
                        } catch (Exception e) {
                            Log.e(TAG, "Error updating UI with expense data: " + e.getMessage(), e);
                        }
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading expenses: " + e.getMessage(), e);
            }
        });
    }

    private void updateExpenseDisplay() {
        if (!filteredExpenses.isEmpty()) {
            // Clear container
            expensesContainer.removeAllViews();

            // Show limited number of items
            int itemsToShow = Math.min(filteredExpenses.size(), MAX_ITEMS_TO_SHOW);

            // Add expense items to container
            for (int i = 0; i < itemsToShow; i++) {
                Expense expense = filteredExpenses.get(i);
                View expenseItemView = createExpenseItemView(expense);
                expensesContainer.addView(expenseItemView);
            }

            // Show/hide "Show More" button
            tvShowMoreExpenses.setVisibility(filteredExpenses.size() > MAX_ITEMS_TO_SHOW ? View.VISIBLE : View.GONE);
        }
    }

    private View createExpenseItemView(Expense expense) {
        // Inflate a layout for individual expense items with the proper parent
        View itemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_expense, expensesContainer, false);

        // Get references to views in the item layout
        TextView tvTitle = itemView.findViewById(R.id.tvExpenseTitle);
        TextView tvAmount = itemView.findViewById(R.id.tvExpenseAmount);
        TextView tvCategory = itemView.findViewById(R.id.tvExpenseCategory);
        TextView tvDate = itemView.findViewById(R.id.tvExpenseDate);

        // Populate with expense data
        tvTitle.setText(expense.getDescription());
        tvAmount.setText(vndFormat.format(expense.getAmount()));
        tvCategory.setText(expense.getCategory());
        tvDate.setText(expense.getDate());

        // Set click listener
        itemView.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditExpenseActivity.class);
            intent.putExtra("expense", expense);
            startActivity(intent);
        });

        return itemView;
    }

    private void filterExpenses(String query) {
        if (query.isEmpty()) {
            filteredExpenses.clear();
            filteredExpenses.addAll(allExpenses);
        } else {
            String lowerCaseQuery = query.toLowerCase(Locale.ROOT);
            filteredExpenses = allExpenses.stream()
                    .filter(expense ->
                        expense.getDescription().toLowerCase(Locale.ROOT).contains(lowerCaseQuery) ||
                        expense.getCategory().toLowerCase(Locale.ROOT).contains(lowerCaseQuery))
                    .collect(Collectors.toList());
        }
        updateExpenseDisplay();
        toggleEmptyState(filteredExpenses.isEmpty());
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