package com.example.se07101campusexpenses;

import android.content.Context;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.example.se07101campusexpenses.database.AppDatabase;
import com.example.se07101campusexpenses.database.BudgetDao;
import com.example.se07101campusexpenses.model.Budget;
import com.example.se07101campusexpenses.model.Expense;
import com.example.se07101campusexpenses.database.ExpenseDao;
import com.example.se07101campusexpenses.model.User;
import com.example.se07101campusexpenses.database.UserDao;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.IOException;
import java.util.List;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class DatabaseTest {
    private AppDatabase db;
    private UserDao userDao;
    private ExpenseDao expenseDao;
    private BudgetDao budgetDao;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        userDao = db.userDao();
        expenseDao = db.expenseDao();
        budgetDao = db.budgetDao();
    }

    @After
    public void closeDb() {
        db.close();
    }

    @Test
    public void writeUserAndReadInList() {
        User user = new User();
        user.username = "testuser";
        user.password = "testpass";
        userDao.insert(user);
        User byName = userDao.login("testuser", "testpass");
        assertEquals("testuser", byName.username);
    }

    @Test
    public void writeExpenseAndReadInList() {
        User user = new User();
        user.username = "testuser";
        user.password = "testpass";
        userDao.insert(user);
        User retrievedUser = userDao.login("testuser", "testpass");

        Expense expense = new Expense();
        expense.userId = retrievedUser.id;
        expense.description = "Lunch";
        expense.amount = 12.50;
        expense.category = "Food";
        expense.date = "2025-08-06";
        expenseDao.insert(expense);

        List<Expense> expenses = expenseDao.getExpensesByUserId(retrievedUser.id);
        assertEquals(1, expenses.size());
        assertEquals("Lunch", expenses.get(0).description);
    }

    @Test
    public void writeBudgetAndReadInList() {
        User user = new User();
        user.username = "testuser";
        user.password = "testpass";
        userDao.insert(user);
        User retrievedUser = userDao.login("testuser", "testpass");

        Budget budget = new Budget();
        budget.setUserId(retrievedUser.id);
        budget.setName("Food");
        budget.setAmount(500.00);
        budgetDao.insert(budget);

        List<Budget> budgets = budgetDao.getBudgetsByUserId(retrievedUser.id);
        assertEquals(1, budgets.size());
        assertEquals("Food", budgets.get(0).getName());
    }
}
