package com.example.se07101campusexpenses.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.se07101campusexpenses.R;
import com.example.se07101campusexpenses.activities.LoginActivity;
import com.example.se07101campusexpenses.database.AppDatabase;
import com.example.se07101campusexpenses.database.UserRepository;
import com.example.se07101campusexpenses.model.User;

public class ProfileFragment extends Fragment {

    private TextView tvProfileUsername, tvProfileEmail;
    private UserRepository userRepository;
    private int userId;
    private static final String SUPPORT_EMAIL = "duygraphics@gmail.com";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvProfileUsername = view.findViewById(R.id.tvProfileUsername);
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        Button btnLogout = view.findViewById(R.id.btnLogout);
        Button btnSendFeedback = view.findViewById(R.id.btnSendFeedback);

        userRepository = new UserRepository(requireContext());
        userId = requireActivity().getSharedPreferences("user_prefs", 0).getInt("user_id", -1);

        loadUserProfile();

        btnLogout.setOnClickListener(v -> logout());
        btnSendFeedback.setOnClickListener(v -> sendFeedback());

        return view;
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
     * Opens the email app with a pre-filled feedback message template
     */
    private void sendFeedback() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{SUPPORT_EMAIL});
        intent.putExtra(Intent.EXTRA_SUBJECT, "CampusExpense Manager Feedback");
        intent.putExtra(Intent.EXTRA_TEXT, 
                "Hello,\n\nI would like to provide the following feedback about the CampusExpense Manager app:\n\n" +
                "[Your feedback here]\n\n" +
                "Device information:\n" +
                "- Device model: " + android.os.Build.MODEL + "\n" +
                "- Android version: " + android.os.Build.VERSION.RELEASE + "\n" +
                "- App version: " + getAppVersion() + "\n\n" +
                "Thank you!");
        
        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(requireContext(), "No email app found", Toast.LENGTH_SHORT).show();
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
}
