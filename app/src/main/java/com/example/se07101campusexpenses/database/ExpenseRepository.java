package com.example.se07101campusexpenses.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExpenseRepository {
    private final DbHelper dbHelper;

    public ExpenseRepository(Context context) {
        dbHelper = new DbHelper(context);
    }

    public long addExpense(Expense expense) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbHelper.COL_EXPENSE_DESCRIPTION, expense.getDescription());
        values.put(DbHelper.COL_EXPENSE_AMOUNT, expense.getAmount());
        values.put(DbHelper.COL_EXPENSE_DATE, expense.getDate());
        values.put(DbHelper.COL_EXPENSE_CATEGORY, expense.getCategory());
        values.put(DbHelper.COL_EXPENSE_RECURRING, expense.isRecurring() ? 1 : 0);
        values.put(DbHelper.COL_EXPENSE_RECURRING_START_DATE, expense.getRecurringStartDate());
        values.put(DbHelper.COL_EXPENSE_RECURRING_END_DATE, expense.getRecurringEndDate());
        long id = db.insert(DbHelper.DB_TABLE_EXPENSES, null, values);
        db.close();
        return id;
    }

    public int updateExpense(Expense expense) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbHelper.COL_EXPENSE_DESCRIPTION, expense.getDescription());
        values.put(DbHelper.COL_EXPENSE_AMOUNT, expense.getAmount());
        values.put(DbHelper.COL_EXPENSE_DATE, expense.getDate());
        values.put(DbHelper.COL_EXPENSE_CATEGORY, expense.getCategory());
        values.put(DbHelper.COL_EXPENSE_RECURRING, expense.isRecurring() ? 1 : 0);
        values.put(DbHelper.COL_EXPENSE_RECURRING_START_DATE, expense.getRecurringStartDate());
        values.put(DbHelper.COL_EXPENSE_RECURRING_END_DATE, expense.getRecurringEndDate());
        return db.update(DbHelper.DB_TABLE_EXPENSES, values, DbHelper.COL_EXPENSE_ID + " = ?",
                new String[]{String.valueOf(expense.getId())});
    }

    public void deleteExpense(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DbHelper.DB_TABLE_EXPENSES, DbHelper.COL_EXPENSE_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
    }

    @SuppressLint("Range")
    public List<Expense> getAllExpenses() {
        List<Expense> expenses = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + DbHelper.DB_TABLE_EXPENSES;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Expense expense = new Expense();
                expense.setId(cursor.getInt(cursor.getColumnIndex(DbHelper.COL_EXPENSE_ID)));
                expense.setDescription(cursor.getString(cursor.getColumnIndex(DbHelper.COL_EXPENSE_DESCRIPTION)));
                expense.setAmount(cursor.getDouble(cursor.getColumnIndex(DbHelper.COL_EXPENSE_AMOUNT)));
                expense.setDate(cursor.getString(cursor.getColumnIndex(DbHelper.COL_EXPENSE_DATE)));
                expense.setCategory(cursor.getString(cursor.getColumnIndex(DbHelper.COL_EXPENSE_CATEGORY)));
                expense.setRecurring(cursor.getInt(cursor.getColumnIndex(DbHelper.COL_EXPENSE_RECURRING)) == 1);
                expense.setRecurringStartDate(cursor.getString(cursor.getColumnIndex(DbHelper.COL_EXPENSE_RECURRING_START_DATE)));
                expense.setRecurringEndDate(cursor.getString(cursor.getColumnIndex(DbHelper.COL_EXPENSE_RECURRING_END_DATE)));
                expenses.add(expense);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return expenses;
    }

    @SuppressLint("Range")
    public boolean hasExpenseForMonth(String description, int year, int month) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + DbHelper.DB_TABLE_EXPENSES +
                " WHERE " + DbHelper.COL_EXPENSE_DESCRIPTION + " = ? AND " +
                "strftime('%Y', " + DbHelper.COL_EXPENSE_DATE + ") = ? AND " +
                "strftime('%m', " + DbHelper.COL_EXPENSE_DATE + ") = ?";
        Cursor cursor = db.rawQuery(query, new String[]{description, String.valueOf(year), String.format(Locale.US, "%02d", month)});
        boolean exists = false;
        if (cursor.moveToFirst()) {
            exists = cursor.getInt(0) > 0;
        }
        cursor.close();
        db.close();
        return exists;
    }

    @SuppressLint("Range")
    public boolean hasExpenseForWeek(String description, int year, int weekOfYear) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + DbHelper.DB_TABLE_EXPENSES +
                " WHERE " + DbHelper.COL_EXPENSE_DESCRIPTION + " = ? AND " +
                "strftime('%Y', " + DbHelper.COL_EXPENSE_DATE + ") = ? AND " +
                "strftime('%W', " + DbHelper.COL_EXPENSE_DATE + ") = ?";
        Cursor cursor = db.rawQuery(query, new String[]{description, String.valueOf(year), String.format(Locale.US, "%02d", weekOfYear)});
        boolean exists = false;
        if (cursor.moveToFirst()) {
            exists = cursor.getInt(0) > 0;
        }
        cursor.close();
        db.close();
        return exists;
    }

    @SuppressLint("Range")
    public double getTotalExpensesForCategory(String category) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT SUM(" + DbHelper.COL_EXPENSE_AMOUNT + ") FROM " + DbHelper.DB_TABLE_EXPENSES +
                " WHERE " + DbHelper.COL_EXPENSE_CATEGORY + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{category});
        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        db.close();
        return total;
    }

    @SuppressLint("Range")
    public double getTotalExpenses() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT SUM(" + DbHelper.COL_EXPENSE_AMOUNT + ") FROM " + DbHelper.DB_TABLE_EXPENSES;
        Cursor cursor = db.rawQuery(query, null);
        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        db.close();
        return total;
    }

    @SuppressLint("Range")
    public List<Expense> getExpensesBetweenDates(String startDate, String endDate) {
        List<Expense> expenses = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + DbHelper.DB_TABLE_EXPENSES +
                " WHERE " + DbHelper.COL_EXPENSE_DATE + " BETWEEN ? AND ?";
        Cursor cursor = db.rawQuery(query, new String[]{startDate, endDate});
        if (cursor.moveToFirst()) {
            do {
                Expense expense = new Expense();
                expense.setId(cursor.getInt(cursor.getColumnIndex(DbHelper.COL_EXPENSE_ID)));
                expense.setDescription(cursor.getString(cursor.getColumnIndex(DbHelper.COL_EXPENSE_DESCRIPTION)));
                expense.setAmount(cursor.getDouble(cursor.getColumnIndex(DbHelper.COL_EXPENSE_AMOUNT)));
                expense.setDate(cursor.getString(cursor.getColumnIndex(DbHelper.COL_EXPENSE_DATE)));
                expense.setCategory(cursor.getString(cursor.getColumnIndex(DbHelper.COL_EXPENSE_CATEGORY)));
                expense.setRecurring(cursor.getInt(cursor.getColumnIndex(DbHelper.COL_EXPENSE_RECURRING)) == 1);
                expense.setRecurringStartDate(cursor.getString(cursor.getColumnIndex(DbHelper.COL_EXPENSE_RECURRING_START_DATE)));
                expense.setRecurringEndDate(cursor.getString(cursor.getColumnIndex(DbHelper.COL_EXPENSE_RECURRING_END_DATE)));
                expenses.add(expense);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return expenses;
    }

    @SuppressLint("Range")
    public List<Expense> getExpensesByCategory() {
        List<Expense> expenses = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT " + DbHelper.COL_EXPENSE_CATEGORY + ", SUM(" + DbHelper.COL_EXPENSE_AMOUNT + ") as " + DbHelper.COL_EXPENSE_AMOUNT +
                " FROM " + DbHelper.DB_TABLE_EXPENSES +
                " GROUP BY " + DbHelper.COL_EXPENSE_CATEGORY;
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                Expense expense = new Expense();
                expense.setCategory(cursor.getString(cursor.getColumnIndex(DbHelper.COL_EXPENSE_CATEGORY)));
                expense.setAmount(cursor.getDouble(cursor.getColumnIndex(DbHelper.COL_EXPENSE_AMOUNT)));
                expenses.add(expense);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return expenses;
    }

    @SuppressLint("Range")
    public List<Expense> getExpensesByCategoryBetweenDates(String startDate, String endDate) {
        List<Expense> expenses = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT " + DbHelper.COL_EXPENSE_CATEGORY + ", SUM(" + DbHelper.COL_EXPENSE_AMOUNT + ") as " + DbHelper.COL_EXPENSE_AMOUNT +
                " FROM " + DbHelper.DB_TABLE_EXPENSES +
                " WHERE " + DbHelper.COL_EXPENSE_DATE + " BETWEEN ? AND ?" +
                " GROUP BY " + DbHelper.COL_EXPENSE_CATEGORY;
        Cursor cursor = db.rawQuery(query, new String[]{startDate, endDate});
        if (cursor.moveToFirst()) {
            do {
                Expense expense = new Expense();
                expense.setCategory(cursor.getString(cursor.getColumnIndex(DbHelper.COL_EXPENSE_CATEGORY)));
                expense.setAmount(cursor.getDouble(cursor.getColumnIndex(DbHelper.COL_EXPENSE_AMOUNT)));
                expenses.add(expense);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return expenses;
    }
}
