package com.example.se07101campusexpenses.budget;

import android.os.Bundle;
import android.text.TextUtils;
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
    private EditText edtBudgetName, edtBudgetAmount, edtBudgetPeriod;
    private Button btnSaveBudget, btnDeleteBudget, btnBackBudget;
    private BudgetDao budgetDao;
    private Budget budget;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_budget);

        budgetDao = AppDatabase.getInstance(this).budgetDao();

        edtBudgetName = findViewById(R.id.edtBudgetName);
        edtBudgetAmount = findViewById(R.id.edtBudgetMoney);
        edtBudgetPeriod = findViewById(R.id.edtDescription);
        btnSaveBudget = findViewById(R.id.btnSaveBudget);
        btnDeleteBudget = findViewById(R.id.btnDeleteBudget);
        btnBackBudget = findViewById(R.id.btnBackBudget);

        budget = (Budget) getIntent().getSerializableExtra("budget");
        if (budget != null) {
            populateFields();
        }

        btnSaveBudget.setOnClickListener(v -> saveBudget());
        btnDeleteBudget.setOnClickListener(v -> deleteBudget());
        btnBackBudget.setOnClickListener(v -> finish());
    }

    private void populateFields() {
        edtBudgetName.setText(budget.name);
        edtBudgetAmount.setText(String.valueOf(budget.amount));
        edtBudgetPeriod.setText(budget.period);
    }

    private void saveBudget() {
        String name = edtBudgetName.getText().toString().trim();
        String amountStr = edtBudgetAmount.getText().toString().trim();
        String period = edtBudgetPeriod.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(amountStr) || TextUtils.isEmpty(period)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);

        budget.name = name;
        budget.amount = amount;
        budget.period = period;

        AppDatabase.databaseWriteExecutor.execute(() -> {
            budgetDao.update(budget);
            runOnUiThread(() -> {
                Toast.makeText(this, "Budget updated", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    private void deleteBudget() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            budgetDao.delete(budget);
            runOnUiThread(() -> {
                Toast.makeText(this, "Budget deleted", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}
