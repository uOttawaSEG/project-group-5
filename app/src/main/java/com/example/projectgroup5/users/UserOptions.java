package com.example.projectgroup5.users;

import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class UserOptions {

    public interface UsersCallback {
        void onDataReceived(List<User> userIds);
    }

    public static void getUsers(UsersCallback callback, int userRegistrationState) {
        List<User> pendingUsers = new ArrayList<>();
        DatabaseManager databaseManager = DatabaseManager.getDatabaseManager();
        Log.d("UserOptions", "Getting pending users");

        databaseManager.getUserIdByMatchingData("UserRegistrationState", ""+userRegistrationState, new DatabaseManager.DataCallback() {
            @Override
            public void onDataReceived(List<String> userIds) {
                Log.d("UserOptions", "User IDs: " + userIds);

                // Create a counter to track completed user data retrieval
                AtomicInteger remainingCalls = new AtomicInteger(userIds.size());

                for (String userId : userIds) {
                    Log.d("UserOptions", "User ID: " + userId);

                    DatabaseManager.getDatabaseManager().getUserData(userId, UserSession.USER_TYPE, new UserSession.FirebaseCallback<Object>() {
                        @Override
                        public void onCallback(Object userType) {
                            Log.d("UserOptions", "User type: " + userType);
                            User user = User.newUser(userId, (int) (long) ((Long) userType));
                            pendingUsers.add(user);

                            // Decrement the counter and check if all callbacks are complete
                            if (remainingCalls.decrementAndGet() == 0) {
                                Log.d("UserOptions", "Pending users: " + pendingUsers);
                                // Call the callback with the retrieved pending users
                                callback.onDataReceived(pendingUsers);
                            }
                        }
                    });
                }

                // If there are no user IDs, we should call the callback immediately
                if (userIds.isEmpty()) {
                    callback.onDataReceived(pendingUsers);
                }
            }
        });
    }

    public static void getAcceptedUsers(UsersCallback callback) {
        getUsers(callback, UserSession.ACCEPTED);
    }
    public static void getPendingUsers(UsersCallback callback) {
        getUsers(callback, UserSession.WAITLISTED);
    }
    public static void getRejectedUsers(UsersCallback callback) {
        getUsers(callback, UserSession.REJECTED);
    }

}
