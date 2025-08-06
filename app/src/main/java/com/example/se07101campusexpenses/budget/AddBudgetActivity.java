package com.example.se07101campusexpenses.budget;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.se07101campusexpenses.MenuActivity;
import com.example.se07101campusexpenses.R;
import com.example.se07101campusexpenses.database.BudgetRepository;
import com.example.se07101campusexpenses.model.Budget;

public class AddBudgetActivity extends AppCompatActivity {
    EditText edtBudgetName, edtBudgetMoney, edtDescription;
    Button btnSaveBudget, btnBackBudget;
    BudgetRepository repository;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_budget);
        edtBudgetName  = findViewById(R.id.edtBudgetName);
        edtBudgetMoney = findViewById(R.id.edtBudgetMoney);
        edtDescription = findViewById(R.id.edtDescription);
        btnSaveBudget = findViewById(R.id.btnSaveBudget);
        btnBackBudget = findViewById(R.id.btnBackBudget);
        repository = new BudgetRepository(getApplication());
        btnSaveBudget.setOnClickListener(view -> {
            String nameBudget = edtBudgetName.getText().toString();
            String moneyBudgetStr = edtBudgetMoney.getText().toString();
            String description = edtDescription.getText().toString();
            if (nameBudget.isEmpty() || moneyBudgetStr.isEmpty()){
                Toast.makeText(this, "Please enter all values", Toast.LENGTH_SHORT).show();
                return;
            }
            double moneyBudget = Double.parseDouble(moneyBudgetStr);
            Budget budget = new Budget();
            budget.name = nameBudget;
            budget.amount = moneyBudget;
            budget.period = description; // Assuming description is used as period
            repository.insert(budget);
            Toast.makeText(this, "Add budget successfully", Toast.LENGTH_SHORT).show();
            finish();
        });
        btnBackBudget.setOnClickListener(view -> {
            finish();
        });
    }
}
