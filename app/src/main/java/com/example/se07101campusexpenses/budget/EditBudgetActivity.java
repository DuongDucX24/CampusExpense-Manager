package com.example.se07101campusexpenses.budget;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_budget);

        budgetDao = AppDatabase.getInstance(this).budgetDao();

        EditText edtBudgetName = findViewById(R.id.edtBudgetName);
        EditText edtBudgetAmount = findViewById(R.id.edtBudgetMoney);
        EditText edtBudgetPeriod = findViewById(R.id.edtDescription);
        Button btnSaveBudget = findViewById(R.id.btnSaveBudget);
        Button btnDeleteBudget = findViewById(R.id.btnDeleteBudget);
        Button btnBackBudget = findViewById(R.id.btnBackBudget);

        budget = (Budget) getIntent().getSerializableExtra("budget");
        if (budget != null) {
            edtBudgetName.setText(budget.getName());
            edtBudgetAmount.setText(String.valueOf(budget.getAmount()));
            edtBudgetPeriod.setText(budget.getPeriod());
        }

        btnSaveBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String budgetName = edtBudgetName.getText().toString().trim();
                String budgetAmountStr = edtBudgetAmount.getText().toString().trim();
                String budgetPeriod = edtBudgetPeriod.getText().toString().trim();

                if (TextUtils.isEmpty(budgetName) || TextUtils.isEmpty(budgetAmountStr) || TextUtils.isEmpty(budgetPeriod)) {
                    Toast.makeText(EditBudgetActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                double budgetAmount = Double.parseDouble(budgetAmountStr);

                AppDatabase.databaseWriteExecutor.execute(() -> {
                    if (budget == null) {
                        // Add new budget
                        Budget newBudget = new Budget();
                        newBudget.setName(budgetName);
                        newBudget.setAmount(budgetAmount);
                        newBudget.setPeriod(budgetPeriod);
                        budgetDao.insert(newBudget);
                        runOnUiThread(() -> {
                            Toast.makeText(EditBudgetActivity.this, "Budget saved successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    } else {
                        // Update existing budget
                        budget.setName(budgetName);
                        budget.setAmount(budgetAmount);
                        budget.setPeriod(budgetPeriod);
                        budgetDao.update(budget);
                        runOnUiThread(() -> {
                            Toast.makeText(EditBudgetActivity.this, "Budget updated successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    }
                });
            }
        });

        btnDeleteBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppDatabase.databaseWriteExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        budgetDao.delete(budget);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(EditBudgetActivity.this, "Budget deleted", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });
                    }
                });
            }
        });

        btnBackBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
