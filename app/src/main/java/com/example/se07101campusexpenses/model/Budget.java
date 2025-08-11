package com.example.se07101campusexpenses.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "budgets")
public class Budget implements Serializable { // Implemented Serializable
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;     // Corresponds to category or budget item name
    public double amount;   // The budgeted amount
    public String period;   // e.g., "Monthly", "Annually"
    public String description; // Added description
    public int userId;      // Added userId

    // Room requires a no-arg constructor if there are other constructors
    public Budget() {}

    // Updated constructor to include userId and description
    public Budget(String name, double amount, String period, String description, int userId) {
        this.name = name;
        this.amount = amount;
        this.period = period;
        this.description = description;
        this.userId = userId;
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

    public String getDescription() { // Added getter for description
        return description;
    }

    public int getUserId() { // Added getter for userId
        return userId;
    }

    // Setters
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

    public void setDescription(String description) { // Added setter for description
        this.description = description;
    }

    public void setUserId(int userId) { // Added setter for userId
        this.userId = userId;
    }
}
