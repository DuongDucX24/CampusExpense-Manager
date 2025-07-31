package com.example.se07101campusexpenses;

import static android.Manifest.permission.CALL_PHONE;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class DemoIntentActivity extends AppCompatActivity {
    EditText edtUrl, edtPhone;
    Button btnUrl, btnCallPhone, btnGotoActivity;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_intent);
        edtUrl = findViewById(R.id.edtURL);
        btnUrl = findViewById(R.id.btnLoadURL);
        edtPhone = findViewById(R.id.phone);
        btnCallPhone = findViewById(R.id.btnCallPhone);
        btnGotoActivity = findViewById(R.id.btnGotoActivity);

        btnGotoActivity.setOnClickListener(v -> {
            String url = edtUrl.getText().toString().trim();
            String phone = edtPhone.getText().toString().trim();
            // chuyen sang mot Activity khac va gui kem du lieu sang Activity do
            Intent intent = new Intent(DemoIntentActivity.this, TestComponentActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("MY_URL", url);
            bundle.putString("MY_PHONE", phone);
            bundle.putInt("ID_USER", 100);
            intent.putExtras(bundle);
            startActivity(intent);
        });

        btnCallPhone.setOnClickListener(v -> {
            String phone = edtPhone.getText().toString().trim();
            if (TextUtils.isEmpty(phone)){
                edtPhone.setError("Phone number can not empty");
                return;
            }
            // call to phone number
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:"+phone));
            // cap quyen duoc phep goi dien thoai
            if(ContextCompat.checkSelfPermission(getApplicationContext(), CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                startActivity(intent);
            } else {
                requestPermissions(new String[]{ CALL_PHONE },1);
            }
        });

        btnUrl.setOnClickListener(v -> {
            String url = edtUrl.getText().toString().trim();
            if (TextUtils.isEmpty(url)){
                edtUrl.setError("URL can not empty");
                return;
            }
            // load noi dung website
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        });
    }
}
