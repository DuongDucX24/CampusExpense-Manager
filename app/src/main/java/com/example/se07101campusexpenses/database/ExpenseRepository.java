package com.example.se07101campusexpenses.database;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.example.se07101campusexpenses.model.Expense;
import com.example.se07101campusexpenses.database.ExpenseDao;
import com.example.se07101campusexpenses.model.CategorySum;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ExpenseRepository {
    private final ExpenseDao expenseDao;

    public ExpenseRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context.getApplicationContext());
        expenseDao = db.expenseDao();
    }

    public void addExpense(final Expense expense) {
        AppDatabase.databaseWriteExecutor.execute(() -> expenseDao.insert(expense));
    }

    public void updateExpense(final Expense expense) {
        AppDatabase.databaseWriteExecutor.execute(() -> expenseDao.update(expense));
    }

    public void deleteExpense(final int expenseId) { // Changed to accept id
        AppDatabase.databaseWriteExecutor.execute(() -> expenseDao.deleteById(expenseId));
    }

    public List<Expense> getAllExpenses() { // Consider if this should be user-specific
        Future<List<Expense>> future = AppDatabase.databaseWriteExecutor.submit(expenseDao::getAllExpenses);
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace(); 
            return null;
        }
    }

    public List<Expense> getExpensesByUserId(int userId) {
        Future<List<Expense>> future = AppDatabase.databaseWriteExecutor.submit(() -> expenseDao.getExpensesByUserId(userId));
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public double getTotalExpensesByUserId(int userId) {
        Future<Double> future = AppDatabase.databaseWriteExecutor.submit(() -> expenseDao.getTotalExpensesByUserId(userId));
        try {
            Double result = future.get();
            return result != null ? result : 0.0;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return 0.0;
        }
    }
    
    public double getTotalExpenses() { // Global total
        Future<Double> future = AppDatabase.databaseWriteExecutor.submit(expenseDao::getTotalExpenses);
        try {
            Double result = future.get();
            return result != null ? result : 0.0;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    // Returns sums of expenses grouped by category for a specific user
    public List<CategorySum> getCategorySumsByUserId(int userId) {
        Future<List<CategorySum>> future = AppDatabase.databaseWriteExecutor.submit(() -> expenseDao.getCategorySumsByUserId(userId));
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Expense> getExpensesBetweenDates(String startDate, String endDate) {
        Future<List<Expense>> future = AppDatabase.databaseWriteExecutor.submit(() -> expenseDao.getExpensesBetweenDates(startDate, endDate));
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets expenses between dates for a specific user
     */
    public List<Expense> getExpensesBetweenDatesForUser(String startDate, String endDate, int userId) {
        Future<List<Expense>> future = AppDatabase.databaseWriteExecutor.submit(() -> {
            List<Expense> allExpenses = expenseDao.getExpensesBetweenDates(startDate, endDate);
            List<Expense> userExpenses = new ArrayList<>();

            for (Expense expense : allExpenses) {
                if (expense.getUserId() == userId) {
                    userExpenses.add(expense);
                }
            }

            return userExpenses;
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<CategorySum> getExpensesByCategoryBetweenDates(String startDate, String endDate) {
        Future<List<CategorySum>> future = AppDatabase.databaseWriteExecutor.submit(() -> expenseDao.getExpensesByCategoryBetweenDates(startDate, endDate));
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets expenses by category for the current month for a specific user
     */
    public List<CategorySum> getExpensesByCategoryForCurrentMonth(int userId) {
        // Get current month in format DD/MM/YYYY
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int year = calendar.get(java.util.Calendar.YEAR);
        int month = calendar.get(java.util.Calendar.MONTH) + 1; // Calendar months are 0-based

        // First day of month
        String startDate = "01/" + String.format(Locale.US, "%02d", month) + "/" + year;

        // Last day of month (approximation - could be improved)
        int lastDay = calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);
        String endDate = lastDay + "/" + String.format(Locale.US, "%02d", month) + "/" + year;

        Future<List<CategorySum>> future = AppDatabase.databaseWriteExecutor.submit(() ->
            expenseDao.getExpensesByCategoryBetweenDatesForUser(startDate, endDate, userId));
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets monthly expense totals for the last six months for a specific user
     */
    public java.util.Map<String, Double> getMonthlyTotalsForLastSixMonths(int userId) {
        // Use LinkedHashMap to maintain insertion order (chronological order of months)
        java.util.Map<String, Double> results = new java.util.LinkedHashMap<>();
        java.text.SimpleDateFormat monthFormat = new java.text.SimpleDateFormat("MMM yyyy", Locale.US);
        java.text.SimpleDateFormat dbDateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy", Locale.US);
        
        // Current calendar for iterations
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        
        // Get data for the last 6 months in chronological order
        for (int i = 5; i >= 0; i--) {
            // Set to current month minus i
            java.util.Calendar tempCal = (java.util.Calendar) calendar.clone();
            tempCal.add(java.util.Calendar.MONTH, -i);
            
            // Set to first day of month
            tempCal.set(java.util.Calendar.DAY_OF_MONTH, 1);
            java.util.Date firstDayOfMonth = tempCal.getTime();
            
            // Set to last day of month
            tempCal.set(java.util.Calendar.DAY_OF_MONTH, tempCal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH));
            java.util.Date lastDayOfMonth = tempCal.getTime();
            
            // Format for database query
            String startDate = dbDateFormat.format(firstDayOfMonth);
            String endDate = dbDateFormat.format(lastDayOfMonth);
            
            // Generate month label with full year for clarity
            String monthLabel = monthFormat.format(firstDayOfMonth);
            
            try {
                Future<Double> future = AppDatabase.databaseWriteExecutor.submit(() -> 
                    expenseDao.getTotalExpensesBetweenDatesForUser(startDate, endDate, userId));
                Double total = future.get();
                results.put(monthLabel, total != null ? total : 0.0);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                results.put(monthLabel, 0.0);
            }
        }
        
        return results;
    }

    /**
     * Checks if an expense with the given description exists for a specific month
     */
    public boolean hasExpenseForMonth(String description, int userId, int year, int month) {
        Future<Boolean> future = AppDatabase.databaseWriteExecutor.submit(() ->
            expenseDao.hasExpenseForMonth(description, userId, year, month));
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Checks if an expense with the given description exists for a specific week
     */
    public boolean hasExpenseForWeek(String description, int userId, int year, int week) {
        Future<Boolean> future = AppDatabase.databaseWriteExecutor.submit(() ->
            expenseDao.hasExpenseForWeek(description, userId, year, week));
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gets a limited number of the most recent expenses for a specific user
     *
     * @param userId User ID to get expenses for
     * @param limit Maximum number of expenses to return
     * @return List of the most recent expenses
     */
    public List<Expense> getRecentExpensesByUserId(int userId, int limit) {
        Future<List<Expense>> future = AppDatabase.databaseWriteExecutor.submit(() -> {
            List<Expense> allUserExpenses = expenseDao.getExpensesByUserId(userId);

            if (allUserExpenses == null || allUserExpenses.isEmpty()) {
                return new ArrayList<>();
            }

            // Sort expenses by date (assuming date format DD/MM/YYYY)
            allUserExpenses.sort((e1, e2) -> {
                try {
                    // Parse dates and compare (most recent first)
                    String[] date1Parts = e1.getDate().split("/");
                    String[] date2Parts = e2.getDate().split("/");

                    int year1 = Integer.parseInt(date1Parts[2]);
                    int month1 = Integer.parseInt(date1Parts[1]);
                    int day1 = Integer.parseInt(date1Parts[0]);

                    int year2 = Integer.parseInt(date2Parts[2]);
                    int month2 = Integer.parseInt(date2Parts[1]);
                    int day2 = Integer.parseInt(date2Parts[0]);

                    if (year1 != year2) return year2 - year1;
                    if (month1 != month2) return month2 - month1;
                    return day2 - day1;
                } catch (Exception e) {
                    return 0; // Default to no change in order if date parsing fails
                }
            });

            // Return at most 'limit' expenses
            return allUserExpenses.subList(0, Math.min(limit, allUserExpenses.size()));
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public LiveData<Double> observeTotalExpensesByUserId(int userId) {
        return expenseDao.observeTotalExpensesByUserId(userId);
    }
}
