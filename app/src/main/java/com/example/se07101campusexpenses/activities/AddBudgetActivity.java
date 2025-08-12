package com.example.se07101campusexpenses.activities;

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
    private EditText edtBudgetName, edtBudgetAmount, edtBudgetDescription, edtBudgetPeriod; // Added edtBudgetDescription and edtBudgetPeriod
    private BudgetDao budgetDao;
    private int userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_budget);

        edtBudgetName = findViewById(R.id.edtBudgetName);
        edtBudgetAmount = findViewById(R.id.edtBudgetMoney); // Assuming edtBudgetMoney is for amount
        edtBudgetDescription = findViewById(R.id.edtBudgetDescription); // Initialize edtBudgetDescription
        edtBudgetPeriod = findViewById(R.id.edtBudgetPeriod); // Initialize edtBudgetPeriod
        Button btnSaveBudget = findViewById(R.id.btnSaveBudget);
        Button btnBackBudget = findViewById(R.id.btnBackBudget);

        budgetDao = AppDatabase.getInstance(this).budgetDao();
        userId = getSharedPreferences("user_prefs", MODE_PRIVATE).getInt("user_id", -1);

        btnSaveBudget.setOnClickListener(v -> {
            String nameBudget = edtBudgetName.getText().toString().trim();
            String amountBudgetStr = edtBudgetAmount.getText().toString().trim();
            String descriptionBudget = edtBudgetDescription.getText().toString().trim();
            String periodBudget = edtBudgetPeriod.getText().toString().trim();

            if (TextUtils.isEmpty(nameBudget) || TextUtils.isEmpty(amountBudgetStr) || TextUtils.isEmpty(periodBudget) || TextUtils.isEmpty(descriptionBudget)) {
                Toast.makeText(this, "Please enter all values", Toast.LENGTH_SHORT).show();
                return;
            }
            if (userId == -1) {
                Toast.makeText(this, "User not logged in. Cannot add budget.", Toast.LENGTH_SHORT).show();
                return;
            }

            double amountBudget;
            try {
                amountBudget = Double.parseDouble(amountBudgetStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid amount format", Toast.LENGTH_SHORT).show();
                return;
            }

            Budget budget = new Budget(nameBudget, amountBudget, periodBudget, descriptionBudget, userId);

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
