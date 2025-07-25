package com.example.se07101campusexpenses.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "campus_expenses"; // ten csdl
    private static final int DB_VERSION = 2; // phien ban

    // khai bao bang du lieu va cac cot trong bang du lieu
    // dinh nghia bang user
    protected static final String DB_TABLE_USER = "user";
    protected static final String COL_USER_ID = "id";
    protected static final String COL_USER_USERNAME = "username";
    protected static final String COL_USER_PASSWORD = "password";
    protected static final String COL_USER_EMAIL = "email";
    protected static final String COL_USER_PHONE = "phone";
    protected static final String COL_USER_ROLE = "role";

    // dung chung cac bang - 2 truong nay
    protected static final String COL_CREATED_AT = "created_at";
    protected static final String COL_UPDATED_AT = "updated_at";

    // dinh nghia bang budget
    protected static final String DB_TABLE_BUDGET = "budget";
    protected static final String COL_BUDGET_ID = "id";
    protected static final String COL_BUDGET_NAME = "budget_name";
    protected static final String COL_BUDGET_MONEY = "budget_money";
    protected static final String COL_BUDGET_DESCRIPTION = "description";

    public DbHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // tao bang user
        String createUserTable = "CREATE TABLE " + DB_TABLE_USER + " ( "
                                + COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                                + COL_USER_USERNAME + " VARCHAR(60) NOT NULL, "
                                + COL_USER_PASSWORD + " VARCHAR(200) NOT NULL, "
                                + COL_USER_EMAIL + " VARCHAR(100) NOT NULL, "
                                + COL_USER_PHONE + " VARCHAR(20), "
                                + COL_USER_ROLE + " INTEGER DEFAULT(0), "
                                + COL_CREATED_AT + " DATETIME, "
                                + COL_UPDATED_AT + " DATETIME ) ";
        // tao bang budget
        String createBudgetTable = "CREATE TABLE " + DB_TABLE_BUDGET + " ( "
                                   + COL_BUDGET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                                   + COL_BUDGET_NAME + " VARCHAR(150) NOT NULL, "
                                   + COL_BUDGET_MONEY + " INTEGER NOT NULL, "
                                   + COL_BUDGET_DESCRIPTION + " TEXT, "
                                   + COL_CREATED_AT + " DATETIME, "
                                   + COL_UPDATED_AT + " DATETIME ) ";
        db.execSQL(createUserTable);
        db.execSQL(createBudgetTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_USER); // xoa bang neu co loi
            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_BUDGET); // xoa bang budget
            onCreate(db); // tao lai bang
        }
    }
}
