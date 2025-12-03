package com.example.se07101campusexpenses.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.se07101campusexpenses.R;
import com.example.se07101campusexpenses.database.AppDatabase;
import com.example.se07101campusexpenses.model.Budget;
import com.example.se07101campusexpenses.model.Expense;

import java.text.NumberFormat; // Added import
import java.util.List;
import java.util.Locale; // Added import

public class NotificationService extends Service {
    private static final String CHANNEL_ID = "BudgetChannel";
    private final Handler handler = new Handler();
    private AppDatabase appDatabase;
    private int userId;
    private NumberFormat vndFormat; // Added for currency formatting

    private final Runnable runnable = new Runnable() {
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
        SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1); // Get current user ID

        vndFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN")); // Initialize formatter
        vndFormat.setMaximumFractionDigits(0); // VND usually doesn't show decimals
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
        // minSdk is 29, so NotificationChannel APIs are always available
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "Budget Notification Channel",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private void checkBudgetStatus() {
        if (userId == -1) return; // Don't run if user is not logged in

        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Budget> budgets = appDatabase.budgetDao().getBudgetsByUserId(userId); // Fetch budgets for current user
            if (budgets == null) return;

            List<Expense> allUserExpenses = appDatabase.expenseDao().getExpensesByUserId(userId);

            for (Budget budget : budgets) {
                double totalExpensesForCategory = 0;
                String budgetCategory = budget.getName(); // budget.getName() is the category
                if (budgetCategory == null) continue; // Skip if budget name is null

                if (allUserExpenses != null) {
                    for (Expense expense : allUserExpenses) {
                        if (expense.getCategory() != null && expense.getCategory().equals(budgetCategory)) {
                            totalExpensesForCategory += expense.getAmount();
                        }
                    }
                }

                String formattedBudgetAmount = vndFormat.format(budget.getAmount());
                String formattedTotalExpenses = vndFormat.format(totalExpensesForCategory);

                if (totalExpensesForCategory >= budget.getAmount()) {
                    sendNotification("Budget Exceeded for " + budgetCategory,
                            "Spent " + formattedTotalExpenses + " of " + formattedBudgetAmount,
                            budgetCategory + "_exceeded"); // Unique ID part
                } else if (totalExpensesForCategory >= budget.getAmount() * 0.9) {
                     sendNotification("Budget Alert for " + budgetCategory,
                            "Spent " + formattedTotalExpenses + " of " + formattedBudgetAmount + " (90% limit)",
                             budgetCategory + "_alert"); // Unique ID part
                }
            }
        });
    }

    // Added budgetCategoryForId to make notification ID unique
    private void sendNotification(String title, String message, String notificationTag) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                // TODO: Replace R.drawable.ic_launcher_background with a proper notification icon e.g. R.drawable.ic_notification
                .setSmallIcon(R.drawable.ic_launcher_background) 
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message)) // For longer messages
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            // Use a unique ID for each notification, e.g., based on category name hashcode + tag
            notificationManager.notify(notificationTag.hashCode(), builder.build()); 
        }
    }
}
