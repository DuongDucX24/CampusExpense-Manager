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
    public String startDate; // Store as String in "YYYY-MM-DD" format
    public String endDate;   // Store as String in "YYYY-MM-DD" format
    public int userId;      // Foreign key to User table

    public RecurringExpense() {}

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
