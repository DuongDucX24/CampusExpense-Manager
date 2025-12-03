package com.example.se07101campusexpenses.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.se07101campusexpenses.R;
import com.example.se07101campusexpenses.database.AppDatabase;
import com.example.se07101campusexpenses.database.UserRepository;
import com.example.se07101campusexpenses.model.User;
import com.example.se07101campusexpenses.security.PasswordUtils;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    EditText edtRegisterUsername, edtRegisterPassword, edtConfirmPassword, edtEmail, edtSecurityAnswer;
    Spinner spinnerSecurityQuestion;
    Button btnPerformRegister, btnBackToLogin;
    private UserRepository userRepository;

    // Define password validation pattern - at least one special character
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[^a-zA-Z0-9]");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userRepository = new UserRepository(this); // Initialize UserRepository

        // Initialize views
        edtRegisterUsername = findViewById(R.id.edtRegisterUsername);
        edtRegisterPassword = findViewById(R.id.edtRegisterPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        edtEmail = findViewById(R.id.edtEmail);
        edtSecurityAnswer = findViewById(R.id.edtSecurityAnswer);
        spinnerSecurityQuestion = findViewById(R.id.spinnerSecurityQuestion);
        btnPerformRegister = findViewById(R.id.btnPerformRegister);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);

        // Setup security questions spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.security_questions, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSecurityQuestion.setAdapter(adapter);

        btnPerformRegister.setOnClickListener(v -> performRegistration());
        btnBackToLogin.setOnClickListener(v -> finish()); // Go back to LoginActivity
    }

    private void performRegistration() {
        String username = edtRegisterUsername.getText().toString().trim();
        String password = edtRegisterPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String securityQuestion = spinnerSecurityQuestion.getSelectedItem().toString();
        String securityAnswer = edtSecurityAnswer.getText().toString().trim();

        // Check if all fields are filled
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password) ||
            TextUtils.isEmpty(confirmPassword) || TextUtils.isEmpty(email) ||
            TextUtils.isEmpty(securityAnswer)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate username (at least 3 characters)
        if (username.length() < 3) {
            Toast.makeText(this, "Username must be at least 3 characters long", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!USERNAME_PATTERN.matcher(username).matches()) {
            Toast.makeText(this, "Username can only contain letters and numbers", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate email format
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate password (at least 6 characters and includes a special character)
        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!SPECIAL_CHAR_PATTERN.matcher(password).find()) {
            Toast.makeText(this, "Password must contain at least one special character", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if passwords match
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate security answer (non-empty)
        if (securityAnswer.isEmpty()) {
            Toast.makeText(this, "Security answer is required", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String hashedPassword = PasswordUtils.hashPassword(password);
            User newUser = new User(username, hashedPassword, email, securityQuestion, securityAnswer);

            AppDatabase.databaseWriteExecutor.execute(() -> {
                // Check if username already exists
                User existingUserByUsername = userRepository.getUserByUsername(username);
                // Check if email already exists
                User existingUserByEmail = userRepository.getUserByEmail(email);

                if (existingUserByUsername != null) {
                    runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "Username already exists", Toast.LENGTH_SHORT).show());
                } else if (existingUserByEmail != null) {
                    runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "Email address already in use", Toast.LENGTH_SHORT).show());
                } else {
                    userRepository.saveUserAccount(newUser);
                    runOnUiThread(() -> {
                        Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                        finish(); // Go back to LoginActivity
                    });
                }
            });
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            Log.e("RegisterActivity", "Error hashing password", e);
            Toast.makeText(this, "Error creating account", Toast.LENGTH_SHORT).show();
        }
    }
}
