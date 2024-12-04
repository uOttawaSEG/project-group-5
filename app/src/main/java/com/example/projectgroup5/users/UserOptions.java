package com.example.projectgroup5.users;

import android.util.Log;

import com.example.projectgroup5.database.DatabaseManager;
import com.example.projectgroup5.events.Event;
import com.example.projectgroup5.events.Registration;
import com.google.firebase.firestore.DocumentReference;

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

    public interface RegistrationsCallback {
        /**
         * Called when the data retrieval is complete and the user list is available.
         *
         * @param registrations A list of User registration objects that were retrieved.
         */
        void onDataReceived(List<Registration> registrations);
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
            // If there are no users, callback immediately
            if (userIds.isEmpty()) {
                callback.onDataReceived(pendingUsers);
                return;
            }
            AtomicInteger remainingCalls = new AtomicInteger(userIds.size());
            for (String userId : userIds) {
//                DatabaseManager.getDatabaseManager().getUserDataFromFirestore(userId, DatabaseManager.USER_TYPE, userType -> {
                User.newUserFromDatabase(userId, task -> {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        Log.e("UserOptions", "Failed to create user from database, user ID: " + userId);
                        if (remainingCalls.decrementAndGet() == 0) {
                            // Call the callback with the retrieved pending users
                            callback.onDataReceived(pendingUsers);
                        }
                        return;
                    }
                    pendingUsers.add(task.getResult());
                    // Decrement the counter and check if all callbacks are complete
                    if (remainingCalls.decrementAndGet() == 0) {
                        // Call the callback with the retrieved pending users
                        callback.onDataReceived(pendingUsers);
                    }
                });
            }

        });
    }

    /**
     * Retrieves the registrations with a specified status for a single event.
     * This method acts as a wrapper around the `getRegistrationWithStatusToEvent` method
     * to simplify the process of getting registrations for one event.
     *
     * @param callback The callback to receive the result once the data has been retrieved.
     *                 This callback will be triggered with a list of `Registration` objects
     *                 that match the specified registration status.
     * @param userRegistrationState The registration status to filter the users by.
     *                              It can be one of the predefined states like "accepted", "waitlisted", etc.
     * @param event The event for which the registrations should be retrieved.
     */
    public static void getRegistrationsWithStatusToEvent(RegistrationsCallback callback, String userRegistrationState, Event event) {
        getRegistrationWithStatusToEvent(callback, userRegistrationState, List.of(event));
    }

    /**
     * Retrieves the registrations with a specified status for a list of events.
     * This method fetches all registrations with the specified status for the provided list
     * of events. The results are passed back to the given callback once all data is retrieved.
     *
     * @param callback The callback to receive the result once the data has been retrieved.
     *                 This callback will be triggered with a list of `Registration` objects
     *                 that match the specified registration status.
     * @param userRegistrationState The registration status to filter the users by.
     *                              It can be one of the predefined states like "accepted", "waitlisted", etc.
     * @param events The list of events for which the registrations should be retrieved.
     */
    public static void getRegistrationWithStatusToEvent(RegistrationsCallback callback, String userRegistrationState, List<Event> events) {
        List<Registration> registrations = new ArrayList<>();

        List<DocumentReference> listOfRegistrations = new ArrayList<>();
        for (Event event : events) {
            Log.d("UserOptions", "Event added OrganizerEvent: " + event);
            listOfRegistrations.addAll(event.getRegistrations());
        }
        // If there are no users, callback immediately
        Log.d("UserOptions", "Event added OrganizerEvent list of registrations: " + listOfRegistrations);
        if (listOfRegistrations.isEmpty()) {
            callback.onDataReceived(registrations);
            return;
        }
        // Create a counter to track completed user data retrieval
        AtomicInteger remainingCalls = new AtomicInteger(listOfRegistrations.size());
        Log.d("UserOptions", "Event added OrganizerEvent list of registrations size: " + listOfRegistrations.size());
        for (DocumentReference registrationReference : listOfRegistrations) {
            DatabaseManager.getDatabaseManager().getRegistration(registrationReference.getId(), task -> {
                // now that we have the registration, we can check if it is the correct status
                if (!task.isSuccessful() || task.getResult() == null || !task.getResult().getRegistrationStatus().equals(userRegistrationState)) {
                    Log.e("UserOptions", "Failed to create registration from database, registration ID: " + registrationReference.getId() + " " + task.getResult().getRegistrationStatus());
                } else {
                    Log.d("UserOptions", "Registration added OrganizerEvent: " + task.getResult() + " " + task.getResult().getRegistrationStatus());
                    registrations.add(task.getResult());
                }
                if (remainingCalls.decrementAndGet() == 0) {
                    // Call the callback with the retrieved pending users
                    callback.onDataReceived(registrations);
                }
                });
        }
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
