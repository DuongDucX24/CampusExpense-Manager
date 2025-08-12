package com.example.se07101campusexpenses.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.se07101campusexpenses.activities.MenuActivity;
import com.example.se07101campusexpenses.R;
import com.example.se07101campusexpenses.database.AppDatabase;
import com.example.se07101campusexpenses.database.UserRepository;
import com.example.se07101campusexpenses.model.User;
import com.example.se07101campusexpenses.security.PasswordUtils;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class LoginActivity extends AppCompatActivity {
    EditText edtUsernameOrEmail, edtPassword; // Changed from edtUsername to edtUsernameOrEmail
    Button btnLogin;
    private UserRepository userRepository;
    Button btnRegister;
    TextView tvForgotPassword; // Added TextView for forgot password link

    // Preference keys
    private static final String PREFS_NAME = "user_prefs";
    private static final String KEY_USER_ID = "user_id";
    private static final int DEFAULT_USER_ID = -1; // Indicates no user logged in

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if user is already logged in
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int userId = sharedPreferences.getInt(KEY_USER_ID, DEFAULT_USER_ID);

        if (userId != DEFAULT_USER_ID) {
            // User is logged in, go to MenuActivity
            Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
            startActivity(intent);
            finish(); // Finish LoginActivity so user can't go back to it
            return; // Skip the rest of onCreate
        }

        // User is not logged in, proceed with login layout
        setContentView(R.layout.activity_login);
        edtPassword = findViewById(R.id.edtPassword);
        edtUsernameOrEmail = findViewById(R.id.edtUsername); // ID in layout is still edtUsername
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword); // Initialize the forgot password TextView

        userRepository = new UserRepository(this);
        setupLoginButton();

        // Change hint to indicate that email can be used too
        edtUsernameOrEmail.setHint("Username or Email");

        // Check if there are any users in the database
        AppDatabase.databaseWriteExecutor.execute(() -> {
            int userCount = userRepository.getUserCount();
            runOnUiThread(() -> {
                if (userCount == 0) {
                    // Disable forgot password if no users exist
                    tvForgotPassword.setEnabled(false);
                    tvForgotPassword.setAlpha(0.5f); // Visual indication that it's disabled
                } else {
                    tvForgotPassword.setEnabled(true);
                    tvForgotPassword.setAlpha(1.0f);
                }
            });
        });

        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Add click listener for forgot password link
        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }

    private void setupLoginButton() {
        btnLogin.setOnClickListener(v -> {
            String usernameOrEmail = edtUsernameOrEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            if (TextUtils.isEmpty(usernameOrEmail)) {
                edtUsernameOrEmail.setError("Enter username or email");
                return;
            }
            if (TextUtils.isEmpty(password)) {
                edtPassword.setError("Enter password");
                return;
            }

            // Determine if input is email or username
            boolean isEmail = Patterns.EMAIL_ADDRESS.matcher(usernameOrEmail).matches();

            AppDatabase.databaseWriteExecutor.execute(() -> {
                User user = null;
                try {
                    if (isEmail) {
                        // Try to get user by email
                        user = userRepository.getUserByEmail(usernameOrEmail);
                    } else {
                        // Try to get user by username
                        user = userRepository.getUserByUsername(usernameOrEmail);
                    }

                    // Verify password if user exists
                    final User finalUser = user;
                    runOnUiThread(() -> {
                        try {
                            if (finalUser != null && PasswordUtils.verifyPassword(password, finalUser.getPassword())) {
                                // Save user ID to SharedPreferences
                                SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putInt(KEY_USER_ID, finalUser.id);
                                editor.apply();

                                Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this, "Invalid username/email or password", Toast.LENGTH_SHORT).show();
                            }
                        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                            Log.e("LoginActivity", "Error during login", e);
                            Toast.makeText(LoginActivity.this, "An error occurred during login.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    Log.e("LoginActivity", "Error during login process", e);
                    runOnUiThread(() -> {
                        Toast.makeText(LoginActivity.this, "An error occurred during login.", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });
    }
}
