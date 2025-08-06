package com.example.se07101campusexpenses.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.se07101campusexpenses.model.Budget;
import java.util.List;

@Dao
public interface BudgetDao {
    @Insert
    void insert(Budget budget);

    @Query("SELECT * FROM budgets")
    List<Budget> getAllBudgets();

    @Query("SELECT SUM(amount) FROM budgets")
    double getTotalBudget();
}

