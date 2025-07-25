package com.example.se07101campusexpenses.database;

public class BudgetModel {
    private int id;
    private String budgetName;
    private int budgetMoney;
    private String budgetDescription;
    private String createdAt;
    private String updatedAt;

    public BudgetModel(int id, String budgetName, int budgetMoney, String budgetDescription, String createdAt, String updatedAt){
        this.id = id;
        this.budgetName = budgetName;
        this.budgetMoney = budgetMoney;
        this.budgetDescription = budgetDescription;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBudgetName() {
        return budgetName;
    }

    public void setBudgetName(String budgetName) {
        this.budgetName = budgetName;
    }

    public int getBudgetMoney() {
        return budgetMoney;
    }

    public void setBudgetMoney(int budgetMoney) {
        this.budgetMoney = budgetMoney;
    }

    public String getBudgetDescription() {
        return budgetDescription;
    }

    public void setBudgetDescription(String budgetDescription) {
        this.budgetDescription = budgetDescription;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
