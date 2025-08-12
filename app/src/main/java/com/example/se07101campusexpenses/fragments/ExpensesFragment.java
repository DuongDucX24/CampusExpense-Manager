package com.example.se07101campusexpenses.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.se07101campusexpenses.R;
import com.example.se07101campusexpenses.activities.AddExpenseActivity;
import com.example.se07101campusexpenses.activities.EditExpenseActivity;
import com.example.se07101campusexpenses.adapter.ExpenseAdapter;
import com.example.se07101campusexpenses.database.AppDatabase; // Added for executor
import com.example.se07101campusexpenses.database.ExpenseRepository;
import com.example.se07101campusexpenses.model.Expense;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ExpensesFragment extends Fragment {

    private ExpenseRepository expenseRepository;
    private ExpenseAdapter expenseAdapter;
    private final List<Expense> expenseList = new ArrayList<>();
    private int userId;

    public ExpensesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        RecyclerView recyclerViewExpenses = view.findViewById(R.id.recyclerViewExpenses);
        FloatingActionButton fabAddExpense = view.findViewById(R.id.fabAddExpense);

        userId = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE).getInt("user_id", -1);
        expenseRepository = new ExpenseRepository(requireContext());

        recyclerViewExpenses.setLayoutManager(new LinearLayoutManager(getContext()));
        expenseAdapter = new ExpenseAdapter(expenseList);
        recyclerViewExpenses.setAdapter(expenseAdapter);

        expenseAdapter.setOnItemClickListener(expense -> {
            Intent intent = new Intent(getActivity(), EditExpenseActivity.class);
            intent.putExtra("expense", expense);
            startActivity(intent);
        });

        fabAddExpense.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddExpenseActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadExpenses();
    }

    private void loadExpenses() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Expense> expenses = expenseRepository.getExpensesByUserId(userId);
            requireActivity().runOnUiThread(() -> {
                if (expenses != null) {
                    expenseList.clear();
                    expenseList.addAll(expenses);
                    expenseAdapter.notifyDataSetChanged();
                }
            });
        });
    }
}