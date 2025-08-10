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
    private EditText edtBudgetName, edtBudgetAmount; // edtBudgetPeriod removed for now

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: Ensure this layout (activity_add_budget or a specific activity_edit_budget) 
        // has edtBudgetName, edtBudgetMoney, btnSaveBudget, btnDeleteBudget, btnBackBudget.
        // If using a separate edit layout, change R.layout.activity_add_budget below.
        setContentView(R.layout.activity_add_budget); 

        budgetDao = AppDatabase.getInstance(this).budgetDao();

        edtBudgetName = findViewById(R.id.edtBudgetName);
        edtBudgetAmount = findViewById(R.id.edtBudgetMoney); // Assuming edtBudgetMoney is for amount
        // TODO: Add an EditText with id edtBudgetPeriod to your layout and uncomment the line below
        // edtBudgetPeriod = findViewById(R.id.edtBudgetPeriod);

        Button btnSaveBudget = findViewById(R.id.btnSaveBudget);
        // TODO: Add a Button with id btnDeleteBudget to your layout and uncomment the line below
        // Button btnDeleteBudget = findViewById(R.id.btnDeleteBudget);
        Button btnBackBudget = findViewById(R.id.btnBackBudget);

        budget = (Budget) getIntent().getSerializableExtra("budget");
        if (budget != null) {
            edtBudgetName.setText(budget.getName()); // Changed from getCategory to getName
            edtBudgetAmount.setText(String.valueOf(budget.getAmount()));
            // TODO: Uncomment and use if edtBudgetPeriod is added
            // if (edtBudgetPeriod != null) edtBudgetPeriod.setText(budget.getPeriod()); 
        }

        btnSaveBudget.setOnClickListener(v -> {
            String budgetName = edtBudgetName.getText().toString().trim();
            String budgetAmountStr = edtBudgetAmount.getText().toString().trim();
            // TODO: Uncomment and use if edtBudgetPeriod is added
            // String budgetPeriod = edtBudgetPeriod != null ? edtBudgetPeriod.getText().toString().trim() : budget.getPeriod();

            if (TextUtils.isEmpty(budgetName) || TextUtils.isEmpty(budgetAmountStr)) {
                Toast.makeText(EditBudgetActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            double budgetAmount = Double.parseDouble(budgetAmountStr);

            budget.setName(budgetName); // Changed from setCategory to setName
            budget.setAmount(budgetAmount);
            // TODO: Set budget.period from edtBudgetPeriod if added
            // budget.setPeriod(budgetPeriod);

            AppDatabase.databaseWriteExecutor.execute(() -> {
                budgetDao.update(budget);
                runOnUiThread(() -> {
                    Toast.makeText(EditBudgetActivity.this, "Budget updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
        });

        // TODO: Uncomment this block if btnDeleteBudget is added to the layout
        /*
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
        */

        btnBackBudget.setOnClickListener(v -> finish());
    }
}
