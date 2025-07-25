package com.example.se07101campusexpenses;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.se07101campusexpenses.database.UserRepository;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class RegisterActivity extends AppCompatActivity {
    EditText edtUsername, edtPassword, edtMail, edtPhone;
    Button btnSignUp;
    UserRepository repository;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        repository = new UserRepository(RegisterActivity.this);
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        edtMail = findViewById(R.id.edtMail);
        edtPhone = findViewById(R.id.edtPhone);
        btnSignUp = findViewById(R.id.btnSignUp);
        registerUserAccount(); // saving account user to database !
    }
    private void registerUserAccount(){
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // su kien dang ky tai khoan cua nguoi dung
                String user = edtUsername.getText().toString().trim();
                if (TextUtils.isEmpty(user)){
                    edtUsername.setError("Enter username, please !");
                    return;
                }
                String password = edtPassword.getText().toString().trim();
                if (TextUtils.isEmpty(password)){
                    edtPassword.setError("Enter password, please !");
                    return;
                }
                String email = edtMail.getText().toString().trim();
                if (TextUtils.isEmpty(email)){
                    edtMail.setError("Enter your email, please !");
                    return;
                }
                String phone = edtPhone.getText().toString().trim();
                // save account to database
                long insert = repository.saveUserAccount(user, password, email, phone);
                if (insert == -1) {
                    // Fail
                    Toast.makeText(RegisterActivity.this, "Save account fail", Toast.LENGTH_SHORT).show();
                } else {
                    // Success
                    Toast.makeText(RegisterActivity.this, "Save account Successfully", Toast.LENGTH_SHORT).show();
                    // quay ve trang dang nhap
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            }
        });
    }
    private void signUpAccount(){
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = edtUsername.getText().toString().trim();
                String password = edtPassword.getText().toString().trim();
                if (TextUtils.isEmpty(user)){
                    edtUsername.setError("Username can not empty");
                    return;
                }
                if (TextUtils.isEmpty(password)){
                    edtPassword.setError("Password can not empty");
                    return;
                }
                // save data to file
                FileOutputStream fileOutput = null;
                try {
                    user = user + "|";
                    fileOutput = openFileOutput("user.txt", Context.MODE_APPEND);
                    fileOutput.write(user.getBytes(StandardCharsets.UTF_8));
                    fileOutput.write(password.getBytes(StandardCharsets.UTF_8));
                    fileOutput.write('\n');
                    fileOutput.close();
                    edtUsername.setText("");
                    edtPassword.setText("");
                    Toast.makeText(RegisterActivity.this, "Sign up account successfully", Toast.LENGTH_SHORT).show();

                } catch (RuntimeException | FileNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
