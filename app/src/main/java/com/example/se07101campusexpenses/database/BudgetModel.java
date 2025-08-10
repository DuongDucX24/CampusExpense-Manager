package com.example.se07101campusexpenses.database;

public class BudgetModel {
    private String name;
    private double amount;
    private String period;
    private int id; // Assuming an ID field might be useful for database operations

    // Constructor
    public BudgetModel(String name, double amount, String period) {
        this.name = name;
        this.amount = amount;
        this.period = period;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getAmount() {
        return amount;
    }

    public String getPeriod() {
        return period;
    }

    // Setters (optional, but good practice if fields might be updated)
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setPeriod(String period) {
        this.period = period;
    }
}
