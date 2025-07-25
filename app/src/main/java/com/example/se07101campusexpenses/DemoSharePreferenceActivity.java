package com.example.se07101campusexpenses;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class DemoSharePreferenceActivity extends AppCompatActivity {
    EditText edtNumber1, edtNumber2, edtResult;
    Button btnSumNumber, btnClearData;
    TextView tvHistory;
    private String history = ""; // logs actions of users
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_share_preference);
        edtNumber1 = findViewById(R.id.edtNumber1);
        edtNumber2 = findViewById(R.id.edtNumber2);
        edtResult = findViewById(R.id.edtResult);
        btnSumNumber = findViewById(R.id.btnSumNumber);
        btnClearData = findViewById(R.id.btnClearData);
        tvHistory = findViewById(R.id.tvHistory);

        // get data from share preferences
        SharedPreferences share = getSharedPreferences("calculator", MODE_PRIVATE);
        history = share.getString("OPERATOR_PLUS", "");
        tvHistory.setText(history);

        // block Edit Text Result
        edtResult.setEnabled(false);
        // event click button Sum number
        btnSumNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long number1 = Integer.parseInt(edtNumber1.getText().toString().trim());
                long number2 = Integer.parseInt(edtNumber2.getText().toString().trim());
                long result = number1 + number2;
                edtResult.setText(String.valueOf(result));
                history += number1 + " + " + number2 + " = " + result;
                tvHistory.setText(history);
                history += '\n'; // ky tu xuong dong cho moi phep toan
            }
        });
        btnClearData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                history = "";
                tvHistory.setText(history);
                edtNumber1.setText(history);
                edtNumber2.setText(history);
                edtResult.setText(history);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        // save data to Share preferences
        SharedPreferences myPref = getSharedPreferences("calculator", MODE_PRIVATE);
        SharedPreferences.Editor editor = myPref.edit();
        editor.putString("OPERATOR_PLUS", history);
        editor.apply();
    }
}
