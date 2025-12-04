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
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ExpensesFragment extends Fragment {

    private static final String TAG = "ExpensesFragment";
    private static final String PREF_EXPENSE_SORT = "expense_sort_order";

    // Sort order constants
    private static final int SORT_AMOUNT_LOW_HIGH = 0;
    private static final int SORT_AMOUNT_HIGH_LOW = 1;
    private static final int SORT_NAME_A_Z = 2;
    private static final int SORT_NAME_Z_A = 3;

    private ExpenseRepository expenseRepository;
    private ExpenseAdapter expenseAdapter;
    private LinearLayout emptyStateContainer;
    private LinearLayout contentLayout;
    private LinearLayout expensesContainer;
    private TextView tvTotalSpent, tvExpenseCount;
    private EditText etSearchExpense;
    private Spinner spinnerSortExpense;
    private Button btnViewAllExpenses;
    private final List<Expense> allExpenses = new ArrayList<>();
    private List<Expense> filteredExpenses = new ArrayList<>();
    private int userId;
    private int currentSortOrder = SORT_AMOUNT_HIGH_LOW;
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
        return inflater.inflate(R.layout.fragment_expenses, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            // Find all UI components
            FloatingActionButton fabAddExpense = view.findViewById(R.id.fabAddExpense);
            emptyStateContainer = view.findViewById(R.id.emptyStateContainer);
            contentLayout = view.findViewById(R.id.contentLayout);
            expensesContainer = view.findViewById(R.id.expensesContainer);
            tvTotalSpent = view.findViewById(R.id.tvTotalSpent);
            tvExpenseCount = view.findViewById(R.id.tvExpenseCount);
            etSearchExpense = view.findViewById(R.id.etSearchExpense);
            spinnerSortExpense = view.findViewById(R.id.spinnerSortExpense);
            btnViewAllExpenses = view.findViewById(R.id.btnViewAllExpenses);

            userId = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE).getInt("user_id", -1);
            expenseRepository = new ExpenseRepository(requireContext());
            expenseAdapter = new ExpenseAdapter();

            // Load saved sort preference
            SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            currentSortOrder = prefs.getInt(PREF_EXPENSE_SORT, SORT_AMOUNT_HIGH_LOW);

            // Setup sort spinner
            setupSortSpinner();

            // Set up View All button
            btnViewAllExpenses.setOnClickListener(v -> {
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

    private void setupSortSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.sort_options,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSortExpense.setAdapter(adapter);
        spinnerSortExpense.setSelection(currentSortOrder);

        spinnerSortExpense.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != currentSortOrder) {
                    currentSortOrder = position;
                    saveSortPreference();
                    sortExpenses();
                    updateExpenseDisplay();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void saveSortPreference() {
        requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                .edit()
                .putInt(PREF_EXPENSE_SORT, currentSortOrder)
                .apply();
    }

    private void sortExpenses() {
        switch (currentSortOrder) {
            case SORT_AMOUNT_LOW_HIGH:
                Collections.sort(filteredExpenses, (a, b) -> Double.compare(a.getAmount(), b.getAmount()));
                break;
            case SORT_AMOUNT_HIGH_LOW:
                Collections.sort(filteredExpenses, (a, b) -> Double.compare(b.getAmount(), a.getAmount()));
                break;
            case SORT_NAME_A_Z:
                Collections.sort(filteredExpenses, (a, b) ->
                    a.getDescription().compareToIgnoreCase(b.getDescription()));
                break;
            case SORT_NAME_Z_A:
                Collections.sort(filteredExpenses, (a, b) ->
                    b.getDescription().compareToIgnoreCase(a.getDescription()));
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadExpenses();
    }

    private void loadExpenses() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                calendar.set(Calendar.DAY_OF_MONTH, 1);
                String startDate = dateFormat.format(calendar.getTime());

                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                String endDate = dateFormat.format(calendar.getTime());

                allExpenses.clear();
                filteredExpenses.clear();

                List<Expense> expenses = expenseRepository.getExpensesBetweenDatesForUser(startDate, endDate, userId);
                if (expenses != null) {
                    allExpenses.addAll(expenses);
                    filteredExpenses.addAll(expenses);
                }

                double totalAmount = 0;
                for (Expense expense : allExpenses) {
                    totalAmount += expense.getAmount();
                }

                final double finalTotalAmount = totalAmount;
                final boolean isEmpty = allExpenses.isEmpty();

                if (isAdded() && getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        try {
                            sortExpenses();
                            updateExpenseDisplay();
                            tvTotalSpent.setText(vndFormat.format(finalTotalAmount));
                            tvExpenseCount.setText(String.valueOf(allExpenses.size()));
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
        expensesContainer.removeAllViews();

        if (!filteredExpenses.isEmpty()) {
            int itemsToShow = Math.min(filteredExpenses.size(), MAX_ITEMS_TO_SHOW);

            for (int i = 0; i < itemsToShow; i++) {
                Expense expense = filteredExpenses.get(i);
                View expenseItemView = createExpenseItemView(expense);
                expensesContainer.addView(expenseItemView);
            }
        }
    }

    private View createExpenseItemView(Expense expense) {
        View itemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_expense, expensesContainer, false);

        TextView tvTitle = itemView.findViewById(R.id.tvExpenseTitle);
        TextView tvAmount = itemView.findViewById(R.id.tvExpenseAmount);
        TextView tvCategory = itemView.findViewById(R.id.tvExpenseCategory);
        TextView tvDate = itemView.findViewById(R.id.tvExpenseDate);

        tvTitle.setText(expense.getDescription());
        tvAmount.setText(vndFormat.format(expense.getAmount()));
        tvCategory.setText(expense.getCategory());
        tvDate.setText(expense.getDate());

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
                        expense.getCategory().toLowerCase(Locale.ROOT).contains(lowerCaseQuery) ||
                        String.valueOf((long) expense.getAmount()).contains(query))
                    .collect(Collectors.toList());
        }
        sortExpenses();
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

