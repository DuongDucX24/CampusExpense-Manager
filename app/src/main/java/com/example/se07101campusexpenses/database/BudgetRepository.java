package com.example.se07101campusexpenses.database;

import android.content.Context;
import androidx.lifecycle.LiveData;

import com.example.se07101campusexpenses.model.Budget;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class BudgetRepository {
    private final BudgetDao budgetDao;

    public BudgetRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context.getApplicationContext()); // Use application context
        budgetDao = db.budgetDao();
    }

    public List<Budget> getAllBudgets() { // This likely should be by userId too
        Future<List<Budget>> future = AppDatabase.databaseWriteExecutor.submit(() -> budgetDao.getAllBudgets());
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void insert(final Budget budget) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            budgetDao.insert(budget);
        });
    }

    public void update(final Budget budget) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            budgetDao.update(budget);
        });
    }

    public void delete(final Budget budget) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            budgetDao.delete(budget);
        });
    }

    // Changed to getTotalBudgetByUserId to match DAO and accept userId
    public double getTotalBudgetByUserId(int userId) {
        Future<Double> future = AppDatabase.databaseWriteExecutor.submit(() -> budgetDao.getTotalBudgetByUserId(userId));
        try {
            Double result = future.get();
            return result != null ? result : 0.0;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    public LiveData<Double> observeTotalBudgetByUserId(int userId) {
        return budgetDao.observeTotalBudgetByUserId(userId);
    }

    public List<Budget> getBudgetsByUserId(int userId) {
        Future<List<Budget>> future = AppDatabase.databaseWriteExecutor.submit(() -> budgetDao.getBudgetsByUserId(userId));
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }
}
