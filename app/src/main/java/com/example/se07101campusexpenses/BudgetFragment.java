package com.example.se07101campusexpenses;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.se07101campusexpenses.adapter.BudgetRVAdapter;
import com.example.se07101campusexpenses.budget.AddBudgetActivity;
import com.example.se07101campusexpenses.budget.EditBudgetActivity;
import com.example.se07101campusexpenses.database.AppDatabase;
import com.example.se07101campusexpenses.database.BudgetDao;
import com.example.se07101campusexpenses.model.Budget;

import java.util.ArrayList;
import java.util.List;

public class BudgetFragment extends Fragment {

    private BudgetRVAdapter budgetRVAdapter;
    private final List<Budget> budgetList = new ArrayList<>();
    private BudgetDao budgetDao;
    private int userId;

    public BudgetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_budget, container, false);
        Button btnCreateBudget = view.findViewById(R.id.btnCreateBudget);
        RecyclerView budgetRv = view.findViewById(R.id.rvBudget);

        userId = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE).getInt("user_id", -1);
        budgetDao = AppDatabase.getInstance(getContext()).budgetDao();

        budgetRVAdapter = new BudgetRVAdapter(budgetList, getContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        budgetRv.setLayoutManager(linearLayoutManager);
        budgetRv.setAdapter(budgetRVAdapter);

        budgetRVAdapter.setOnClickListener(position -> {
            Budget budget = budgetList.get(position);
            Intent intent = new Intent(getActivity(), EditBudgetActivity.class);
            intent.putExtra("budget", budget);
            startActivity(intent);
        });

        btnCreateBudget.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddBudgetActivity.class);
            startActivity(intent);
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadBudgets();
    }

    private void loadBudgets() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Budget> budgets = budgetDao.getBudgetsByUserId(userId);
            requireActivity().runOnUiThread(() -> {
                budgetList.clear();
                budgetList.addAll(budgets);
                budgetRVAdapter.notifyDataSetChanged();
            });
        });
    }
}