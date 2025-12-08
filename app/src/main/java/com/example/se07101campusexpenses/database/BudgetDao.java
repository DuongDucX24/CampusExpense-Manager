package com.example.se07101campusexpenses.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;
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

    @Query("SELECT * FROM budgets WHERE id = :id")
    Budget getBudgetById(int id);

    @Query("SELECT * FROM budgets ORDER BY name ASC")
    List<Budget> getAllBudgets(); // Potentially for all users, or should be by userId

    @Query("SELECT * FROM budgets WHERE name = :name AND period = :period LIMIT 1")
    Budget getBudgetByNameAndPeriod(String name, String period); // Might also need userId

    @Query("SELECT SUM(amount) FROM budgets WHERE userId = :userId") // Added for getTotalBudget
    double getTotalBudgetByUserId(int userId);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM budgets WHERE userId = :userId")
    LiveData<Double> observeTotalBudgetByUserId(int userId);

    @Query("SELECT * FROM budgets WHERE userId = :userId ORDER BY name ASC") // Added for getBudgetsByUserId
    List<Budget> getBudgetsByUserId(int userId);

    @Query("DELETE FROM budgets WHERE userId = :userId")
    void deleteByUserId(int userId);
}
