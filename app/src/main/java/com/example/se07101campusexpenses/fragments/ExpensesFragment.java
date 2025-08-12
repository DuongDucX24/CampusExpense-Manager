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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.se07101campusexpenses.R;
import com.example.se07101campusexpenses.activities.AddExpenseActivity;
import com.example.se07101campusexpenses.activities.EditExpenseActivity;
import com.example.se07101campusexpenses.adapter.ExpenseAdapter;
import com.example.se07101campusexpenses.database.AppDatabase;
import com.example.se07101campusexpenses.database.ExpenseRepository;
import com.example.se07101campusexpenses.model.Expense;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.NumberFormat;
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
    private ConstraintLayout contentLayout;
    private TextView tvTotalSpent, tvExpenseCount;
    private EditText etSearchExpense;
    private List<Expense> allExpenses = new ArrayList<>();
    private List<Expense> filteredExpenses = new ArrayList<>();
    private int userId;
    private NumberFormat vndFormat;

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
            // Find all UI components
            RecyclerView recyclerViewExpenses = view.findViewById(R.id.recyclerViewExpenses);
            FloatingActionButton fabAddExpense = view.findViewById(R.id.fabAddExpense);
            emptyStateContainer = view.findViewById(R.id.emptyStateContainer);
            contentLayout = view.findViewById(R.id.contentLayout);
            tvTotalSpent = view.findViewById(R.id.tvTotalSpent);
            tvExpenseCount = view.findViewById(R.id.tvExpenseCount);
            etSearchExpense = view.findViewById(R.id.etSearchExpense);

            userId = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE).getInt("user_id", -1);
            expenseRepository = new ExpenseRepository(requireContext());

            recyclerViewExpenses.setLayoutManager(new LinearLayoutManager(getContext()));

            // Create the adapter with empty constructor
            expenseAdapter = new ExpenseAdapter();
            recyclerViewExpenses.setAdapter(expenseAdapter);

            // Setup item click listener
            expenseAdapter.setOnItemClickListener(expense -> {
                Intent intent = new Intent(getActivity(), EditExpenseActivity.class);
                intent.putExtra("expense", expense);
                startActivity(intent);
            });

            // Setup FAB
            fabAddExpense.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), AddExpenseActivity.class);
                startActivity(intent);
            });

            // Setup search functionality
            if (etSearchExpense != null) {
                etSearchExpense.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        filterExpenses(s.toString());
                    }

                    @Override
                    public void afterTextChanged(Editable s) {}
                });
            }

            // Load data initially
            loadExpenses();
        } catch (Exception e) {
            Log.e(TAG, "Error in onViewCreated: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error setting up Expenses view", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadExpenses();  // Refresh data when returning to this fragment
    }

    private void loadExpenses() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                allExpenses = expenseRepository.getExpensesByUserId(userId);

                if (allExpenses == null) {
                    allExpenses = new ArrayList<>();
                }

                // Filter for the current month's summary
                List<Expense> currentMonthExpenses = filterCurrentMonthExpenses(allExpenses);

                // Calculate totals for current month
                double totalMonthlySpent = 0;
                for (Expense expense : currentMonthExpenses) {
                    totalMonthlySpent += expense.getAmount();
                }

                // Apply current search filter
                String currentSearchQuery = etSearchExpense != null ? etSearchExpense.getText().toString() : "";

                // Get a final reference for the lambda
                final double finalTotalMonthlySpent = totalMonthlySpent;
                final int currentExpenseCount = currentMonthExpenses.size();

                // Filter expenses based on search
                filterExpenses(currentSearchQuery);

                // Make sure fragment is still attached before updating UI
                if (isAdded() && getActivity() != null && !getActivity().isFinishing()) {
                    requireActivity().runOnUiThread(() -> {
                        try {
                            // Update UI with the new data
                            updateExpenseList();

                            // Update monthly summary
                            if (tvTotalSpent != null) {
                                tvTotalSpent.setText(vndFormat.format(finalTotalMonthlySpent));
                            }

                            if (tvExpenseCount != null) {
                                tvExpenseCount.setText(String.valueOf(currentExpenseCount));
                            }

                            // Show/hide empty state
                            toggleEmptyState(filteredExpenses.isEmpty());
                        } catch (Exception e) {
                            Log.e(TAG, "Error updating UI: " + e.getMessage(), e);
                        }
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading expenses: " + e.getMessage(), e);
            }
        });
    }

    private List<Expense> filterCurrentMonthExpenses(List<Expense> expenses) {
        if (expenses == null) return new ArrayList<>();

        // Get current month and year
        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH) + 1; // Calendar months are 0-indexed
        int currentYear = calendar.get(Calendar.YEAR);

        List<Expense> filteredList = new ArrayList<>();

        for (Expense expense : expenses) {
            try {
                String[] dateParts = expense.getDate().split("/");
                if (dateParts.length == 3) {
                    int month = Integer.parseInt(dateParts[1]);
                    int year = Integer.parseInt(dateParts[2]);

                    if (month == currentMonth && year == currentYear) {
                        filteredList.add(expense);
                    }
                }
            } catch (Exception e) {
                // Skip expenses with invalid dates
                Log.w(TAG, "Skipping expense with invalid date: " + expense.getDate());
            }
        }

        return filteredList;
    }

    private void filterExpenses(String query) {
        if (allExpenses == null) return;

        try {
            if (query.isEmpty()) {
                filteredExpenses = new ArrayList<>(allExpenses);
            } else {
                String lowerCaseQuery = query.toLowerCase();
                filteredExpenses = allExpenses.stream()
                    .filter(expense -> {
                        String description = expense.getDescription();
                        String category = expense.getCategory();
                        return (description != null && description.toLowerCase().contains(lowerCaseQuery)) ||
                               (category != null && category.toLowerCase().contains(lowerCaseQuery));
                    })
                    .collect(Collectors.toList());
            }

            if (isAdded()) {
                updateExpenseList();
                toggleEmptyState(filteredExpenses.isEmpty());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error filtering expenses: " + e.getMessage(), e);
        }
    }

    private void updateExpenseList() {
        try {
            // First set to null to force a full refresh
            expenseAdapter.submitList(null);
            // Then submit the actual list
            expenseAdapter.submitList(new ArrayList<>(filteredExpenses));
        } catch (Exception e) {
            Log.e(TAG, "Error updating expense list: " + e.getMessage(), e);
        }
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