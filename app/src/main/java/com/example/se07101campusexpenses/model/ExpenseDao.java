package com.example.se07101campusexpenses.model;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ExpenseDao {
    @Insert
    void insert(Expense expense);

    @Query("SELECT * FROM expenses WHERE userId = :userId")
    List<Expense> getExpensesByUserId(int userId);

    @Update
    void update(Expense expense);

    @Delete
    void delete(Expense expense);
}
