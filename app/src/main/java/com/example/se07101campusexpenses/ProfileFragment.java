package com.example.se07101campusexpenses;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.se07101campusexpenses.database.AppDatabase;
import com.example.se07101campusexpenses.database.UserRepository;
import com.example.se07101campusexpenses.model.User;

public class ProfileFragment extends Fragment {

    private TextView tvProfileUsername, tvProfileEmail;
    private UserRepository userRepository;
    private int userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvProfileUsername = view.findViewById(R.id.tvProfileUsername);
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        Button btnLogout = view.findViewById(R.id.btnLogout);

        userRepository = new UserRepository(requireContext());
        userId = requireActivity().getSharedPreferences("user_prefs", 0).getInt("user_id", -1);

        loadUserProfile();

        btnLogout.setOnClickListener(v -> logout());

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
