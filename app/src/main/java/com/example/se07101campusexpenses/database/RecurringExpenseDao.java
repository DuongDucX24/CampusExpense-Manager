package com.example.se07101campusexpenses.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.se07101campusexpenses.model.RecurringExpense;
import java.util.List;

@Dao
public interface RecurringExpenseDao {
    @Insert
    void insert(RecurringExpense recurringExpense);

    @Query("SELECT * FROM recurring_expenses WHERE userId = :userId")
    List<RecurringExpense> getRecurringExpensesByUser(int userId);

    @Query("SELECT * FROM recurring_expenses")
    List<RecurringExpense> getAllRecurringExpenses();
}
