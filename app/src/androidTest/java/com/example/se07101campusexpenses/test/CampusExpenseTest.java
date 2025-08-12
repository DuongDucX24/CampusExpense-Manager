package com.example.se07101campusexpenses.test;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.se07101campusexpenses.R;
import com.example.se07101campusexpenses.activities.LoginActivity;
import com.example.se07101campusexpenses.database.AppDatabase;
import com.example.se07101campusexpenses.database.BudgetRepository;
import com.example.se07101campusexpenses.database.ExpenseRepository;
import com.example.se07101campusexpenses.database.UserRepository;
import com.example.se07101campusexpenses.model.Budget;
import com.example.se07101campusexpenses.model.Expense;
import com.example.se07101campusexpenses.model.User;
import com.example.se07101campusexpenses.security.EncryptionHelper;
import com.example.se07101campusexpenses.security.PasswordUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test suite for CampusExpense Manager.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class CampusExpenseTest {
    private static final String TAG = "CampusExpenseTest";

    // Test user credentials
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "TestPassword123";
    private static final String TEST_EMAIL = "test@example.com";

    // Test expense data
    private static final String TEST_DESCRIPTION = "Test Expense";
    private static final double TEST_AMOUNT = 50000;
    private static final String TEST_CATEGORY = "Food";

    // Test budget data
    private static final String TEST_BUDGET_NAME = "Food Budget";
    private static final double TEST_BUDGET_AMOUNT = 100000;

    // Context used for repositories
    private Context context;

    // Database repositories
    private UserRepository userRepository;
    private ExpenseRepository expenseRepository;
    private BudgetRepository budgetRepository;

    // Test user ID
    private int testUserId = -1;

    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    @Before
    public void setup() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        // Initialize repositories
        userRepository = new UserRepository(context);
        expenseRepository = new ExpenseRepository(context);
        budgetRepository = new BudgetRepository(context);

        // Create test user if it doesn't exist
        createTestUser();
    }

    @After
    public void cleanup() {
        // Clean up test data
        if (testUserId != -1) {
            cleanupTestData();
        }
    }

    /**
     * Creates a test user for running tests
     */
    private void createTestUser() {
        final CountDownLatch latch = new CountDownLatch(1);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // Check if user already exists
                User existingUser = userRepository.getUserByUsername(TEST_USERNAME);

                if (existingUser == null) {
                    User testUser = new User();
                    testUser.setUsername(TEST_USERNAME);
                    testUser.setPassword(PasswordUtils.hashPassword(TEST_PASSWORD));
                    testUser.setEmail(TEST_EMAIL);

                    // Insert test user
                    userRepository.saveUserAccount(testUser);
                    
                    // Get the created user to retrieve its ID
                    User createdUser = userRepository.getUserByUsername(TEST_USERNAME);
                    if (createdUser != null) {
                        testUserId = createdUser.getId();
                    }
                } else {
                    testUserId = existingUser.getId();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error creating test user: " + e.getMessage(), e);
            } finally {
                latch.countDown();
            }
        });

        try {
            boolean completed = latch.await(5, TimeUnit.SECONDS);
            if (!completed) {
                Log.w(TAG, "Timed out waiting for test user creation");
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted while creating test user: " + e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Cleans up test data after tests
     */
    private void cleanupTestData() {
        final CountDownLatch latch = new CountDownLatch(1);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // Delete test user's expenses
                List<Expense> expenses = expenseRepository.getExpensesByUserId(testUserId);
                if (expenses != null) {
                    for (Expense expense : expenses) {
                        expenseRepository.deleteExpense(expense.getId());
                    }
                }

                // Delete test user's budgets
                List<Budget> budgets = budgetRepository.getBudgetsByUserId(testUserId);
                if (budgets != null) {
                    for (Budget budget : budgets) {
                        budgetRepository.delete(budget);
                    }
                }

                // We don't delete the test user as it may be needed for future tests
            } catch (Exception e) {
                Log.e(TAG, "Error cleaning up test data: " + e.getMessage(), e);
            } finally {
                latch.countDown();
            }
        });

        try {
            boolean completed = latch.await(5, TimeUnit.SECONDS);
            if (!completed) {
                Log.w(TAG, "Timed out waiting for test data cleanup");
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted while cleaning up test data: " + e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Simple login test using the actual UI element IDs
     */
    @Test
    public void testBasicLogin() {
        // Enter login credentials using the actual IDs from activity_login.xml
        onView(withId(R.id.edtUsername))
                .perform(clearText(), typeText(TEST_USERNAME), closeSoftKeyboard());

        onView(withId(R.id.edtPassword))
                .perform(clearText(), typeText(TEST_PASSWORD), closeSoftKeyboard());

        // Find login button by text (need to look for the actual button ID in the layout)
        onView(withText("Login")).perform(click());
        
        // Wait for login process
        SystemClock.sleep(2000);
    }
    
    /**
     * Test data encryption functionality
     */
    @Test
    public void testDataEncryption() {
        // Test basic encryption/decryption
        String testText = "Sensitive information";
        String encrypted = EncryptionHelper.encrypt(context, testText);
        String decrypted = EncryptionHelper.decrypt(context, encrypted);

        // Verify encryption worked
        assertNotNull("Encryption failed", encrypted);
        assertFalse("Encryption not working properly", testText.equals(encrypted));

        // Verify decryption worked
        assertEquals("Decryption failed", testText, decrypted);
    }

    // Helper methods

    /**
     * Adds a test expense directly to the database
     */
    private void addTestExpense() {
        final CountDownLatch latch = new CountDownLatch(1);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                Expense expense = new Expense();
                expense.setDescription(TEST_DESCRIPTION);
                expense.setAmount(TEST_AMOUNT);
                expense.setCategory(TEST_CATEGORY);
                expense.setDate(new SimpleDateFormat("dd/MM/yyyy", Locale.US).format(new Date()));
                expense.setUserId(testUserId);

                expenseRepository.addExpense(expense);
            } catch (Exception e) {
                Log.e(TAG, "Error adding test expense: " + e.getMessage(), e);
            } finally {
                latch.countDown();
            }
        });

        try {
            boolean completed = latch.await(5, TimeUnit.SECONDS);
            if (!completed) {
                Log.w(TAG, "Timed out waiting for test expense creation");
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted while adding test expense: " + e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Adds a test budget directly to the database
     */
    private void addTestBudget() {
        final CountDownLatch latch = new CountDownLatch(1);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                Budget budget = new Budget();
                budget.setName(TEST_BUDGET_NAME);
                budget.setAmount(TEST_BUDGET_AMOUNT);
                budget.setUserId(testUserId);
                
                // Use the appropriate method to add budget
                budgetRepository.insert(budget);
            } catch (Exception e) {
                Log.e(TAG, "Error adding test budget: " + e.getMessage(), e);
            } finally {
                latch.countDown();
            }
        });

        try {
            boolean completed = latch.await(5, TimeUnit.SECONDS);
            if (!completed) {
                Log.w(TAG, "Timed out waiting for test budget creation");
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted while adding test budget: " + e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Creates many test expenses for performance testing
     */
    private void createManyTestExpenses(int count) {
        final CountDownLatch latch = new CountDownLatch(1);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);

                for (int i = 0; i < count; i++) {
                    Expense expense = new Expense();
                    expense.setDescription("Test Expense " + i);
                    expense.setAmount(1000 * (i % 10 + 1));  // Varying amounts

                    // Alternate between a few categories
                    switch (i % 5) {
                        case 0: expense.setCategory("Food"); break;
                        case 1: expense.setCategory("Transport"); break;
                        case 2: expense.setCategory("Entertainment"); break;
                        case 3: expense.setCategory("Utilities"); break;
                        case 4: expense.setCategory("Other"); break;
                    }

                    // Set date to recent dates (past 30 days)
                    calendar.setTime(new Date());
                    calendar.add(Calendar.DAY_OF_MONTH, -(i % 30));
                    expense.setDate(dateFormat.format(calendar.getTime()));

                    expense.setUserId(testUserId);
                    expenseRepository.addExpense(expense);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error creating test expenses: " + e.getMessage(), e);
            } finally {
                latch.countDown();
            }
        });

        try {
            boolean completed = latch.await(30, TimeUnit.SECONDS);
            if (!completed) {
                Log.w(TAG, "Timed out waiting for test expenses creation");
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted while creating test expenses: " + e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
    }
}
