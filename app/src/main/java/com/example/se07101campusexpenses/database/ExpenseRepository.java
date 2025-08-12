package com.example.se07101campusexpenses.database;

import android.content.Context;
import com.example.se07101campusexpenses.model.Expense;
import com.example.se07101campusexpenses.database.ExpenseDao;
import com.example.se07101campusexpenses.model.CategorySum;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ExpenseRepository {
    private final ExpenseDao expenseDao;

    public ExpenseRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context.getApplicationContext());
        expenseDao = db.expenseDao();
    }

    public void addExpense(final Expense expense) {
        AppDatabase.databaseWriteExecutor.execute(() -> expenseDao.insert(expense));
    }

    public void updateExpense(final Expense expense) {
        AppDatabase.databaseWriteExecutor.execute(() -> expenseDao.update(expense));
    }

    public void deleteExpense(final int expenseId) { // Changed to accept id
        AppDatabase.databaseWriteExecutor.execute(() -> expenseDao.deleteById(expenseId));
    }

    public List<Expense> getAllExpenses() { // Consider if this should be user-specific
        Future<List<Expense>> future = AppDatabase.databaseWriteExecutor.submit(expenseDao::getAllExpenses);
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace(); 
            return null;
        }
    }

    public List<Expense> getExpensesByUserId(int userId) {
        Future<List<Expense>> future = AppDatabase.databaseWriteExecutor.submit(() -> expenseDao.getExpensesByUserId(userId));
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public double getTotalExpensesByUserId(int userId) {
        Future<Double> future = AppDatabase.databaseWriteExecutor.submit(() -> expenseDao.getTotalExpensesByUserId(userId));
        try {
            Double result = future.get();
            return result != null ? result : 0.0;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return 0.0;
        }
    }
    
    public double getTotalExpenses() { // Global total
        Future<Double> future = AppDatabase.databaseWriteExecutor.submit(expenseDao::getTotalExpenses);
        try {
            Double result = future.get();
            return result != null ? result : 0.0;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    // Returns sums of expenses grouped by category for a specific user
    public List<CategorySum> getCategorySumsByUserId(int userId) {
        Future<List<CategorySum>> future = AppDatabase.databaseWriteExecutor.submit(() -> expenseDao.getCategorySumsByUserId(userId));
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Expense> getExpensesBetweenDates(String startDate, String endDate) {
        Future<List<Expense>> future = AppDatabase.databaseWriteExecutor.submit(() -> expenseDao.getExpensesBetweenDates(startDate, endDate));
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<CategorySum> getExpensesByCategoryBetweenDates(String startDate, String endDate) {
        Future<List<CategorySum>> future = AppDatabase.databaseWriteExecutor.submit(() -> expenseDao.getExpensesByCategoryBetweenDates(startDate, endDate));
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean hasExpenseForMonth(String description, int userId, int year, int month) {
        String yearMonth = String.format(Locale.US, "%d-%02d", year, month);
        Future<Boolean> future = AppDatabase.databaseWriteExecutor.submit(() -> expenseDao.hasExpenseForMonth(description, userId, yearMonth));
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean hasExpenseForWeek(String description, int userId, int year, int weekOfYear) {
        // String yearWeek = String.format(Locale.US, "%d-%02d", year, weekOfYear);
        // Future<Boolean> future = AppDatabase.databaseWriteExecutor.submit(() -> expenseDao.hasExpenseForWeek(description, userId, yearWeek));
        // try {
        // return future.get();
        // } catch (InterruptedException | ExecutionException e) {
        // e.printStackTrace();
        // return false;
        // }
        return false; // DAO method for week is still a placeholder
    }
}
