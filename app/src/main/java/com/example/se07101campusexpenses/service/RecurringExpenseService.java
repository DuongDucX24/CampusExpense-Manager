package com.example.se07101campusexpenses.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import com.example.se07101campusexpenses.model.Expense; // Corrected import
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
            // TODO: Fetch recurring expenses for a specific user or handle multi-user scenario if needed.
            // For now, assuming getAllRecurringExpenses() is appropriate or will be adapted.
            List<RecurringExpense> recurringExpenses = recurringExpenseRepository.getAllRecurringExpenses();
            if (recurringExpenses != null) {
                for (RecurringExpense recurringExpense : recurringExpenses) {
                    if (shouldCreateExpense(recurringExpense)) {
                        createExpenseFromRecurring(recurringExpense);
                    }
                }
            }
            stopSelf();
        }).start();
        return START_NOT_STICKY;
    }

    private boolean shouldCreateExpense(RecurringExpense recurringExpense) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date startDate = sdf.parse(recurringExpense.getStartDate()); // Use getter
            Date endDate = sdf.parse(recurringExpense.getEndDate());     // Use getter
            Date today = new Date();

            if (today.after(startDate) && today.before(endDate)) {
                Calendar cal = Calendar.getInstance();
                if (recurringExpense.getFrequency().equalsIgnoreCase("Monthly")) { // Use getter
                    // Check if an expense for this month has already been created
                    return !expenseRepository.hasExpenseForMonth(recurringExpense.getDescription(), recurringExpense.getUserId(), cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1);
                } else if (recurringExpense.getFrequency().equalsIgnoreCase("Weekly")) { // Use getter
                    // Check if an expense for this week has already been created
                    return !expenseRepository.hasExpenseForWeek(recurringExpense.getDescription(), recurringExpense.getUserId(), cal.get(Calendar.YEAR), cal.get(Calendar.WEEK_OF_YEAR));
                }
            }
        } catch (Exception e) {
            Log.e("RecurringExpenseService", "Error checking if expense should be created", e);
        }
        return false;
    }

    private void createExpenseFromRecurring(RecurringExpense recurringExpense) {
        Expense expense = new Expense(); 
        expense.setDescription(recurringExpense.getDescription());
        expense.setAmount(recurringExpense.getAmount());
        expense.setCategory(recurringExpense.getCategory());
        expense.setDate(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        expense.setUserId(recurringExpense.getUserId()); 
        expense.setRecurring(true); // Mark that this expense came from a recurring source
        expenseRepository.addExpense(expense);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
