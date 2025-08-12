package com.example.se07101campusexpenses.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
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

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText edtEmail, edtSecurityAnswer, edtNewPassword, edtConfirmPassword;
    private Button btnFindAccount, btnVerifyAnswer, btnResetPassword, btnBackToLogin;
    private TextView tvInstructions, tvSecurityQuestion;
    private LinearLayout layoutEmailEntry, layoutSecurityQuestion, layoutResetPassword;
    private UserRepository userRepository;

    // Store the user for the recovery process
    private User recoveryUser;

    // Password validation pattern - at least one special character
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[^a-zA-Z0-9]");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        userRepository = new UserRepository(this);

        // Initialize views
        edtEmail = findViewById(R.id.edtEmail);
        edtSecurityAnswer = findViewById(R.id.edtSecurityAnswer);
        edtNewPassword = findViewById(R.id.edtNewPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);

        btnFindAccount = findViewById(R.id.btnFindAccount);
        btnVerifyAnswer = findViewById(R.id.btnVerifyAnswer);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);

        tvInstructions = findViewById(R.id.tvInstructions);
        tvSecurityQuestion = findViewById(R.id.tvSecurityQuestion);

        layoutEmailEntry = findViewById(R.id.layoutEmailEntry);
        layoutSecurityQuestion = findViewById(R.id.layoutSecurityQuestion);
        layoutResetPassword = findViewById(R.id.layoutResetPassword);

        // Initial layout setup
        layoutEmailEntry.setVisibility(View.VISIBLE);
        layoutSecurityQuestion.setVisibility(View.GONE);
        layoutResetPassword.setVisibility(View.GONE);

        setupButtons();
    }

    private void setupButtons() {
        // Find account button
        btnFindAccount.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                edtEmail.setError("Email is required");
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                edtEmail.setError("Please enter a valid email address");
                return;
            }

            // Check if email exists in database
            AppDatabase.databaseWriteExecutor.execute(() -> {
                recoveryUser = userRepository.getUserByEmail(email);
                runOnUiThread(() -> {
                    if (recoveryUser != null) {
                        // Show security question
                        tvSecurityQuestion.setText(recoveryUser.getSecurityQuestion());

                        // Move to security question step
                        layoutEmailEntry.setVisibility(View.GONE);
                        layoutSecurityQuestion.setVisibility(View.VISIBLE);
                        tvInstructions.setText("Answer your security question");
                    } else {
                        Toast.makeText(ForgotPasswordActivity.this,
                                "No account found with this email address",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });

        // Verify security answer button
        btnVerifyAnswer.setOnClickListener(v -> {
            String answer = edtSecurityAnswer.getText().toString().trim();

            if (TextUtils.isEmpty(answer)) {
                edtSecurityAnswer.setError("Please enter your answer");
                return;
            }

            // Check if answer matches
            if (answer.equals(recoveryUser.getSecurityAnswer())) {
                // Answer is correct, move to reset password step
                layoutSecurityQuestion.setVisibility(View.GONE);
                layoutResetPassword.setVisibility(View.VISIBLE);
                tvInstructions.setText("Create a new password for your account");
            } else {
                Toast.makeText(this, "Incorrect security answer", Toast.LENGTH_SHORT).show();
            }
        });

        // Reset password button
        btnResetPassword.setOnClickListener(v -> {
            String newPassword = edtNewPassword.getText().toString().trim();
            String confirmPassword = edtConfirmPassword.getText().toString().trim();

            // Validate password
            if (TextUtils.isEmpty(newPassword)) {
                edtNewPassword.setError("Password is required");
                return;
            }

            if (newPassword.length() < 6) {
                edtNewPassword.setError("Password must be at least 6 characters");
                return;
            }

            if (!SPECIAL_CHAR_PATTERN.matcher(newPassword).find()) {
                edtNewPassword.setError("Password must contain at least one special character");
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                edtConfirmPassword.setError("Passwords do not match");
                return;
            }

            // Update password in database - making sure we keep all other user data intact
            try {
                String hashedPassword = PasswordUtils.hashPassword(newPassword);

                // Make a copy of the current user data to avoid concurrent modification issues
                final User updatedUser = new User();
                updatedUser.setId(recoveryUser.getId());
                updatedUser.setUsername(recoveryUser.getUsername());
                updatedUser.setEmail(recoveryUser.getEmail());
                updatedUser.setSecurityQuestion(recoveryUser.getSecurityQuestion());
                updatedUser.setSecurityAnswer(recoveryUser.getSecurityAnswer());
                updatedUser.setPassword(hashedPassword);

                AppDatabase.databaseWriteExecutor.execute(() -> {
                    try {
                        // Use the dedicated update method
                        userRepository.updateUser(updatedUser);

                        runOnUiThread(() -> {
                            Toast.makeText(ForgotPasswordActivity.this,
                                    "Password reset successful",
                                    Toast.LENGTH_SHORT).show();

                            // Return to login screen after a slight delay to show the toast
                            new android.os.Handler().postDelayed(() -> {
                                // Make sure we're still active
                                if (!isFinishing()) {
                                    setResult(RESULT_OK);
                                    finish();
                                }
                            }, 1000); // Reduced to 1 second for better UX
                        });
                    } catch (Exception e) {
                        Log.e("ForgotPasswordActivity", "Error updating user: " + e.getMessage(), e);
                        runOnUiThread(() -> {
                            Toast.makeText(ForgotPasswordActivity.this,
                                    "Error: Could not update password. Please try again.",
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                Log.e("ForgotPasswordActivity", "Error hashing password", e);
                Toast.makeText(this, "Error resetting password", Toast.LENGTH_SHORT).show();
            }
        });

        // Back to login button
        btnBackToLogin.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }
}
