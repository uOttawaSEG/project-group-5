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

    public static void getRegistrationsWithStatusToEvent(RegistrationsCallback callback, String userRegistrationState, Event event) {
        getRegistrationWithStatusToEvent(callback, userRegistrationState, List.of(event));
    }

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
                // Decrement the counter and check if all callbacks are complete
//                    Registration registration = task.getResult();
//                    Log.e("UserOptions", "Failed to create registration from database, registration ID: " + registrationReference.getId());
//                    if (registration.getRegistrationStatus().equals(userRegistrationState)) {
//                        DatabaseManager.getDatabaseManager().getRegistration(registrationReference.getId(), task2 -> {
//                            if (!task2.isSuccessful() || task2.getResult() == null) {
//                                Log.e("UserOptions", "Failed to create registration from database, registration ID: " + registrationReference.getId());
//                                if (remainingCalls.decrementAndGet() == 0) {
//                                    // Call the callback with the retrieved pending users
//                                    Log.d("UserOptions", "Registration added OrganizerEvent0: " + registration);
//                                    callback.onDataReceived(registrations);
//                                }
//                                return;
//                            }
//                            registrations.add(task2.getResult());
//                            // Decrement the counter and check if all callbacks are complete
//                            if (remainingCalls.decrementAndGet() == 0) {
//                                // Call the callback with the retrieved pending users
//                                Log.d("UserOptions", "Registration added OrganizerEvent1: " + registration);
//                                callback.onDataReceived(registrations);
//                            }
//                        });
//                    } else if (remainingCalls.decrementAndGet() == 0) {
//                        // Call the callback with the retrieved pending users
//                        Log.d("UserOptions", "Registration added OrganizerEvent3: " + registration);
//                        callback.onDataReceived(registrations);
//                    }
                });
        }
    }


    /*
    List<Event> events = new ArrayList<>();
        DatabaseManager databaseManager = DatabaseManager.getDatabaseManager();
        databaseManager.getOrganizerEvents(UserSession.getInstance().getUserId(), task -> {
            if (task == null || !task.isSuccessful()) {
                Log.e("EventOptions", "Failed to get organizer events");
                callback.onDataReceived(events);
                return;
            } else {
                List<DocumentReference> eventIds = task.getResult();
                // Create a counter to track completed event data retrieval
                AtomicInteger remainingCalls = new AtomicInteger(eventIds.size());
                Log.d("EventOptions", "Got " + eventIds.size() + " events");
                for (DocumentReference eventId : eventIds) {
                    DatabaseManager.getDatabaseManager().getEvent(eventId.getId(), task2 -> {
                        if (task2.getResult() == null || !task2.isSuccessful()) {
                            Log.e("EventOptions", "Failed to create event from database, event ID: " + eventId);
                            if (remainingCalls.decrementAndGet() == 0) {
                                // Call the callback with the retrieved pending events
                                callback.onDataReceived(events);
                            }
                            return;
                        }
                        if (task2.getResult().holdsAnEvent()) {
                            Event event = task2.getResult().getEvent();
                            // check if the event is in the correct time status
                            if (event.getTimeStatus().equals(eventTimeStatus)) {
                                events.add(event);
                            }
                        }
                        // Decrement the counter and check if all callbacks are complete
                        if (remainingCalls.decrementAndGet() == 0) {
                            // Call the callback with the retrieved pending events
                            callback.onDataReceived(events);
                        }
                    });
                }
                // If there are no events, callback immediately
                if (eventIds.isEmpty()) {
                    callback.onDataReceived(events);
                }
            }
        });
    * */


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
