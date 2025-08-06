package com.example.se07101campusexpenses.database;

import java.io.Serializable;

public class BudgetModel implements Serializable {
    private int id;
    private String name;
    private double amount;
    private String period;

    public BudgetModel() {
    }

    public BudgetModel(int id, String name, double amount, String period) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.period = period;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }
}
