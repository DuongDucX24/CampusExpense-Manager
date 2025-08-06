package com.example.se07101campusexpenses.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import com.example.se07101campusexpenses.database.Expense;
import com.example.se07101campusexpenses.database.ExpenseRepository;
import com.example.se07101campusexpenses.database.RecurringExpenseRepository;
import com.example.se07101campusexpenses.model.RecurringExpense;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecurringExpenseService extends Service {

    private RecurringExpenseRepository recurringExpenseRepository;
    private ExpenseRepository expenseRepository;

    @Override
    public void onCreate() {
        super.onCreate();
        recurringExpenseRepository = new RecurringExpenseRepository(getApplication());
        expenseRepository = new ExpenseRepository(getApplication());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(() -> {
            List<RecurringExpense> recurringExpenses = recurringExpenseRepository.getAllRecurringExpenses();
            for (RecurringExpense recurringExpense : recurringExpenses) {
                if (shouldCreateExpense(recurringExpense)) {
                    createExpenseFromRecurring(recurringExpense);
                }
            }
            stopSelf();
        }).start();
        return START_NOT_STICKY;
    }

    private boolean shouldCreateExpense(RecurringExpense recurringExpense) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date startDate = sdf.parse(recurringExpense.startDate);
            Date endDate = sdf.parse(recurringExpense.endDate);
            Date today = new Date();

            if (today.after(startDate) && today.before(endDate)) {
                Calendar cal = Calendar.getInstance();
                if (recurringExpense.frequency.equalsIgnoreCase("Monthly")) {
                    // Check if an expense for this month has already been created
                    return !expenseRepository.hasExpenseForMonth(recurringExpense.description, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1);
                } else if (recurringExpense.frequency.equalsIgnoreCase("Weekly")) {
                    // Check if an expense for this week has already been created
                    return !expenseRepository.hasExpenseForWeek(recurringExpense.description, cal.get(Calendar.YEAR), cal.get(Calendar.WEEK_OF_YEAR));
                }
            }
        } catch (Exception e) {
            Log.e("RecurringExpenseService", "Error checking if expense should be created", e);
        }
        return false;
    }

    private void createExpenseFromRecurring(RecurringExpense recurringExpense) {
        com.example.se07101campusexpenses.database.Expense expense = new com.example.se07101campusexpenses.database.Expense();
        expense.setDescription(recurringExpense.description);
        expense.setAmount(recurringExpense.amount);
        expense.setCategory(recurringExpense.category);
        expense.setDate(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        // expense.setUserId(recurringExpense.userId);
        expenseRepository.addExpense(expense);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
