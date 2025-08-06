package com.example.se07101campusexpenses;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.se07101campusexpenses.database.Expense;
import com.example.se07101campusexpenses.database.ExpenseRepository;

import java.util.Calendar;

/**
 * Activity for adding a new expense.
 *
 * <p>This activity provides a user interface for inputting expense details, including
 * description, amount, date, category, and whether it's a recurring expense.
 * It uses {@link ExpenseRepository} to interact with the database for saving expenses.
 * </p>
 *
 * <p>Key features include:
 * <ul>
 *     <li>Input fields for expense description, amount, and date.</li>
 *     <li>A spinner for selecting the expense category.</li>
 *     <li>A checkbox to indicate if the expense is recurring.</li>
 *     <li>Conditional visibility for recurring start and end date pickers.</li>
 *     <li>Date pickers for selecting dates.</li>
 *     <li>Validation to ensure all required fields are filled.</li>
 *     <li>Saving the expense to the database.</li>
 * </ul>
 * </p>
 */
public class AddExpenseActivity extends AppCompatActivity {

    private EditText edtExpenseDescription, edtExpenseAmount, edtExpenseDate, edtRecurringStartDate, edtRecurringEndDate;
    private Spinner spinnerExpenseCategory;
    private CheckBox chkRecurring;
    private LinearLayout layoutRecurringDates;

    private ExpenseRepository expenseRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        expenseRepository = new ExpenseRepository(this);

        edtExpenseDescription = findViewById(R.id.edtExpenseDescription);
        edtExpenseAmount = findViewById(R.id.edtExpenseAmount);
        edtExpenseDate = findViewById(R.id.edtExpenseDate);
        spinnerExpenseCategory = findViewById(R.id.spinnerExpenseCategory);
        chkRecurring = findViewById(R.id.chkRecurring);
        layoutRecurringDates = findViewById(R.id.layoutRecurringDates);
        edtRecurringStartDate = findViewById(R.id.edtRecurringStartDate);
        edtRecurringEndDate = findViewById(R.id.edtRecurringEndDate);
        Button btnSaveExpense = findViewById(R.id.btnSaveExpense);
        Button btnBackExpense = findViewById(R.id.btnBackExpense);

        // Category Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.expense_categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerExpenseCategory.setAdapter(adapter);

        // Date Pickers
        edtExpenseDate.setOnClickListener(v -> showDatePickerDialog(edtExpenseDate));
        edtRecurringStartDate.setOnClickListener(v -> showDatePickerDialog(edtRecurringStartDate));
        edtRecurringEndDate.setOnClickListener(v -> showDatePickerDialog(edtRecurringEndDate));

        // Recurring Expense Checkbox
        chkRecurring.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                layoutRecurringDates.setVisibility(View.VISIBLE);
            } else {
                layoutRecurringDates.setVisibility(View.GONE);
            }
        });

        btnSaveExpense.setOnClickListener(v -> saveExpense());
        btnBackExpense.setOnClickListener(v -> finish());
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
        boolean isRecurring = chkRecurring.isChecked();
        String recurringStartDate = edtRecurringStartDate.getText().toString().trim();
        String recurringEndDate = edtRecurringEndDate.getText().toString().trim();

        if (TextUtils.isEmpty(description) || TextUtils.isEmpty(amountStr) || TextUtils.isEmpty(date)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);

        Expense expense = new Expense();
        expense.setDescription(description);
        expense.setAmount(amount);
        expense.setDate(date);
        expense.setCategory(category);
        expense.setRecurring(isRecurring);
        if (isRecurring) {
            expense.setRecurringStartDate(recurringStartDate);
            expense.setRecurringEndDate(recurringEndDate);
        }

        long id = expenseRepository.addExpense(expense);
        if (id > 0) {
            Toast.makeText(this, "Expense saved successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to save expense", Toast.LENGTH_SHORT).show();
        }
    }
}
