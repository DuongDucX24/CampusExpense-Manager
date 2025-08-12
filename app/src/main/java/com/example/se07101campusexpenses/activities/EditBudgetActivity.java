package com.example.se07101campusexpenses.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.se07101campusexpenses.R;
import com.example.se07101campusexpenses.database.AppDatabase;
import com.example.se07101campusexpenses.database.BudgetDao;
import com.example.se07101campusexpenses.model.Budget;

public class EditBudgetActivity extends AppCompatActivity {
    private BudgetDao budgetDao;
    private Budget budget;
    private EditText edtBudgetName, edtBudgetAmount, edtBudgetDescription, edtBudgetPeriod;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Using activity_add_budget, ensure it has all necessary fields including btnDeleteBudget
        setContentView(R.layout.activity_add_budget);

        budgetDao = AppDatabase.getInstance(this).budgetDao();

        edtBudgetName = findViewById(R.id.edtBudgetName);
        edtBudgetAmount = findViewById(R.id.edtBudgetMoney); // Assuming edtBudgetMoney is for amount
        edtBudgetDescription = findViewById(R.id.edtBudgetDescription); // Initialize edtBudgetDescription
        edtBudgetPeriod = findViewById(R.id.edtBudgetPeriod); // Initialize edtBudgetPeriod

        Button btnSaveBudget = findViewById(R.id.btnSaveBudget);
        // Declare btnDeleteBudget
        Button btnDeleteBudget = findViewById(R.id.btnDeleteBudget); // Initialize btnDeleteBudget
        Button btnBackBudget = findViewById(R.id.btnBackBudget);

        budget = (Budget) getIntent().getSerializableExtra("budget");
        if (budget != null) {
            edtBudgetName.setText(budget.getName());
            edtBudgetAmount.setText(String.valueOf(budget.getAmount()));
            edtBudgetDescription.setText(budget.getDescription());
            edtBudgetPeriod.setText(budget.getPeriod());
        } else {
            Toast.makeText(this, "Error loading budget data.", Toast.LENGTH_SHORT).show();
            finish(); // Finish if no budget data
            return;
        }
        
        // Make delete button visible if it's part of a shared layout
        if (btnDeleteBudget != null) {
            btnDeleteBudget.setVisibility(View.VISIBLE);
        }

        btnSaveBudget.setOnClickListener(v -> {
            String budgetName = edtBudgetName.getText().toString().trim();
            String budgetAmountStr = edtBudgetAmount.getText().toString().trim();
            String budgetDescription = edtBudgetDescription.getText().toString().trim();
            String budgetPeriod = edtBudgetPeriod.getText().toString().trim();

            if (TextUtils.isEmpty(budgetName) || TextUtils.isEmpty(budgetAmountStr) || TextUtils.isEmpty(budgetPeriod) || TextUtils.isEmpty(budgetDescription)) {
                Toast.makeText(EditBudgetActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            
            double budgetAmount;
            try {
                budgetAmount = Double.parseDouble(budgetAmountStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid amount format", Toast.LENGTH_SHORT).show();
                return;
            }

            budget.setName(budgetName);
            budget.setAmount(budgetAmount);
            budget.setDescription(budgetDescription);
            budget.setPeriod(budgetPeriod);

            AppDatabase.databaseWriteExecutor.execute(() -> {
                budgetDao.update(budget);
                runOnUiThread(() -> {
                    Toast.makeText(EditBudgetActivity.this, "Budget updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
        });

        // Setup delete button listener
        if (btnDeleteBudget != null) {
            btnDeleteBudget.setOnClickListener(v -> {
                if (budget != null) {
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        budgetDao.delete(budget);
                        runOnUiThread(() -> {
                            Toast.makeText(EditBudgetActivity.this, "Budget deleted", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    });
                }
            });
        }

        btnBackBudget.setOnClickListener(v -> finish());
    }
}
