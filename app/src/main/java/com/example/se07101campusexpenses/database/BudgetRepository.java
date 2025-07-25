package com.example.se07101campusexpenses.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class BudgetRepository extends DbHelper{
    public BudgetRepository(@Nullable Context context) {
        super(context);
    }

    public long addNewBudget(String name, int money, String description){
        @SuppressLint({"NewApi", "LocalSuppress"}) DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        @SuppressLint({"NewApi", "LocalSuppress"}) ZonedDateTime zone = ZonedDateTime.now();
        @SuppressLint({"NewApi", "LocalSuppress"}) String currentDate = dtf.format(zone);
        // lay ra ngay thang hien tai
        ContentValues values = new ContentValues();
        values.put(DbHelper.COL_BUDGET_NAME, name);
        values.put(DbHelper.COL_BUDGET_MONEY, money);
        values.put(DbHelper.COL_BUDGET_DESCRIPTION, description);
        values.put(DbHelper.COL_CREATED_AT, currentDate);
        SQLiteDatabase db = this.getWritableDatabase(); // ghi du lieu
        long insert = db.insert(DbHelper.DB_TABLE_BUDGET, null, values);
        db.close();
        return insert;
    }

    @SuppressLint("Range")
    public ArrayList<BudgetModel> getListBudget(){
        ArrayList<BudgetModel> budgetArrayList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase(); // doc du lieu
        Cursor cursor = db.rawQuery("SELECT * FROM " + DbHelper.DB_TABLE_BUDGET, null);
        if (cursor != null && cursor.getCount() > 0){
            if (cursor.moveToFirst()){
                do {
                    // do du lieu vao model
                    budgetArrayList.add(
                        new BudgetModel(
                            cursor.getInt(cursor.getColumnIndex(DbHelper.COL_BUDGET_ID)),
                            cursor.getString(cursor.getColumnIndex(DbHelper.COL_BUDGET_NAME)),
                            cursor.getInt(cursor.getColumnIndex(DbHelper.COL_BUDGET_MONEY)),
                            cursor.getString(cursor.getColumnIndex(DbHelper.COL_BUDGET_DESCRIPTION)),
                            cursor.getString(cursor.getColumnIndex(DbHelper.COL_CREATED_AT)),
                            cursor.getString(cursor.getColumnIndex(DbHelper.COL_UPDATED_AT))
                        )
                    );
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close();
        return  budgetArrayList;
    }
}
