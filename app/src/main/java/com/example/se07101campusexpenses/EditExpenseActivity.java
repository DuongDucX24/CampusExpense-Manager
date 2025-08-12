package com.example.se07101campusexpenses;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.se07101campusexpenses.database.AppDatabase;
import com.example.se07101campusexpenses.database.ExpenseRepository;
import com.example.se07101campusexpenses.model.Expense;

import java.util.Arrays;
import java.util.Calendar;

public class EditExpenseActivity extends AppCompatActivity {

    private EditText edtExpenseDescription, edtExpenseAmount, edtExpenseDate;
    private Spinner spinnerExpenseCategory;

    private ExpenseRepository expenseRepository;
    private Expense expense;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_expense);

        expenseRepository = new ExpenseRepository(this);

        edtExpenseDescription = findViewById(R.id.edtExpenseDescription);
        edtExpenseAmount = findViewById(R.id.edtExpenseAmount);
        edtExpenseDate = findViewById(R.id.edtExpenseDate);
        spinnerExpenseCategory = findViewById(R.id.spinnerExpenseCategory);
        Button btnSaveExpense = findViewById(R.id.btnSaveExpense);
        Button btnDeleteExpense = findViewById(R.id.btnDeleteExpense);
        Button btnBackExpense = findViewById(R.id.btnBackExpense);

        // Category Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.expense_categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerExpenseCategory.setAdapter(adapter);

        // Date Pickers
        edtExpenseDate.setOnClickListener(v -> showDatePickerDialog(edtExpenseDate));

        // Get expense from intent
        expense = (Expense) getIntent().getSerializableExtra("expense");
        if (expense != null) {
            populateFields();
        }

        btnSaveExpense.setOnClickListener(v -> saveExpense());
        btnDeleteExpense.setOnClickListener(v -> deleteExpense());
        btnBackExpense.setOnClickListener(v -> finish());
    }

    private void populateFields() {
        edtExpenseDescription.setText(expense.getDescription());
        edtExpenseAmount.setText(String.valueOf(expense.getAmount()));
        edtExpenseDate.setText(expense.getDate());
        String[] categories = getResources().getStringArray(R.array.expense_categories);
        spinnerExpenseCategory.setSelection(Arrays.asList(categories).indexOf(expense.getCategory()));
    }

    private void showDatePickerDialog(final EditText editText) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String selectedDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1;
                    editText.setText(selectedDate);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void saveExpense() {
        String description = edtExpenseDescription.getText().toString().trim();
        String amountStr = edtExpenseAmount.getText().toString().trim();
        String date = edtExpenseDate.getText().toString().trim();
        String category = spinnerExpenseCategory.getSelectedItem().toString();

        if (TextUtils.isEmpty(description) || TextUtils.isEmpty(amountStr) || TextUtils.isEmpty(date)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);

        expense.setDescription(description);
        expense.setAmount(amount);
        expense.setDate(date);
        expense.setCategory(category);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            expenseRepository.updateExpense(expense);
            runOnUiThread(() -> {
                Toast.makeText(this, "Expense updated successfully", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    private void deleteExpense() {
        if (expense != null) {
            AppDatabase.databaseWriteExecutor.execute(() -> {
                expenseRepository.deleteExpense(expense.getId());
                runOnUiThread(() -> {
                    Toast.makeText(this, "Expense deleted successfully", Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
        }
    }
}
