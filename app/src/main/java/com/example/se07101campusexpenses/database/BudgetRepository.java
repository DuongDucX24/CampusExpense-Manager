package com.example.se07101campusexpenses.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BudgetRepository extends DbHelper {
    public BudgetRepository(@Nullable Context context) {
        super(context);
    }

    public long addBudget(BudgetModel budget) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbHelper.COL_BUDGET_NAME, budget.getName());
        values.put(DbHelper.COL_BUDGET_AMOUNT, budget.getAmount());
        values.put(DbHelper.COL_BUDGET_PERIOD, budget.getPeriod());
        long id = db.insert(DbHelper.DB_TABLE_BUDGET, null, values);
        db.close();
        return id;
    }

    public int updateBudget(BudgetModel budget) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbHelper.COL_BUDGET_NAME, budget.getName());
        values.put(DbHelper.COL_BUDGET_AMOUNT, budget.getAmount());
        values.put(DbHelper.COL_BUDGET_PERIOD, budget.getPeriod());
        return db.update(DbHelper.DB_TABLE_BUDGET, values, DbHelper.COL_BUDGET_ID + " = ?",
                new String[]{String.valueOf(budget.getId())});
    }

    public void deleteBudget(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(DbHelper.DB_TABLE_BUDGET, DbHelper.COL_BUDGET_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
    }

    @SuppressLint("Range")
    public List<BudgetModel> getAllBudgets() {
        List<BudgetModel> budgets = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + DbHelper.DB_TABLE_BUDGET;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                BudgetModel budget = new BudgetModel();
                budget.setId(cursor.getInt(cursor.getColumnIndex(DbHelper.COL_BUDGET_ID)));
                budget.setName(cursor.getString(cursor.getColumnIndex(DbHelper.COL_BUDGET_NAME)));
                budget.setAmount(cursor.getDouble(cursor.getColumnIndex(DbHelper.COL_BUDGET_AMOUNT)));
                budget.setPeriod(cursor.getString(cursor.getColumnIndex(DbHelper.COL_BUDGET_PERIOD)));
                budgets.add(budget);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return budgets;
    }
}
