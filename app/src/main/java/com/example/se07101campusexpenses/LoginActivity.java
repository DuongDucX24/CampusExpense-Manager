package com.example.se07101campusexpenses;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.se07101campusexpenses.database.AppDatabase;
import com.example.se07101campusexpenses.database.UserRepository;
import com.example.se07101campusexpenses.model.User;

public class LoginActivity extends AppCompatActivity {
    EditText edtUsername, edtPassword;
    Button btnLogin;
    private UserRepository userRepository;
    TextView tvRegister;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        edtPassword = findViewById(R.id.edtPassword);
        edtUsername = findViewById(R.id.edtUsername);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegisterAccount);
        userRepository = new UserRepository(this);
        checkUserLogin();

        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
    private void checkUserLogin(){
        btnLogin.setOnClickListener(v -> {
            String username = edtUsername.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            if (TextUtils.isEmpty(username)){
                edtUsername.setError("Enter username");
                return;
            }
            if (TextUtils.isEmpty(password)){
                edtPassword.setError("Enter password");
                return;
            }

            AppDatabase.databaseWriteExecutor.execute(() -> {
                User user = userRepository.login(username, password);
                runOnUiThread(() -> {
                    if (user != null) {
                        // Save user ID to SharedPreferences
                        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt("user_id", user.id);
                        editor.apply();

                        Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });
    }
}
