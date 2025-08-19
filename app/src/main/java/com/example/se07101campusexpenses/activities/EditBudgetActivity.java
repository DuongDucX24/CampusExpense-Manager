package com.example.se07101campusexpenses.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.se07101campusexpenses.R;
import com.example.se07101campusexpenses.database.AppDatabase;
import com.example.se07101campusexpenses.database.BudgetDao;
import com.example.se07101campusexpenses.database.ExpenseDao;
import com.example.se07101campusexpenses.model.Budget;
import com.example.se07101campusexpenses.util.FormatUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditBudgetActivity extends AppCompatActivity {
    private BudgetDao budgetDao;
    private Budget budget;
    private EditText edtBudgetName, edtBudgetAmount, edtBudgetDescription;
    private Spinner spBudgetPeriod;

    private final SimpleDateFormat isoFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Reuse add budget layout; we will change the title programmatically
        setContentView(R.layout.activity_add_budget);

        budgetDao = AppDatabase.getInstance(this).budgetDao();

        TextView tvTitle = findViewById(R.id.tvBudgetTitle);
        if (tvTitle != null) tvTitle.setText(R.string.title_edit_budget);

        edtBudgetName = findViewById(R.id.edtBudgetName);
        edtBudgetAmount = findViewById(R.id.edtBudgetMoney);
        edtBudgetDescription = findViewById(R.id.edtBudgetDescription);
        spBudgetPeriod = findViewById(R.id.spBudgetPeriod);

        Button btnSaveBudget = findViewById(R.id.btnSaveBudget);
        Button btnBackBudget = findViewById(R.id.btnBackBudget);

        // Apply dot-grouping formatter to amount field
        FormatUtils.applyDotGroupingFormatter(edtBudgetAmount);

        budget = (Budget) getIntent().getSerializableExtra("budget");
        if (budget != null) {
            edtBudgetName.setText(budget.getName());
            // Format initial amount
            edtBudgetAmount.setText(FormatUtils.formatDoubleWithDots(budget.getAmount()));
            edtBudgetDescription.setText(budget.getDescription());
            // Set spinner selection based on budget period
            if (spBudgetPeriod != null && spBudgetPeriod.getAdapter() != null) {
                int count = spBudgetPeriod.getAdapter().getCount();
                for (int i = 0; i < count; i++) {
                    Object item = spBudgetPeriod.getAdapter().getItem(i);
                    if (item != null && item.toString().equalsIgnoreCase(budget.getPeriod())) {
                        spBudgetPeriod.setSelection(i);
                        break;
                    }
                }
            }
        } else {
            Toast.makeText(this, "Error loading budget data.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnSaveBudget.setOnClickListener(v -> {
            String budgetName = edtBudgetName.getText().toString().trim();
            // Strip grouping before parse
            String budgetAmountStr = edtBudgetAmount.getText().toString().trim();
            String budgetAmountPlain = FormatUtils.stripGrouping(budgetAmountStr);
            String budgetDescription = edtBudgetDescription.getText().toString().trim();
            String budgetPeriod = (spBudgetPeriod != null && spBudgetPeriod.getSelectedItem() != null)
                    ? spBudgetPeriod.getSelectedItem().toString() : "Monthly";

            if (TextUtils.isEmpty(budgetName) || TextUtils.isEmpty(budgetAmountPlain) ||
                    TextUtils.isEmpty(budgetPeriod) || TextUtils.isEmpty(budgetDescription)) {
                Toast.makeText(EditBudgetActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            double newAmount;
            try { newAmount = Double.parseDouble(budgetAmountPlain); }
            catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid amount format", Toast.LENGTH_SHORT).show();
                return;
            }

            // Enforce: cannot set budget below sum of linked expenses for the current period
            String[] range = currentRangeForPeriod(budgetPeriod);
            AppDatabase.databaseWriteExecutor.execute(() -> {
                ExpenseDao expenseDao = AppDatabase.getInstance(getApplicationContext()).expenseDao();
                Double spent = expenseDao.getTotalByBudgetBetweenDates(budget.getId(), range[0], range[1]);
                double spentVal = spent != null ? spent : 0d;
                if (newAmount < spentVal) {
                    runOnUiThread(() -> Toast.makeText(EditBudgetActivity.this,
                            "Amount can't be less than already spent (" + spentVal + ") in this period",
                            Toast.LENGTH_LONG).show());
                    return;
                }
                // Proceed to save
                budget.setName(budgetName);
                budget.setAmount(newAmount);
                budget.setDescription(budgetDescription);
                budget.setPeriod(budgetPeriod);

                budgetDao.update(budget);
                runOnUiThread(() -> {
                    Toast.makeText(EditBudgetActivity.this, "Budget updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
        });

        btnBackBudget.setOnClickListener(v -> finish());
    }

    private String[] currentRangeForPeriod(String period) {
        Calendar cal = Calendar.getInstance();
        java.text.SimpleDateFormat sdf = isoFmt;
        if (period == null) period = "Monthly";
        switch (period.toLowerCase(Locale.US)) {
            case "daily": {
                String d = sdf.format(cal.getTime());
                return new String[]{d, d};
            }
            case "weekly": {
                int dow = cal.get(Calendar.DAY_OF_WEEK); // 1=Sunday
                int diffToMonday = (dow == Calendar.SUNDAY) ? -6 : (Calendar.MONDAY - dow);
                cal.add(Calendar.DAY_OF_MONTH, diffToMonday);
                String start = sdf.format(cal.getTime());
                cal.add(Calendar.DAY_OF_MONTH, 6);
                String end = sdf.format(cal.getTime());
                return new String[]{start, end};
            }
            case "monthly":
            default: {
                cal.set(Calendar.DAY_OF_MONTH, 1);
                String start = sdf.format(cal.getTime());
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                String end = sdf.format(cal.getTime());
                return new String[]{start, end};
            }
        }
    }
}
