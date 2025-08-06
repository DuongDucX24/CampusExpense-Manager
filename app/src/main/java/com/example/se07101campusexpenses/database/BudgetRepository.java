package com.example.se07101campusexpenses.database;

import android.app.Application;

import com.example.se07101campusexpenses.model.Budget;

import java.util.List;

public class BudgetRepository {
    private final BudgetDao budgetDao;
    private final List<Budget> allBudgets;

    public BudgetRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        budgetDao = db.budgetDao();
        allBudgets = budgetDao.getAllBudgets();
    }

    public List<Budget> getAllBudgets() {
        return allBudgets;
    }

    public void insert(Budget budget) {
        AppDatabase.databaseWriteExecutor.execute(() -> budgetDao.insert(budget));
    }

    public double getTotalBudget() {
        return budgetDao.getTotalBudget();
    }
}
