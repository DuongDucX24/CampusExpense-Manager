package com.example.se07101campusexpenses.budget;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.se07101campusexpenses.R;

public class AddExpenseActivity extends AppCompatActivity {
    private EditText edtExpenseName, edtExpenseAmount, edtExpenseDescription;
    private Button btnSaveExpense, btnBackExpense;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        edtExpenseName = findViewById(R.id.edtExpenseName);
        edtExpenseAmount = findViewById(R.id.edtExpenseAmount);
        edtExpenseDescription = findViewById(R.id.edtExpenseDescription);
        btnSaveExpense = findViewById(R.id.btnSaveExpense);
        btnBackExpense = findViewById(R.id.btnBackExpense);

        btnSaveExpense.setOnClickListener(v -> addExpense());
        btnBackExpense.setOnClickListener(v -> finish());
    }

    private void addExpense() {
        String name = edtExpenseName.getText().toString().trim();
        String amountStr = edtExpenseAmount.getText().toString().trim();
        String description = edtExpenseDescription.getText().toString().trim();

        Toast Toast = null;
        if (name.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);

        // Save expense to database (implement your DB logic here)
        // Update the related budget's remaining amount
        // Example: budget.setRemaining(budget.getRemaining() - amount);

        Toast.makeText(this, "Expense added", Toast.LENGTH_SHORT).show();
        finish();
    }
}
