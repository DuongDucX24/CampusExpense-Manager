package com.example.se07101campusexpenses.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.example.se07101campusexpenses.R;
import com.example.se07101campusexpenses.database.AppDatabase;
import com.example.se07101campusexpenses.database.UserRepository;
import com.example.se07101campusexpenses.model.User;
import com.example.se07101campusexpenses.security.PasswordUtils;
import com.example.se07101campusexpenses.util.SessionManager;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.concurrent.Executor;

public class LoginActivity extends AppCompatActivity {
    EditText edtUsernameOrEmail, edtPassword;
    Button btnLogin;
    private UserRepository userRepository;
    Button btnRegister;
    TextView tvForgotPassword;
    TextView tvLoginTitle;

    // Preference keys
    private static final String PREFS_NAME = "user_prefs";
    private static final String KEY_USER_ID = "user_id";
    private static final int DEFAULT_USER_ID = -1;

    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    private SessionManager sessionManager;
    private boolean isLockMode = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        // Check if this is a session lock situation
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int userId = sharedPreferences.getInt(KEY_USER_ID, DEFAULT_USER_ID);

        if (userId != DEFAULT_USER_ID && !sessionManager.isSessionLocked()) {
            // User is logged in and session is not locked, go to MenuActivity
            Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Determine if we're in lock mode
        isLockMode = (userId != DEFAULT_USER_ID && sessionManager.isSessionLocked());

        // User is not logged in or session is locked, proceed with login layout
        setContentView(R.layout.activity_login);
        edtPassword = findViewById(R.id.edtPassword);
        edtUsernameOrEmail = findViewById(R.id.edtUsername);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvLoginTitle = findViewById(R.id.tvLoginTitle);

        userRepository = new UserRepository(this);
        setupLoginButton();

        edtUsernameOrEmail.setHint("Username or Email");

        tvForgotPassword.setEnabled(true);
        tvForgotPassword.setAlpha(1.0f);

        // Configure UI for lock mode
        if (isLockMode) {
            setupLockMode();
        }

        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

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
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                String usernameOrEmail = edtUsernameOrEmail.getText().toString().trim();
                boolean isEmail = Patterns.EMAIL_ADDRESS.matcher(usernameOrEmail).matches();

                AppDatabase.databaseWriteExecutor.execute(() -> {
                    final User finalUser = isEmail
                            ? userRepository.getUserByEmail(usernameOrEmail)
                            : userRepository.getUserByUsername(usernameOrEmail);

                    runOnUiThread(() -> {
                        if (finalUser != null) {
                            // Unlock session and proceed
                            sessionManager.unlockSession();

                            SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putInt(KEY_USER_ID, finalUser.id);
                            editor.apply();

                            // Store username for future lock screens
                            sessionManager.setLockedUsername(finalUser.getUsername());

                            String message = isLockMode ? "Session unlocked" : "Biometric login successful";
                            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    "User not found. Please check username/email.", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            }

            @Override
            public void onAuthenticationFailed() {
                Toast.makeText(getApplicationContext(), "Authentication failed",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(isLockMode ? "Unlock Session" : "Biometric login for your app")
                .setSubtitle(isLockMode ? "Verify your identity to continue" : "Log in using your biometric credential")
                .setNegativeButtonText("Use account password")
                .build();

        Button btnBiometricLogin = findViewById(R.id.btnBiometricLogin);
        btnBiometricLogin.setOnClickListener(view -> biometricPrompt.authenticate(promptInfo));
    }

    private void setupLockMode() {
        // Show lock reason toast
        Toast.makeText(this, "Session locked due to inactivity", Toast.LENGTH_LONG).show();

        // Update title if it exists
        if (tvLoginTitle != null) {
            tvLoginTitle.setText("Session Locked");
        }

        // Pre-fill username and make it read-only
        String lockedUsername = sessionManager.getLockedUsername();
        if (!TextUtils.isEmpty(lockedUsername)) {
            edtUsernameOrEmail.setText(lockedUsername);
            edtUsernameOrEmail.setEnabled(false);
            edtUsernameOrEmail.setAlpha(0.7f);
        }

        // Hide register button in lock mode
        btnRegister.setVisibility(View.GONE);

        // Update login button text
        btnLogin.setText("Unlock");
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
                                // Unlock session if it was locked
                                sessionManager.unlockSession();

                                SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putInt(KEY_USER_ID, finalUser.id);
                                editor.apply();

                                // Store username for future lock screens
                                sessionManager.setLockedUsername(finalUser.getUsername());

                                String message = isLockMode ? "Session unlocked" : "Login successful";
                                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
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
