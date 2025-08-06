package com.example.se07101campusexpenses.database;

import java.io.Serializable;

public class Expense implements Serializable {
    private int id;
    private String description;
    private double amount;
    private String date;
    private String category;
    private boolean recurring;
    private String recurringStartDate;
    private String recurringEndDate;

    public Expense() {
    }

    public Expense(int id, String description, double amount, String date, String category, boolean recurring, String recurringStartDate, String recurringEndDate) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.category = category;
        this.recurring = recurring;
        this.recurringStartDate = recurringStartDate;
        this.recurringEndDate = recurringEndDate;
    }

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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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
}
