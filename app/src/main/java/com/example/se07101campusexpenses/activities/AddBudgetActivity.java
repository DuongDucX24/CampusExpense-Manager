package com.example.se07101campusexpenses.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.se07101campusexpenses.R;
import com.example.se07101campusexpenses.database.AppDatabase;
import com.example.se07101campusexpenses.database.BudgetDao;
import com.example.se07101campusexpenses.model.Budget;
import com.example.se07101campusexpenses.util.FormatUtils;
import com.example.se07101campusexpenses.util.SessionManager;

public class AddBudgetActivity extends AppCompatActivity {
    private EditText edtBudgetName, edtBudgetAmount, edtBudgetDescription;
    private Spinner spBudgetPeriod;
    private BudgetDao budgetDao;
    private int userId;
    private SessionManager sessionManager;

    @Override
    @SuppressLint("MissingInflatedId")
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);
        setContentView(R.layout.activity_add_budget);

        edtBudgetName = findViewById(R.id.edtBudgetName);
        edtBudgetAmount = findViewById(R.id.edtBudgetMoney);
        edtBudgetDescription = findViewById(R.id.edtBudgetDescription);
        // Directly reference spinner id to avoid reflection
        spBudgetPeriod = findViewById(R.id.spBudgetPeriod);
        Button btnSaveBudget = findViewById(R.id.btnSaveBudget);
        Button btnBackBudget = findViewById(R.id.btnBackBudget);

        // Apply dot grouping formatter to amount field
        FormatUtils.applyDotGroupingFormatter(edtBudgetAmount);

        budgetDao = AppDatabase.getInstance(this).budgetDao();
        userId = getSharedPreferences("user_prefs", MODE_PRIVATE).getInt("user_id", -1);

        btnSaveBudget.setOnClickListener(v -> {
            String nameBudget = edtBudgetName.getText().toString().trim();
            String amountBudgetStr = edtBudgetAmount.getText().toString().trim();
            String descriptionBudget = edtBudgetDescription.getText().toString().trim();
            String periodBudget = (spBudgetPeriod != null && spBudgetPeriod.getSelectedItem() != null)
                    ? spBudgetPeriod.getSelectedItem().toString()
                    : "Monthly"; // default fallback

            if (TextUtils.isEmpty(nameBudget) || TextUtils.isEmpty(amountBudgetStr) || TextUtils.isEmpty(periodBudget) || TextUtils.isEmpty(descriptionBudget)) {
                Toast.makeText(this, "Please enter all values", Toast.LENGTH_SHORT).show();
                return;
            }
            if (userId == -1) {
                Toast.makeText(this, "User not logged in. Cannot add budget.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Strip grouping before parse
            String plain = FormatUtils.stripGrouping(amountBudgetStr);
            double amountBudget;
            try {
                amountBudget = Double.parseDouble(plain);
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

    @Override
    protected void onResume() {
        super.onResume();
        if (sessionManager.checkAndLockIfTimeout()) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }
}
