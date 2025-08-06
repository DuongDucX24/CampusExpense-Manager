package com.example.se07101campusexpenses.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.se07101campusexpenses.model.Budget;
import com.example.se07101campusexpenses.model.Expense;
import com.example.se07101campusexpenses.model.ExpenseDao;
import com.example.se07101campusexpenses.model.RecurringExpense;
import com.example.se07101campusexpenses.model.User;
import com.example.se07101campusexpenses.model.UserDao;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {RecurringExpense.class, Budget.class, User.class, Expense.class}, version = 4, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract RecurringExpenseDao recurringExpenseDao();
    public abstract BudgetDao budgetDao();
    public abstract UserDao userDao();
    public abstract ExpenseDao expenseDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "campus_expense_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
