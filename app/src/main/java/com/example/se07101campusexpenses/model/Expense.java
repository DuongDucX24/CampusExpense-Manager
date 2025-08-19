package com.example.se07101campusexpenses.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "expenses")
public class Expense implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String description;
    public String date;
    public double amount;
    public String category;
    public int userId;
    public boolean recurring;
    public String recurringStartDate;
    public String recurringEndDate;
    public Integer budgetId; // link to a single budget

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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public boolean isRecurring() {
        return recurring;
    }

    public void setRecurring(boolean recurring) {
        this.recurring = recurring;
    }

    public String getRecurringStartDate() {
        return recurringStartDate;
    }

    public void setRecurringStartDate(String recurringStartDate) {
        this.recurringStartDate = recurringStartDate;
    }

    public String getRecurringEndDate() {
        return recurringEndDate;
    }

    public void setRecurringEndDate(String recurringEndDate) {
        this.recurringEndDate = recurringEndDate;
    }

    public Integer getBudgetId() {
        return budgetId;
    }

    public void setBudgetId(Integer budgetId) {
        this.budgetId = budgetId;
    }
}
