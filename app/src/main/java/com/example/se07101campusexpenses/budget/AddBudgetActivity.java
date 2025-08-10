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
    private EditText edtBudgetName, edtBudgetAmount; // edtBudgetPeriod removed for now
    private BudgetDao budgetDao;
    private int userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_budget);

        edtBudgetName = findViewById(R.id.edtBudgetName);
        edtBudgetAmount = findViewById(R.id.edtBudgetMoney); // Assuming edtBudgetMoney is for amount
        // TODO: Add an EditText with id edtBudgetPeriod to your R.layout.activity_add_budget xml and uncomment the line below
        // edtBudgetPeriod = findViewById(R.id.edtBudgetPeriod);
        Button btnSaveBudget = findViewById(R.id.btnSaveBudget);
        Button btnBackBudget = findViewById(R.id.btnBackBudget);

        budgetDao = AppDatabase.getInstance(this).budgetDao();
        userId = getSharedPreferences("user_prefs", MODE_PRIVATE).getInt("user_id", -1);

        btnSaveBudget.setOnClickListener(v -> {
            String nameBudget = edtBudgetName.getText().toString().trim();
            String amountBudgetStr = edtBudgetAmount.getText().toString().trim();
            // TODO: Uncomment and use if edtBudgetPeriod is added
            // String periodBudget = edtBudgetPeriod.getText().toString().trim();

            // Add validation for periodBudget if it's included
            if (TextUtils.isEmpty(nameBudget) || TextUtils.isEmpty(amountBudgetStr) /*|| TextUtils.isEmpty(periodBudget)*/) {
                Toast.makeText(this, "Please enter all values", Toast.LENGTH_SHORT).show();
                return;
            }

            double amountBudget = Double.parseDouble(amountBudgetStr);

            Budget budget = new Budget();
            budget.name = nameBudget; // Changed from category to name
            budget.amount = amountBudget;
            // TODO: Set budget.period from edtBudgetPeriod if added, otherwise uses placeholder
            // budget.period = periodBudget;
            budget.setPeriod("Monthly"); // Placeholder, get from input
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
