package com.example.projectgroup5.users;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class UserOptions {

    public interface UsersCallback {
        void onDataReceived(List<User> userIds);
    }

    public static void getUsersWithRegistrationStatus(UsersCallback callback, int userRegistrationState) {
        List<User> pendingUsers = new ArrayList<>();
        DatabaseManager databaseManager = DatabaseManager.getDatabaseManager();
        databaseManager.getUserIdByMatchingData("UserRegistrationState", String.valueOf(userRegistrationState), userIds -> {
            // Create a counter to track completed user data retrieval
            AtomicInteger remainingCalls = new AtomicInteger(userIds.size());
            for (String userId : userIds) {
                DatabaseManager.getDatabaseManager().getUserData(userId, UserSession.USER_TYPE, userType -> {
                    User user = User.newUser(userId, (int) (long) ((Long) userType));
                    pendingUsers.add(user);
                    // Decrement the counter and check if all callbacks are complete
                    if (remainingCalls.decrementAndGet() == 0) {
                        // Call the callback with the retrieved pending users
                        callback.onDataReceived(pendingUsers);
                    }
                });
            }
            // If there are no users, callback immediately
            if (userIds.isEmpty()) {
                callback.onDataReceived(pendingUsers);
            }
        });
    }

    public static void getAcceptedUsers(UsersCallback callback) {
        getUsersWithRegistrationStatus(callback, UserSession.ACCEPTED);
    }
    public static void getPendingUsers(UsersCallback callback) {
        getUsersWithRegistrationStatus(callback, UserSession.WAITLISTED);
    }
    public static void getRejectedUsers(UsersCallback callback) {
        getUsersWithRegistrationStatus(callback, UserSession.REJECTED);
    }

}
