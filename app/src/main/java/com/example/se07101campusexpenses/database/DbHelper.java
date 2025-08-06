package com.example.se07101campusexpenses.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DbHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "CampusExpense";
    public static final int DB_VERSION = 2;

    // Table User
    public static final String DB_TABLE_USER = "user";
    public static final String COL_USER_ID = "id";
    public static final String COL_USER_USERNAME = "username";
    public static final String COL_USER_PASSWORD = "password";
    public static final String COL_USER_EMAIL = "email";
    public static final String COL_USER_PHONE = "phone";
    public static final String COL_USER_ROLE = "role";
    public static final String COL_CREATED_AT = "created_at";

    // Table Budget
    public static final String DB_TABLE_BUDGET = "budget";
    public static final String COL_BUDGET_ID = "id";
    public static final String COL_BUDGET_NAME = "name";
    public static final String COL_BUDGET_AMOUNT = "amount";
    public static final String COL_BUDGET_PERIOD = "period";

    // Table Expenses
    public static final String DB_TABLE_EXPENSES = "expenses";
    public static final String COL_EXPENSE_ID = "id";
    public static final String COL_EXPENSE_DESCRIPTION = "description";
    public static final String COL_EXPENSE_AMOUNT = "amount";
    public static final String COL_EXPENSE_DATE = "date";
    public static final String COL_EXPENSE_CATEGORY = "category";
    public static final String COL_EXPENSE_RECURRING = "recurring";
    public static final String COL_EXPENSE_RECURRING_START_DATE = "recurring_start_date";
    public static final String COL_EXPENSE_RECURRING_END_DATE = "recurring_end_date";


    public DbHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // sql create table user
        String sql_user = "CREATE TABLE " + DB_TABLE_USER + "(" +
                COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USER_USERNAME + " TEXT NOT NULL, " +
                COL_USER_PASSWORD + " TEXT NOT NULL, " +
                COL_USER_EMAIL + " TEXT NOT NULL, " +
                COL_USER_PHONE + " TEXT NOT NULL, " +
                COL_USER_ROLE + " INTEGER, " +
                COL_CREATED_AT + " TEXT NOT NULL" +
                ")";
        db.execSQL(sql_user);

        // sql create table budget
        String sql_budget = "CREATE TABLE " + DB_TABLE_BUDGET + "(" +
                COL_BUDGET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_BUDGET_NAME + " TEXT NOT NULL, " +
                COL_BUDGET_AMOUNT + " REAL NOT NULL, " +
                COL_BUDGET_PERIOD + " TEXT NOT NULL" +
                ")";
        db.execSQL(sql_budget);

        // sql create table expenses
        String sql_expenses = "CREATE TABLE " + DB_TABLE_EXPENSES + "(" +
                COL_EXPENSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_EXPENSE_DESCRIPTION + " TEXT NOT NULL, " +
                COL_EXPENSE_AMOUNT + " REAL NOT NULL, " +
                COL_EXPENSE_DATE + " TEXT NOT NULL, " +
                COL_EXPENSE_CATEGORY + " TEXT NOT NULL, " +
                COL_EXPENSE_RECURRING + " INTEGER DEFAULT 0, " +
                COL_EXPENSE_RECURRING_START_DATE + " TEXT, " +
                COL_EXPENSE_RECURRING_END_DATE + " TEXT" +
                ")";
        db.execSQL(sql_expenses);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + DB_TABLE_USER + " ADD COLUMN " + COL_USER_PHONE + " TEXT NOT NULL DEFAULT ''");
            db.execSQL("ALTER TABLE " + DB_TABLE_USER + " ADD COLUMN " + COL_USER_ROLE + " INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE " + DB_TABLE_USER + " ADD COLUMN " + COL_CREATED_AT + " TEXT NOT NULL DEFAULT ''");
            String sql_budget = "CREATE TABLE " + DB_TABLE_BUDGET + "(" +
                    COL_BUDGET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_BUDGET_NAME + " TEXT NOT NULL, " +
                    COL_BUDGET_AMOUNT + " REAL NOT NULL, " +
                    COL_BUDGET_PERIOD + " TEXT NOT NULL" +
                    ")";
            db.execSQL(sql_budget);
            String sql_expenses = "CREATE TABLE " + DB_TABLE_EXPENSES + "(" +
                    COL_EXPENSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_EXPENSE_DESCRIPTION + " TEXT NOT NULL, " +
                    COL_EXPENSE_AMOUNT + " REAL NOT NULL, " +
                    COL_EXPENSE_DATE + " TEXT NOT NULL, " +
                    COL_EXPENSE_CATEGORY + " TEXT NOT NULL, " +
                    COL_EXPENSE_RECURRING + " INTEGER DEFAULT 0, " +
                    COL_EXPENSE_RECURRING_START_DATE + " TEXT, " +
                    COL_EXPENSE_RECURRING_END_DATE + " TEXT" +
                    ")";
            db.execSQL(sql_expenses);
        }
    }
}
