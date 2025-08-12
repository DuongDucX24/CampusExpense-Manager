package com.example.se07101campusexpenses.database;

import android.content.Context;
import com.example.se07101campusexpenses.model.RecurringExpense;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class RecurringExpenseRepository {
    private final RecurringExpenseDao recurringExpenseDao;

    public RecurringExpenseRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context.getApplicationContext()); // Use application context
        recurringExpenseDao = db.recurringExpenseDao();
    }

    // This method is used by RecurringExpenseService to get all recurring expenses across all users.
    public List<RecurringExpense> getAllRecurringExpenses() {
        Future<List<RecurringExpense>> future = AppDatabase.databaseWriteExecutor.submit(recurringExpenseDao::getAllRecurringExpenses);
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Changed to match DAO method name: getAllRecurringExpensesByUserId
    public List<RecurringExpense> getAllRecurringExpensesByUserId(final int userId) {
        Future<List<RecurringExpense>> future = AppDatabase.databaseWriteExecutor.submit(() -> recurringExpenseDao.getAllRecurringExpensesByUserId(userId));
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void insert(final RecurringExpense recurringExpense) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            recurringExpenseDao.insert(recurringExpense);
        });
    }

    public void update(final RecurringExpense recurringExpense) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            recurringExpenseDao.update(recurringExpense);
        });
    }

    public void delete(final RecurringExpense recurringExpense) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            recurringExpenseDao.delete(recurringExpense);
        });
    }
}
