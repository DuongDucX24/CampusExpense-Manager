package com.example.se07101campusexpenses;

import android.app.AlarmManager;
import android.content.Intent;
import android.os.Bundle;
import android.app.PendingIntent;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.se07101campusexpenses.service.NotificationService;
import com.example.se07101campusexpenses.service.RecurringExpenseService;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        scheduleRecurringExpenseService();
        scheduleNotificationService();
    }

    private void scheduleRecurringExpenseService() {
        Intent intent = new Intent(this, RecurringExpenseService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        // Run the service once a day
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    private void scheduleNotificationService() {
        Intent intent = new Intent(this, NotificationService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 1, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        // Run the service every hour
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), AlarmManager.INTERVAL_HOUR, pendingIntent);
    }
}