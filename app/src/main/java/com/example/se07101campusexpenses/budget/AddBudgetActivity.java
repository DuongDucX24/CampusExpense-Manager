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

public class AddBudgetActivity extends AppCompatActivity {
    private EditText edtBudgetName, edtBudgetAmount, edtBudgetPeriod;
    private Button btnSaveBudget, btnBackBudget;
    private BudgetDao budgetDao;
    private int userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_budget);

        budgetDao = AppDatabase.getInstance(this).budgetDao();
        userId = getSharedPreferences("user_prefs", MODE_PRIVATE).getInt("user_id", -1);

        edtBudgetName  = findViewById(R.id.edtBudgetName);
        edtBudgetAmount = findViewById(R.id.edtBudgetMoney);
        edtBudgetPeriod = findViewById(R.id.edtDescription); // Assuming this is for period
        btnSaveBudget = findViewById(R.id.btnSaveBudget);
        btnBackBudget = findViewById(R.id.btnBackBudget);

        btnSaveBudget.setOnClickListener(view -> {
            String nameBudget = edtBudgetName.getText().toString().trim();
            String amountBudgetStr = edtBudgetAmount.getText().toString().trim();
            String period = edtBudgetPeriod.getText().toString().trim();

            if (TextUtils.isEmpty(nameBudget) || TextUtils.isEmpty(amountBudgetStr) || TextUtils.isEmpty(period)) {
                Toast.makeText(this, "Please enter all values", Toast.LENGTH_SHORT).show();
                return;
            }

            double amountBudget = Double.parseDouble(amountBudgetStr);

            Budget budget = new Budget();
            budget.name = nameBudget;
            budget.amount = amountBudget;
            budget.period = period;
            budget.userId = userId;

            AppDatabase.databaseWriteExecutor.execute(() -> {
                budgetDao.insert(budget);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Add budget successfully", Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
        });

        btnBackBudget.setOnClickListener(view -> finish());
    }
}
