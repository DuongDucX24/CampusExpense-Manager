package com.example.se07101campusexpenses;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class TestLifeCycleActivity extends AppCompatActivity {
    private static final String FLAG_LOGS = "FLAG_LOGS";
    Button btnFirstActivity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ham se nay se duoc goi khi 1 activity dc tao
        // ham nay se la noi tao ra cac bien, hay load giao dien ....
        // ham nay se chay dau tien nhat va chi chay duy nhat 1 lan trong toan bo vong doi cua activity
        setContentView(R.layout.activity_test_life_cycle);
        Log.i(FLAG_LOGS, "****** onCreate is running ******");
        // anh xa giao dien - tim phn tu ngoai view(giao dien)
        btnFirstActivity = findViewById(R.id.btnFirstActivity); // tim phan tu
        // bat su kien
        btnFirstActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // bat ra thong bao
                Toast.makeText(TestLifeCycleActivity.this, "Go to an other Activity", Toast.LENGTH_SHORT).show();
                // di chuyen sang mot activity khac
                Intent intent = new Intent(TestLifeCycleActivity.this, LifeCycleSecondActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // ham nay duoc goi ngay khi Activity duoc hien thi len man hinh
        Log.i(FLAG_LOGS,"****** onStart is running ******");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // ham nay se duoc goi ngay khi Activity co the tuong voi nguoi dung
        Log.i(FLAG_LOGS, "****** onResume is running *******");
    }
    // khi onResume chay xong thi Activity dang hoat dong

    @Override
    protected void onPause() {
        super.onPause();
        // Ham nay se duoc goi khi chuan bi co 1 activity khac duoc kich hoat
        Log.i(FLAG_LOGS, "****** onPause is running ******");
    }

    @Override
    protected void onStop() {
        super.onStop();
        // ham nay duoc goi khi Activity bi an di
        Log.i(FLAG_LOGS,"****** onStop is running ******");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        // hien thi lai Activity da tung bi an di
        Log.i(FLAG_LOGS, "******* onRestart *******");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // ham nay se duoc goi khi App bi huy
        Log.i(FLAG_LOGS,"****** onDestroy ******");
    }
}
