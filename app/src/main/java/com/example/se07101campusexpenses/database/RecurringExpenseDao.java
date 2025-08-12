package com.example.se07101campusexpenses.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;
import com.example.se07101campusexpenses.model.RecurringExpense;
import java.util.List;

@Dao
public interface RecurringExpenseDao {
    @Insert
    void insert(RecurringExpense recurringExpense);

    @Update
    void update(RecurringExpense recurringExpense);

    @Delete
    void delete(RecurringExpense recurringExpense);

    @Query("SELECT * FROM recurring_expenses WHERE id = :id")
    RecurringExpense getRecurringExpenseById(int id);

    @Query("SELECT * FROM recurring_expenses WHERE userId = :userId ORDER BY startDate DESC")
    List<RecurringExpense> getAllRecurringExpensesByUserId(int userId);

    @Query("SELECT * FROM recurring_expenses") // Needed by RecurringExpenseService
    List<RecurringExpense> getAllRecurringExpenses();
}
