package com.example.projectgroup5.users;

import android.util.Log;

import com.example.projectgroup5.database.DatabaseManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class UserOptions {

    /**
     * Callback interface for receiving a list of users.
     * <p>
     * This interface defines a method to be called when a list of user objects is available,
     * typically after querying a database or performing an asynchronous operation.
     */
    public interface UsersCallback {
        /**
         * Called when the data retrieval is complete and the user list is available.
         *
         * @param userIds A list of User objects that were retrieved.
         */
        void onDataReceived(List<User> userIds);
    }

    /**
     * Retrieves a list of users with a specific registration status.
     * <p>
     * This method queries the database for user IDs matching the specified registration state
     * and then retrieves the user type for each user. It collects the user instances into a list
     * and invokes the provided callback once all user data has been retrieved.
     *
     * @param callback              The callback to be invoked with the list of users once the data retrieval is complete.
     * @param userRegistrationState The registration state to filter users by. This should correspond to
     *                              the values defined in the user registration state enumeration.
     */
    public static void getUsersWithRegistrationStatus(UsersCallback callback, String userRegistrationState) {
        List<User> pendingUsers = new ArrayList<>();
        DatabaseManager databaseManager = DatabaseManager.getDatabaseManager();
        databaseManager.getUserIdByMatchingDataFromFirestore(DatabaseManager.USER_REGISTRATION_STATE, userRegistrationState, userIds -> {
            // Create a counter to track completed user data retrieval
            AtomicInteger remainingCalls = new AtomicInteger(userIds.size());
            for (String userId : userIds) {
                DatabaseManager.getDatabaseManager().getUserDataFromFirestore(userId, DatabaseManager.USER_TYPE, userType -> {
                    User user = User.newUserFromDatabase(userId, userType.toString());
                    if (user == null) {
                        Log.e("UserOptions", "Failed to create user from database, user ID: " + userId);
                        if (remainingCalls.decrementAndGet() == 0) {
                            // Call the callback with the retrieved pending users
                            callback.onDataReceived(pendingUsers);
                        }
                        return;
                    }
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

    /**
     * Retrieves a list of users who have been accepted.
     * <p>
     * This method invokes the {@link #getUsersWithRegistrationStatus(UsersCallback, String)} method
     * with the registration state set to accepted. It passes the provided callback to receive
     * the list of accepted users.
     *
     * @param callback The callback to be invoked with the list of accepted users once the data retrieval is complete.
     */
    public static void getAcceptedUsers(UsersCallback callback) {
        getUsersWithRegistrationStatus(callback, User.ACCEPTED);
    }

    /**
     * Retrieves a list of users who are pending approval (waitlisted).
     * <p>
     * This method invokes the {@link #getUsersWithRegistrationStatus(UsersCallback, String)} method
     * with the registration state set to waitlisted. It passes the provided callback to receive
     * the list of pending users.
     *
     * @param callback The callback to be invoked with the list of pending users once the data retrieval is complete.
     */
    public static void getPendingUsers(UsersCallback callback) {
        getUsersWithRegistrationStatus(callback, User.WAITLISTED);
    }

    /**
     * Retrieves a list of users who have been rejected.
     * <p>
     * This method invokes the {@link #getUsersWithRegistrationStatus(UsersCallback, String)} method
     * with the registration state set to rejected. It passes the provided callback to receive
     * the list of rejected users.
     *
     * @param callback The callback to be invoked with the list of rejected users once the data retrieval is complete.
     */
    public static void getRejectedUsers(UsersCallback callback) {
        getUsersWithRegistrationStatus(callback, User.REJECTED);
    }

}
