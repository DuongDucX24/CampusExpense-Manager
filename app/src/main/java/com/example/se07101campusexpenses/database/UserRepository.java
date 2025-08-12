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
            // If the user has a valid ID (greater than 0), update instead of insert
            if (user.getId() > 0) {
                userDao.update(user);
            } else {
                userDao.insert(user);
            }
        });
    }

    public void updateUser(final User user) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            userDao.update(user);
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

    public User loginWithEmail(final String email, final String password) {
        Future<User> future = AppDatabase.databaseWriteExecutor.submit(new Callable<User>() {
            @Override
            public User call() throws Exception {
                return userDao.loginWithEmail(email, password);
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

    public User getUserByEmail(final String email) {
        Future<User> future = AppDatabase.databaseWriteExecutor.submit(new Callable<User>() {
            @Override
            public User call() throws Exception {
                return userDao.getUserByEmail(email);
            }
        });
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public User getUserById(final int userId) {
        Future<User> future = AppDatabase.databaseWriteExecutor.submit(() -> userDao.getUserById(userId));
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int getUserCount() {
        try {
            Future<Integer> future = AppDatabase.databaseWriteExecutor.submit(() -> userDao.getUserCount());
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
