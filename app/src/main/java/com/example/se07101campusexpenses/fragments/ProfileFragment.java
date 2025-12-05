package com.example.se07101campusexpenses.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.se07101campusexpenses.R;
import com.example.se07101campusexpenses.activities.LoginActivity;
import com.example.se07101campusexpenses.activities.ImportActivity;
import com.example.se07101campusexpenses.activities.ExportActivity;
import com.example.se07101campusexpenses.database.AppDatabase;
import com.example.se07101campusexpenses.database.UserRepository;
import com.example.se07101campusexpenses.model.User;
import com.example.se07101campusexpenses.security.PasswordUtils;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.concurrent.Executor;

import androidx.appcompat.app.AlertDialog;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

public class ProfileFragment extends Fragment {

    private TextView tvProfileUsername, tvProfileEmail;
    private UserRepository userRepository;
    private int userId;
    private static final String SUPPORT_EMAIL = "duygraphics@gmail.com";
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvProfileUsername = view.findViewById(R.id.tvProfileUsername);
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        Button btnLogout = view.findViewById(R.id.btnLogout);
        Button btnSendFeedback = view.findViewById(R.id.btnSendFeedback);
        Button btnDeleteAccount = view.findViewById(R.id.btnDeleteAccount);
        Button btnImport = view.findViewById(R.id.btnImport);
        Button btnExport = view.findViewById(R.id.btnExport);

        btnImport.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ImportActivity.class);
            startActivity(intent);
        });

        btnExport.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ExportActivity.class);
            startActivity(intent);
        });

        userRepository = new UserRepository(requireContext());
        userId = requireActivity().getSharedPreferences("user_prefs", 0).getInt("user_id", -1);

        loadUserProfile();

        btnLogout.setOnClickListener(v -> logout());
        btnSendFeedback.setOnClickListener(v -> sendFeedback());
        btnDeleteAccount.setOnClickListener(v -> showDeleteConfirmationDialog());

        setupBiometricPrompt();

        return view;
    }

    private void setupBiometricPrompt() {
        Executor executor = ContextCompat.getMainExecutor(requireContext());
        biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                deleteAccount();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getContext(), "Biometric authentication failed.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getContext(), "Biometric authentication error: " + errString, Toast.LENGTH_SHORT).show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Authentication")
                .setSubtitle("Confirm account deletion with your biometric credential")
                .setNegativeButtonText("Cancel")
                .build();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Delete Account");
        builder.setMessage("Are you sure you want to delete your account? This action is irreversible. Please enter your password to confirm.");

        final EditText input = new EditText(requireContext());
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("Delete", (dialog, which) -> {
            String password = input.getText().toString();
            verifyPasswordAndDelete(password);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.setNeutralButton("Use Biometrics", (dialog, which) -> biometricPrompt.authenticate(promptInfo));

        builder.show();
    }

    private void verifyPasswordAndDelete(String password) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            User user = userRepository.getUserById(userId);
            if (user != null) {
                try {
                    if (PasswordUtils.verifyPassword(password, user.getPassword())) {
                        deleteAccount();
                    } else {
                        requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Incorrect password.", Toast.LENGTH_SHORT).show());
                    }
                } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                    e.printStackTrace();
                    requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error verifying password.", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void deleteAccount() {
        userRepository.deleteUserAccount(userId);
        requireActivity().runOnUiThread(() -> {
            Toast.makeText(getContext(), "Account deleted successfully.", Toast.LENGTH_SHORT).show();
            logout();
        });
    }

    private void loadUserProfile() {
        if (userId != -1) {
            AppDatabase.databaseWriteExecutor.execute(() -> {
                User user = userRepository.getUserById(userId);
                if (user != null && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        tvProfileUsername.setText("Username: " + user.getUsername());
                        tvProfileEmail.setText("Email: " + user.getEmail());
                    });
                }
            });
        }
    }
    
    /**
     * Opens the Gmail app with a pre-filled feedback message template
     */
    private void sendFeedback() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + SUPPORT_EMAIL));
        intent.putExtra(Intent.EXTRA_SUBJECT, "CampusExpense Manager Feedback");
        intent.putExtra(Intent.EXTRA_TEXT, 
                "Hello,\n\nI would like to provide the following feedback about the CampusExpense Manager app:\n\n" +
                "[Your feedback here]\n\n" +
                "Device information:\n" +
                "- Device model: " + android.os.Build.MODEL + "\n" +
                "- Android version: " + android.os.Build.VERSION.RELEASE + "\n" +
                "- App version: " + getAppVersion() + "\n\n" +
                "Thank you!");
        
        // Try to specifically open Gmail
        intent.setPackage("com.google.android.gm");

        try {
            startActivity(intent);
        } catch (android.content.ActivityNotFoundException ex) {
            // If Gmail is not installed, try any email app
            intent.setPackage(null);
            if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
                startActivity(intent);
                Toast.makeText(requireContext(), "Gmail not found, opening default email app", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "No email app found", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    /**
     * Gets the current app version name
     */
    private String getAppVersion() {
        try {
            return requireActivity().getPackageManager()
                    .getPackageInfo(requireActivity().getPackageName(), 0).versionName;
        } catch (Exception e) {
            return "unknown";
        }
    }

    private void logout() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("user_prefs", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("user_id");
        editor.apply();

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserProfile();
    }
}
