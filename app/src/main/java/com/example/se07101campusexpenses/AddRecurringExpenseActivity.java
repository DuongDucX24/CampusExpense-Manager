package com.example.se07101campusexpenses;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.se07101campusexpenses.database.AppDatabase;
import com.example.se07101campusexpenses.database.RecurringExpenseDao;
import com.example.se07101campusexpenses.model.RecurringExpense;

/**
 * Activity for adding a new recurring expense.
 *
 * <p>This activity provides a user interface for inputting the details of a recurring expense,
 * such as description, amount, category, frequency, start date, and end date.
 * Upon saving, the new recurring expense is persisted to the local database.
 */
public class AddRecurringExpenseActivity extends AppCompatActivity {

    private EditText etDescription, etAmount, etCategory, etStartDate, etEndDate;
    private Spinner spFrequency;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recurring_expense);

        etDescription = findViewById(R.id.etDescription);
        etAmount = findViewById(R.id.etAmount);
        etCategory = findViewById(R.id.etCategory);
        spFrequency = findViewById(R.id.spFrequency);
        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        Button btnSave = findViewById(R.id.btnSave);

        RecurringExpenseDao recurringExpenseDao = AppDatabase.getInstance(this).recurringExpenseDao();

        btnSave.setOnClickListener(v -> saveRecurringExpense(recurringExpenseDao));
    }

    private void saveRecurringExpense(RecurringExpenseDao recurringExpenseDao) {
        String description = etDescription.getText().toString();
        String amountStr = etAmount.getText().toString();
        String category = etCategory.getText().toString();
        String frequency = spFrequency.getSelectedItem().toString();
        String startDate = etStartDate.getText().toString();
        String endDate = etEndDate.getText().toString();

        if (description.isEmpty() || amountStr.isEmpty() || category.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        int userId = getCurrentUserId();

        RecurringExpense recurringExpense = new RecurringExpense();
        recurringExpense.description = description;
        recurringExpense.amount = amount;
        recurringExpense.category = category;
        recurringExpense.frequency = frequency;
        recurringExpense.startDate = startDate;
        recurringExpense.endDate = endDate;
        recurringExpense.userId = userId;

        AppDatabase.databaseWriteExecutor.execute(() -> {
            recurringExpenseDao.insert(recurringExpense);
            runOnUiThread(() -> {
                Toast.makeText(this, "Recurring expense saved", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    private int getCurrentUserId() {
        // Implement logic to get the current user's ID
        return getSharedPreferences("user_prefs", MODE_PRIVATE).getInt("user_id", -1);
    }
}
