package com.example.se07101campusexpenses;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.se07101campusexpenses.database.AppDatabase;
import com.example.se07101campusexpenses.database.UserRepository;
import com.example.se07101campusexpenses.model.User;

public class RegisterActivity extends AppCompatActivity {
    EditText edtUsername, edtPassword;
    Button btnSignUp;
    private UserRepository userRepository;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userRepository = new UserRepository(this);
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        registerUserAccount();
    }

    private void registerUserAccount(){
        btnSignUp.setOnClickListener(v -> {
            String username = edtUsername.getText().toString().trim();
            if (TextUtils.isEmpty(username)){
                edtUsername.setError("Enter username, please !");
                return;
            }
            String password = edtPassword.getText().toString().trim();
            if (TextUtils.isEmpty(password)){
                edtPassword.setError("Enter password, please !");
                return;
            }

            AppDatabase.databaseWriteExecutor.execute(() -> {
                User existingUser = userRepository.getUserByUsername(username);
                if (existingUser != null) {
                    runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "Username already exists", Toast.LENGTH_SHORT).show());
                } else {
                    User newUser = new User();
                    newUser.username = username;
                    newUser.password = password; // In a real app, hash the password
                    userRepository.saveUserAccount(newUser);
                    runOnUiThread(() -> {
                        Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    });
                }
            });
        });
    }
}
