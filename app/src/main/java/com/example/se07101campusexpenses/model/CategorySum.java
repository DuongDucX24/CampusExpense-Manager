package com.example.se07101campusexpenses.model;

public class CategorySum {
    public String category;
    public double amount; // This will hold the SUM(amount)

    // Room might need getters if fields are private, or public fields are fine.
    // Add constructor, getters, setters if needed for your usage.

    public CategorySum() {
        // Empty constructor needed for Room
    }

    public CategorySum(String category, double amount) {
        this.category = category;
        this.amount = amount;
    }

    // Getters and setters
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    // Convenience method for pie chart data
    public double getSum() {
        return amount;
    }

    @Override
    public String toString() {
        return "CategorySum{" +
                "category='" + category + '\'' +
                ", amount=" + amount +
                '}';
    }
}
