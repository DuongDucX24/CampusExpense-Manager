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
        Button btnSaveBudget = findViewById(R.id.btnSaveBudget);
        Button btnDeleteBudget = findViewById(R.id.btnDeleteBudget);
        Button btnBackBudget = findViewById(R.id.btnBackBudget);

        budget = (Budget) getIntent().getSerializableExtra("budget");
        if (budget != null) {
            edtBudgetName.setText(budget.getCategory());
            edtBudgetAmount.setText(String.valueOf(budget.getAmount()));
        }

        btnSaveBudget.setOnClickListener(v -> {
            String budgetName = edtBudgetName.getText().toString().trim();
            String budgetAmountStr = edtBudgetAmount.getText().toString().trim();

            if (TextUtils.isEmpty(budgetName) || TextUtils.isEmpty(budgetAmountStr)) {
                Toast.makeText(EditBudgetActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            double budgetAmount = Double.parseDouble(budgetAmountStr);

            budget.setCategory(budgetName);
            budget.setAmount(budgetAmount);

            AppDatabase.databaseWriteExecutor.execute(() -> {
                budgetDao.update(budget);
                runOnUiThread(() -> {
                    Toast.makeText(EditBudgetActivity.this, "Budget updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
        });

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

        btnBackBudget.setOnClickListener(v -> finish());
    }
}
