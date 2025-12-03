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
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.example.se07101campusexpenses.R;
import com.example.se07101campusexpenses.database.AppDatabase;
import com.example.se07101campusexpenses.database.UserRepository;
import com.example.se07101campusexpenses.model.User;
import com.example.se07101campusexpenses.security.PasswordUtils;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.concurrent.Executor;

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

    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

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

        // Keep "Forgot Password" always enabled and clickable
        tvForgotPassword.setEnabled(true);
        tvForgotPassword.setAlpha(1.0f);

        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Add click listener for forgot password link
        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        Executor executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(LoginActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              CharSequence errString) {
                Toast.makeText(getApplicationContext(),
                        "Authentication error: " + errString, Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onAuthenticationSucceeded(
                    BiometricPrompt.AuthenticationResult result) {
                // Assuming you have a way to get the user's details after successful biometric auth
                // For now, let's just log in the last logged-in user if available
                SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                int userId = sharedPreferences.getInt(KEY_USER_ID, DEFAULT_USER_ID);
                if (userId != DEFAULT_USER_ID) {
                    Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "No user found for biometric login. Please log in manually first.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAuthenticationFailed() {
                Toast.makeText(getApplicationContext(), "Authentication failed",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login for your app")
                .setSubtitle("Log in using your biometric credential")
                .setNegativeButtonText("Use account password")
                .build();

        // Add a button for biometric login and set its click listener
        Button btnBiometricLogin = findViewById(R.id.btnBiometricLogin);
        btnBiometricLogin.setOnClickListener(view -> biometricPrompt.authenticate(promptInfo));
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

            boolean isEmail = Patterns.EMAIL_ADDRESS.matcher(usernameOrEmail).matches();

            AppDatabase.databaseWriteExecutor.execute(() -> {
                try {
                    final User finalUser = isEmail
                            ? userRepository.getUserByEmail(usernameOrEmail)
                            : userRepository.getUserByUsername(usernameOrEmail);

                    runOnUiThread(() -> {
                        try {
                            if (finalUser != null && PasswordUtils.verifyPassword(password, finalUser.getPassword())) {
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
                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, "An error occurred during login.", Toast.LENGTH_SHORT).show());
                }
            });
        });
    }
}
