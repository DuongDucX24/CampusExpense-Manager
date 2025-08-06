package com.example.se07101campusexpenses.model;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface ExpenseDao {
    @Insert
    void insert(Expense expense);

    @Query("SELECT * FROM expenses WHERE userId = :userId")
    List<Expense> getExpensesByUserId(int userId);
}

