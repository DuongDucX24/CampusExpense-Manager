package com.example.se07101campusexpenses.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class UserRepository extends DbHelper{
    public UserRepository(@Nullable Context context) {
        super(context);
    }

    public long saveUserAccount(String username, String password, String email, String phone){
        @SuppressLint({"NewApi", "LocalSuppress"}) DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        @SuppressLint({"NewApi", "LocalSuppress"}) ZonedDateTime zone = ZonedDateTime.now();
        @SuppressLint({"NewApi", "LocalSuppress"}) String currentDate = dtf.format(zone);
        // lay ra ngay thang hien tai
        ContentValues values = new ContentValues();
        values.put(DbHelper.COL_USER_USERNAME, username); // do du lieu vao cot username
        values.put(DbHelper.COL_USER_PASSWORD, password);
        values.put(DbHelper.COL_USER_EMAIL, email);
        values.put(DbHelper.COL_USER_PHONE, phone);
        values.put(DbHelper.COL_USER_ROLE, 0); // admin
        values.put(DbHelper.COL_CREATED_AT, currentDate);
        SQLiteDatabase db = this.getWritableDatabase(); // write data into database
        long insert = db.insert(DbHelper.DB_TABLE_USER, null, values);
        db.close();
        return insert;
    }

    @SuppressLint("Range")
    public UserModel getInfoUserByUsername(String username, String password){
        UserModel user = new UserModel();
        try {
            SQLiteDatabase db = this.getReadableDatabase(); // doc du lieu(cau lenh select)
            // tao mot mang chua cac cot du lieu can truy van
            String[] columns = {
                    DbHelper.COL_USER_ID,
                    DbHelper.COL_USER_USERNAME,
                    DbHelper.COL_USER_EMAIL,
                    DbHelper.COL_USER_PHONE,
                    DbHelper.COL_USER_ROLE };
            // SELECT id, username, email, phone, role FROM user WHERE username = ? AND password = ?
            String condition = DbHelper.COL_USER_USERNAME + " =? AND " + DbHelper.COL_USER_PASSWORD + " =? ";
            String[] params = { username, password };
            // thuc thi cau lenh sql
            Cursor cursor = db.query(DbHelper.DB_TABLE_USER, columns, condition, params, null, null, null);
            if (cursor.getCount() > 0){
                // co du lieu
                cursor.moveToFirst();
                // do du lieu vao model
                user.setId(cursor.getInt(cursor.getColumnIndex(DbHelper.COL_USER_ID)));
                user.setUsername(cursor.getString(cursor.getColumnIndex(DbHelper.COL_USER_USERNAME)));
                user.setEmail(cursor.getString(cursor.getColumnIndex(DbHelper.COL_USER_EMAIL)));
                user.setPhone(cursor.getString(cursor.getColumnIndex(DbHelper.COL_USER_PHONE)));
                user.setRole(cursor.getInt(cursor.getColumnIndex(DbHelper.COL_USER_ROLE)));
            }
            cursor.close();
            db.close();
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
        return user;
    }
    
}
