package com.example.se07101campusexpenses.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "budgets")
public class Budget {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
    public double amount;
    public String period;
    public int userId;

    public String getName() {
        return name;
    }

    public double getAmount() {
        return amount;
    }
}

