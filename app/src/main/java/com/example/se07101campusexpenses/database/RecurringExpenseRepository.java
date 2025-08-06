package com.example.se07101campusexpenses.database;

import android.app.Application;
import com.example.se07101campusexpenses.model.RecurringExpense;
import java.util.List;

public class RecurringExpenseRepository {
    private final RecurringExpenseDao recurringExpenseDao;
    private final List<RecurringExpense> allRecurringExpenses;

    public RecurringExpenseRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        recurringExpenseDao = db.recurringExpenseDao();
        allRecurringExpenses = recurringExpenseDao.getAllRecurringExpenses();
    }

    public List<RecurringExpense> getAllRecurringExpenses() {
        return allRecurringExpenses;
    }

    public void insert(RecurringExpense recurringExpense) {
        AppDatabase.databaseWriteExecutor.execute(() -> recurringExpenseDao.insert(recurringExpense));
    }
}
