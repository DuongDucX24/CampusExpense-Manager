package com.example.se07101campusexpenses;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class TestEventActivity extends AppCompatActivity {

    EditText edtData;
    Button btnClickMe, btnUnblock, btnGender;
    CheckBox cbAccept;
    RadioGroup radGender;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_event);
        edtData = findViewById(R.id.edtText);
        btnClickMe = findViewById(R.id.btnClickMe);
        btnUnblock = findViewById(R.id.btnUnblock);
        cbAccept = findViewById(R.id.cbAccept);
        btnGender = findViewById(R.id.btnConfirmGender);
        radGender = findViewById(R.id.radGender);

        // block element
        edtData.setEnabled(false);
        btnClickMe.setEnabled(false);

        btnGender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // xac dinh xem chon gioi tinh nao?
                int selectedId = radGender.getCheckedRadioButtonId();
                RadioButton rad = (RadioButton) findViewById(selectedId);
                if (rad == null){
                    // nguoi dung chua chon gioi tinh
                    Toast.makeText(TestEventActivity.this, "Choose Gender, please", Toast.LENGTH_SHORT).show();
                } else {
                    // da chon
                    String gender = rad.getText().toString().trim();
                    Toast.makeText(TestEventActivity.this, gender, Toast.LENGTH_SHORT).show();
                }
            }
        });

        cbAccept.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    edtData.setEnabled(true);
                    btnClickMe.setEnabled(true);
                } else {
                    edtData.setEnabled(false);
                    btnClickMe.setEnabled(false);
                }
            }
        });

        btnUnblock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // unblock
                edtData.setEnabled(true);
                btnClickMe.setEnabled(true);
            }
        });

        // bat su kien khi click Button
        btnClickMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // lay du lieu nguoi dung nhap vao Edit Text
                String data = edtData.getText().toString().trim();
                if (TextUtils.isEmpty(data)){
                    edtData.setError("Data can not empty");
                    return;
                }
                Toast.makeText(TestEventActivity.this, data, Toast.LENGTH_LONG).show();
            }
        });
    }
}
