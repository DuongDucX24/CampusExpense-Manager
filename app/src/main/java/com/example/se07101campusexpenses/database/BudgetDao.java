package com.example.se07101campusexpenses.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.se07101campusexpenses.model.Budget;
import java.util.List;

@Dao
public interface BudgetDao {
    @Insert
    void insert(Budget budget);

    @Update
    void update(Budget budget);

    @Delete
    void delete(Budget budget);

    @Query("SELECT * FROM budgets")
    List<Budget> getAllBudgets();

    @Query("SELECT * FROM budgets WHERE userId = :userId")
    List<Budget> getBudgetsByUserId(int userId);

    @Query("SELECT SUM(amount) FROM budgets")
    double getTotalBudget();
}
