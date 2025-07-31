package com.example.se07101campusexpenses.budget;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.se07101campusexpenses.MenuActivity;
import com.example.se07101campusexpenses.R;
import com.example.se07101campusexpenses.database.BudgetRepository;

public class AddBudgetActivity extends AppCompatActivity {
    Button btnSave, btnBack;
    EditText edtBudgetName, edtBudgetMoney, edtDescription;
    BudgetRepository repository;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_budget);
        btnSave = findViewById(R.id.btnSaveBudget);
        btnBack = findViewById(R.id.btnBackBudget);
        edtBudgetName  = findViewById(R.id.edtBudgetName);
        edtBudgetMoney = findViewById(R.id.edtBudgetMoney);
        edtDescription = findViewById(R.id.edtDescription);
        repository = new BudgetRepository(AddBudgetActivity.this);

        btnSave.setOnClickListener(v -> {
            String nameBudget = edtBudgetName.getText().toString().trim();
            int moneyBudget = Integer.parseInt(edtBudgetMoney.getText().toString().trim());
            String description = edtDescription.getText().toString().trim();
            if (TextUtils.isEmpty(nameBudget)){
                edtBudgetName.setError("Enter name's budget, please");
                return;
            }
            if (moneyBudget <= 0){
                edtBudgetMoney.setError("Enter money's budget, please");
                return;
            }
            long insertBudget = repository.addNewBudget(nameBudget, moneyBudget, description);
            if (insertBudget == -1){
                // loi
                Toast.makeText(AddBudgetActivity.this, "Can not create budget, please try again", Toast.LENGTH_SHORT).show();
            } else {
                // thanh cong
                Toast.makeText(AddBudgetActivity.this, "Create budget successfully", Toast.LENGTH_SHORT).show();
                // Quay ve lai Menu activity
                Intent intent = new Intent(AddBudgetActivity.this, MenuActivity.class);
                startActivity(intent);
            }
        });
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(AddBudgetActivity.this, MenuActivity.class);
            startActivity(intent);
        });
    }
}
