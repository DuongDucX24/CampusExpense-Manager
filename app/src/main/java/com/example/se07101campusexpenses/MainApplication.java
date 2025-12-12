package com.example.se07101campusexpenses;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import com.example.se07101campusexpenses.service.RecurringExpenseWorker;
import com.example.se07101campusexpenses.util.SessionManager;
import java.util.concurrent.TimeUnit;

public class MainApplication extends Application implements Application.ActivityLifecycleCallbacks {

    private int activityReferences = 0;
    private boolean isActivityChangingConfigurations = false;
    private SessionManager sessionManager;

    @Override
    public void onCreate() {
        super.onCreate();
        sessionManager = new SessionManager(this);
        registerActivityLifecycleCallbacks(this);
        setupRecurringExpenseWorker();
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        // Not needed
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        if (++activityReferences == 1 && !isActivityChangingConfigurations) {
            // App enters foreground - clear background time to prevent immediate lock
            // The actual timeout check happens in each activity's onResume
        }
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        // Not needed - session check handled in individual activities
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        // Not needed
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        isActivityChangingConfigurations = activity.isChangingConfigurations();
        if (--activityReferences == 0 && !isActivityChangingConfigurations) {
            // App goes to background - record the time
            sessionManager.recordBackgroundTime();
        }
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
        // Not needed
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        // Not needed
    }

    private void setupRecurringExpenseWorker() {
        PeriodicWorkRequest recurringWorkRequest =
                new PeriodicWorkRequest.Builder(RecurringExpenseWorker.class, 1, TimeUnit.DAYS)
                        .build();

        WorkManager.getInstance(this)
                .enqueueUniquePeriodicWork(
                        "RecurringExpenseWork",
                        ExistingPeriodicWorkPolicy.KEEP,
                        recurringWorkRequest
                );
    }
}
