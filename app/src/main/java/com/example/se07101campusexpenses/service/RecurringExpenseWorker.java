package com.example.se07101campusexpenses.service;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.se07101campusexpenses.database.AppDatabase;
import com.example.se07101campusexpenses.database.ExpenseDao;
import com.example.se07101campusexpenses.database.RecurringExpenseDao;
import com.example.se07101campusexpenses.model.Expense;
import com.example.se07101campusexpenses.model.RecurringExpense;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecurringExpenseWorker extends Worker {

    private static final String TAG = "RecurringExpenseWorker";

    public RecurringExpenseWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Worker is running...");
        try {
            RecurringExpenseDao recurringExpenseDao = AppDatabase.getInstance(getApplicationContext()).recurringExpenseDao();
            ExpenseDao expenseDao = AppDatabase.getInstance(getApplicationContext()).expenseDao();
            List<RecurringExpense> recurringExpenses = recurringExpenseDao.getAll();

            for (RecurringExpense recurring : recurringExpenses) {
                if (isExpenseDue(recurring)) {
                    createExpenseFromRecurring(recurring, expenseDao);
                }
            }
            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Error in recurring expense worker", e);
            return Result.failure();
        }
    }

    private boolean isExpenseDue(RecurringExpense recurring) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date startDate = sdf.parse(recurring.getStartDate());
            Date endDate = sdf.parse(recurring.getEndDate());
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            if (today.getTime().after(startDate) && today.getTime().before(endDate)) {
                Calendar startCal = Calendar.getInstance();
                startCal.setTime(startDate);

                switch (recurring.getFrequency().toLowerCase()) {
                    case "daily":
                        return true;
                    case "weekly":
                        return startCal.get(Calendar.DAY_OF_WEEK) == today.get(Calendar.DAY_OF_WEEK);
                    case "monthly":
                        return startCal.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing date", e);
        }
        return false;
    }

    private void createExpenseFromRecurring(RecurringExpense recurring, ExpenseDao expenseDao) {
        Expense expense = new Expense();
        expense.setUserId(recurring.getUserId());
        expense.setDescription(recurring.getDescription());
        expense.setAmount(recurring.getAmount());
        expense.setCategory(recurring.getCategory());
        expense.setDate(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()));
        expense.setRecurring(true);
        expenseDao.insert(expense);
        Log.d(TAG, "Created expense from recurring: " + recurring.getDescription());
    }
}

