package com.example.se07101campusexpenses.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.se07101campusexpenses.R;
import com.example.se07101campusexpenses.database.AppDatabase;
import com.example.se07101campusexpenses.model.Budget;
import com.example.se07101campusexpenses.model.Expense;

import java.util.List;

public class NotificationService extends Service {
    private static final String CHANNEL_ID = "BudgetChannel";
    private Handler handler = new Handler();
    private AppDatabase appDatabase;

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            checkBudgetStatus();
            handler.postDelayed(this, 60 * 60 * 1000); // Check every hour
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        appDatabase = AppDatabase.getInstance(getApplicationContext());
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler.post(runnable);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Budget Notification Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private void checkBudgetStatus() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Budget> budgets = appDatabase.budgetDao().getAllBudgets();
            for (Budget budget : budgets) {
                List<Expense> expenses = appDatabase.expenseDao().getExpensesByUserId(budget.userId);
                double totalExpenses = 0;
                for (Expense expense : expenses) {
                    if (expense.category.equals(budget.name)) {
                        totalExpenses += expense.amount;
                    }
                }

                if (totalExpenses >= budget.amount) {
                    sendNotification("Budget Exceeded", "You have exceeded your budget for " + budget.name);
                } else if (totalExpenses >= budget.amount * 0.9) {
                    sendNotification("Budget Alert", "You are approaching your budget limit for " + budget.name);
                }
            }
        });
    }

    private void sendNotification(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }
}

