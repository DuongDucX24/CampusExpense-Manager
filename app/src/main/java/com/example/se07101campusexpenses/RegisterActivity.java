package com.example.se07101campusexpenses;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast; // Added for later use
import android.text.TextUtils; // Added for validation

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.se07101campusexpenses.database.AppDatabase; // Added for DB access
import com.example.se07101campusexpenses.database.UserRepository;
import com.example.se07101campusexpenses.model.User; // Added for User model

public class RegisterActivity extends AppCompatActivity {

    EditText edtRegisterUsername, edtRegisterPassword, edtConfirmPassword, edtEmail;
    Button btnPerformRegister, btnBackToLogin;
    private UserRepository userRepository;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userRepository = new UserRepository(this); // Initialize UserRepository

        edtRegisterUsername = findViewById(R.id.edtRegisterUsername);
        edtRegisterPassword = findViewById(R.id.edtRegisterPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        edtEmail = findViewById(R.id.edtEmail);
        btnPerformRegister = findViewById(R.id.btnPerformRegister);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);

        btnPerformRegister.setOnClickListener(v -> performRegistration());

        btnBackToLogin.setOnClickListener(v -> finish()); // Go back to LoginActivity
    }

    private void performRegistration() {
        String username = edtRegisterUsername.getText().toString().trim();
        String password = edtRegisterPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword) || TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Implement secure password hashing here!
        // For now, storing plaintext (NOT SECURE - FOR DEMO ONLY)
        User newUser = new User(username, password, email); // HASH THE PASSWORD INSTEAD

        AppDatabase.databaseWriteExecutor.execute(() -> {
            // Check if user already exists
            User existingUser = userRepository.getUserByUsername(username);
            if (existingUser == null) {
                userRepository.saveUserAccount(newUser);
                runOnUiThread(() -> {
                    Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                    finish(); // Go back to LoginActivity
                });
            } else {
                runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "Username already exists", Toast.LENGTH_SHORT).show());
            }
        });
    }
}
