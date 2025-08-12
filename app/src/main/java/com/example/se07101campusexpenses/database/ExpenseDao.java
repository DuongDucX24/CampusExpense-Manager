package com.example.se07101campusexpenses.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;
import com.example.se07101campusexpenses.model.Expense;
import com.example.se07101campusexpenses.model.CategorySum;
import java.util.List;

@Dao
public interface ExpenseDao {
    @Insert
    void insert(Expense expense);

    @Update
    void update(Expense expense);

    @Delete
    void delete(Expense expense);

    @Query("DELETE FROM expenses WHERE id = :id") // Added for deleteById
    void deleteById(int id);

    @Query("SELECT * FROM expenses WHERE id = :id")
    Expense getExpenseById(int id);

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    List<Expense> getAllExpenses(); // This might need to be user-specific

    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY date DESC") // Added
    List<Expense> getExpensesByUserId(int userId);

    @Query("SELECT SUM(amount) FROM expenses WHERE userId = :userId") // Added, assumes total for a user
    double getTotalExpensesByUserId(int userId);
    
    @Query("SELECT SUM(amount) FROM expenses") // Added for global total, if needed
    double getTotalExpenses();

    // This query returns a list of expenses, not grouped by category. 
    // If you need sums per category for a specific user, it would be different.
    // If you need sums per category for a specific user, it would be different.
    // For now, returning all expenses for a user, or global if no user context.
    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY category, date DESC") // Added
    List<Expense> getExpensesByCategoryAndUserId(int userId);

    @Query("SELECT category, SUM(amount) as amount FROM expenses WHERE userId = :userId GROUP BY category") // Added for sums per category for a user
    List<CategorySum> getCategorySumsByUserId(int userId);

    // Placeholders for RecurringExpenseService needs
    @Query("SELECT COUNT(*) > 0 FROM expenses WHERE description = :description AND userId = :userId AND SUBSTR(date, 1, 7) = :yearMonth")
    boolean hasExpenseForMonth(String description, int userId, String yearMonth); // yearMonth format YYYY-MM

    // Week checking is more complex with SUBSTR and depends on SQLite date functions available/used
    // For simplicity, this is a placeholder and might need a more robust date checking strategy.
    // @Query("SELECT COUNT(*) > 0 FROM expenses WHERE description = :description AND userId = :userId AND strftime('%Y-%W', date) = :yearWeek")
    // boolean hasExpenseForWeek(String description, int userId, String yearWeek); // yearWeek format YYYY-WW

    @Query("SELECT * FROM expenses WHERE date BETWEEN :startDate AND :endDate")
    List<Expense> getExpensesBetweenDates(String startDate, String endDate); // Global, or add userId

    @Query("SELECT category, SUM(amount) as amount FROM expenses WHERE date BETWEEN :startDate AND :endDate GROUP BY category")
    List<CategorySum> getExpensesByCategoryBetweenDates(String startDate, String endDate); // Global, or add userId
}
