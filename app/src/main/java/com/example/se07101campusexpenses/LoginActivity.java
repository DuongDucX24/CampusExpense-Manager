package com.example.se07101campusexpenses;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
// import android.widget.TextView; // TextView import no longer needed for btnRegister
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.se07101campusexpenses.database.AppDatabase;
import com.example.se07101campusexpenses.database.UserRepository;
import com.example.se07101campusexpenses.model.User;
import com.example.se07101campusexpenses.security.PasswordUtils;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class LoginActivity extends AppCompatActivity {
    EditText edtUsername, edtPassword;
    Button btnLogin;
    private UserRepository userRepository;
    Button btnRegister; // Changed from TextView to Button

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
        edtUsername = findViewById(R.id.edtUsername);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister); // This should now resolve
        userRepository = new UserRepository(this);
        setupLoginButton();

        btnRegister.setOnClickListener(v -> { // Listener now on btnRegister
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void setupLoginButton() {
        btnLogin.setOnClickListener(v -> {
            String username = edtUsername.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            if (TextUtils.isEmpty(username)) {
                edtUsername.setError("Enter username");
                return;
            }
            if (TextUtils.isEmpty(password)) {
                edtPassword.setError("Enter password");
                return;
            }

            AppDatabase.databaseWriteExecutor.execute(() -> {
                User user = userRepository.getUserByUsername(username);
                runOnUiThread(() -> {
                    try {
                        if (user != null && PasswordUtils.verifyPassword(password, user.getPassword())) {
                            // Save user ID to SharedPreferences
                            SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putInt(KEY_USER_ID, user.id);
                            editor.apply();

                            Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                        }
                    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                        Log.e("LoginActivity", "Error during login", e);
                        Toast.makeText(LoginActivity.this, "An error occurred during login.", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });
    }
}
