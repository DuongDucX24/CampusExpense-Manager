package com.example.se07101campusexpenses.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "recurring_expenses")
public class RecurringExpense {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String description;
    public double amount;
    public String category;
    public String frequency; // e.g., "Monthly", "Weekly"
    public String startDate;
    public String endDate;
    public int userId;
}

