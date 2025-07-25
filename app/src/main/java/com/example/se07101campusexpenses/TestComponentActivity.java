package com.example.se07101campusexpenses;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class TestComponentActivity extends AppCompatActivity {
    private Button btnExitApp;
    private DatePicker calendarSchool;
    private EditText edtDate, edtChooseTime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_component);
        btnExitApp = findViewById(R.id.btnExitApp);
        calendarSchool = findViewById(R.id.dtpCalendar);
        edtChooseTime = findViewById(R.id.edtChooseTime);
        edtDate = findViewById(R.id.edtMyTime);
        edtDate.setEnabled(false);

        // nhan du lieu tu DemoIntentActivity gui sang
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null){
            // co du lieu gui sang
            // nhan du lieu
            String url = bundle.getString("MY_URL", "");
            String phone = bundle.getString("MY_PHONE", "");
            int idUser = bundle.getInt("ID_USER", 0);

            Toast.makeText(TestComponentActivity.this, url + "-" + phone + "-" + idUser, Toast.LENGTH_LONG).show();
        }

        //setup date picker
        Calendar today = Calendar.getInstance();
        calendarSchool.init(today.get(Calendar.YEAR),
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH),
                new DatePicker.OnDateChangedListener() {
                    @Override
                    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // dd-mm-yyyy
                        String myDate = dayOfMonth + "-" + (monthOfYear + 1) + "-" + year;
                        edtDate.setText(myDate);
                    }
                });

        edtChooseTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        TestComponentActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                edtChooseTime.setText(dayOfMonth + "-" + (month + 1) + "-" +year);
                            }
                        }, year, month, day );
                datePickerDialog.show();
            }
        });

        btnExitApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // bat ra AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(TestComponentActivity.this);
                builder.setMessage("Do you want to exit App ?");
                builder.setTitle("Alert !");
                builder.setCancelable(false);
                builder.setPositiveButton("Yes", (DialogInterface.OnClickListener)(dialog, which) -> {
                    // khi bam button Yes - dong ung dung
                    finish();
                });
                builder.setNegativeButton("No", (DialogInterface.OnClickListener)(dialog, which) -> {
                    dialog.cancel(); // huy khong lam gi ca khi bam Button No
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
    }
}
