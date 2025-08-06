package com.example.se07101campusexpenses.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.example.se07101campusexpenses.MainActivity;
import com.example.se07101campusexpenses.R;
import com.example.se07101campusexpenses.database.BudgetRepository;
import com.example.se07101campusexpenses.database.ExpenseRepository;
import com.example.se07101campusexpenses.model.Budget;

import java.util.List;

public class NotificationService extends Service {

    private static final String CHANNEL_ID = "BudgetChannel";
    private BudgetRepository budgetRepository;
    private ExpenseRepository expenseRepository;

    @Override
    public void onCreate() {
        super.onCreate();
        budgetRepository = new BudgetRepository(getApplication());
        expenseRepository = new ExpenseRepository(getApplication());
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(() -> {
            checkBudgets();
            stopSelf();
        }).start();
        return START_NOT_STICKY;
    }

    private void checkBudgets() {
        List<Budget> budgets = budgetRepository.getAllBudgets();
        for (Budget budget : budgets) {
            double totalExpenses = expenseRepository.getTotalExpensesForCategory(budget.getName());
            if (totalExpenses >= budget.getAmount()) {
                sendNotification("Budget Exceeded", "You have exceeded your budget for " + budget.getName());
            } else if (totalExpenses >= budget.getAmount() * 0.9) {
                sendNotification("Budget Alert", "You are approaching your budget limit for " + budget.getName());
            }
        }
    }

    private void sendNotification(String title, String message) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Budget Notifications";
            String description = "Notifications for budget alerts";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
