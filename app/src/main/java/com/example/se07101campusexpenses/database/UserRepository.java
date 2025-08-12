package com.example.se07101campusexpenses.database;

import android.content.Context;
import com.example.se07101campusexpenses.model.User;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class UserRepository {
    private final UserDao userDao;

    public UserRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        userDao = db.userDao();
    }

    public void saveUserAccount(final User user) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            userDao.insert(user);
        });
    }

    public User login(final String username, final String password) {
        Future<User> future = AppDatabase.databaseWriteExecutor.submit(new Callable<User>() {
            @Override
            public User call() throws Exception {
                return userDao.login(username, password);
            }
        });
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public User getUserByUsername(final String username) {
        Future<User> future = AppDatabase.databaseWriteExecutor.submit(new Callable<User>() {
            @Override
            public User call() throws Exception {
                return userDao.getUserByUsername(username);
            }
        });
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }
}
