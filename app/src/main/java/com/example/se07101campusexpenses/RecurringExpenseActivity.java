package com.example.se07101campusexpenses;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.se07101campusexpenses.adapter.RecurringExpenseAdapter;
import com.example.se07101campusexpenses.database.AppDatabase;
import com.example.se07101campusexpenses.database.RecurringExpenseDao;
import com.example.se07101campusexpenses.model.RecurringExpense;
import java.util.List;

public class RecurringExpenseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recurring_expense);

        RecyclerView rvRecurringExpenses = findViewById(R.id.rvRecurringExpenses);
        Button btnAddRecurringExpense = findViewById(R.id.btnAddRecurringExpense);

        RecurringExpenseDao recurringExpenseDao = AppDatabase.getInstance(this).recurringExpenseDao();

        rvRecurringExpenses.setLayoutManager(new LinearLayoutManager(this));

        int userId = getCurrentUserId();
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<RecurringExpense> recurringExpenses = recurringExpenseDao.getRecurringExpensesByUser(userId);
            runOnUiThread(() -> {
                RecurringExpenseAdapter adapter = new RecurringExpenseAdapter(recurringExpenses);
                rvRecurringExpenses.setAdapter(adapter);
            });
        });

        btnAddRecurringExpense.setOnClickListener(v -> startActivity(new Intent(RecurringExpenseActivity.this, AddRecurringExpenseActivity.class)));
    }

    private int getCurrentUserId() {
        // Implement logic to get the current user's ID
        return 1; // Placeholder
    }
}
