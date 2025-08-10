package com.example.se07101campusexpenses.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.se07101campusexpenses.model.User; // Added import

@Dao
public interface UserDao {
    @Insert
    void insert(User user);

    @Query("SELECT * FROM users WHERE username = :username AND password = :password")
    User login(String username, String password);

    @Query("SELECT * FROM users WHERE username = :username")
    User getUserByUsername(String username);
}
