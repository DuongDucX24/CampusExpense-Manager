package com.example.se07101campusexpenses;

import android.app.Application;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import com.example.se07101campusexpenses.service.RecurringExpenseWorker;
import java.util.concurrent.TimeUnit;

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        setupRecurringExpenseWorker();
    }

    private void setupRecurringExpenseWorker() {
        PeriodicWorkRequest recurringWorkRequest =
                new PeriodicWorkRequest.Builder(RecurringExpenseWorker.class, 1, TimeUnit.DAYS)
                        .build();

        WorkManager.getInstance(this).enqueue(recurringWorkRequest);
    }
}

