package com.example.projectgroup5.database;

import static com.example.projectgroup5.database.DatabaseManager.EVENT_REGISTRATION_STATUS;
import static com.example.projectgroup5.database.DatabaseManager.USER_REGISTRATION_STATE;

import android.util.Log;

import com.example.projectgroup5.MainActivity;
import com.example.projectgroup5.R;
import com.example.projectgroup5.events.Event;
import com.example.projectgroup5.events.Registration;
import com.example.projectgroup5.users.User;
import com.example.projectgroup5.users.UserSession;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DatabaseListener {

    /**
     * A list that holds the active Firestore listener registrations.
     * Each {@link ListenerRegistration} represents a listener that is currently active
     * and monitoring Firestore events. This list is used by the {@link #clearListeners()}
     * method to remove all registered listeners when they are no longer needed.
     */
    private static final List<ListenerRegistration> firestoreListeners = new ArrayList<>();

    /**
     * A map that holds the Firestore event start listener registrations, with the listener's
     * identifier (String) as the key and the corresponding {@link ListenerRegistration} as the value.
     * This map is used to manage and track listeners for specific event start action, allowing
     * efficient retrieval and removal of event start listeners.
     */
    private static final HashMap<String, ListenerRegistration> firestoreEventStartListeners = new HashMap<>();

    /**
     * Clears all active listeners from the Firestore database and removes them.
     * Iterates through each listener in the {@link #firestoreListeners} list, logs the removal,
     * and invokes {@link DatabaseManager#getDatabaseManager()} to remove the listener from Firestore.
     * Additionally, clears the {@link #firestoreListeners} list and calls {@link #clearEventStartListeners()}
     * to clean up event start listeners.
     */
    public static void clearListeners() {
        for (ListenerRegistration listener : firestoreListeners) {
            Log.e("DatabaseListener", "clearing listener " + listener.toString());
            DatabaseManager.getDatabaseManager().removeEventListenerFromFirestore(listener);
        }
        firestoreListeners.clear();
        clearEventStartListeners();
    }

    /**
     * Clears all active Firestore event start listeners and removes them.
     * Converts the {@link #firestoreEventStartListeners} map values to a list,
     * then iterates through each listener to log the removal and invoke
     * {@link DatabaseManager#getDatabaseManager()} to remove the listener from Firestore.
     * Finally, clears the {@link #firestoreEventStartListeners} map to remove all entries.
     */
    public static void clearEventStartListeners() {
        // convert the firestoreEventStartListeners to a list
        List<ListenerRegistration> firestoreEventStartListenersList = new ArrayList<>(firestoreEventStartListeners.values());
        for (ListenerRegistration listener : firestoreEventStartListenersList) {
            Log.e("DatabaseListener", "clearing listener " + listener.toString());
            DatabaseManager.getDatabaseManager().removeEventListenerFromFirestore(listener);
        }
        firestoreEventStartListeners.clear();
    }

    /**
     * Deletes a specific Firestore event start listener identified by the given registration ID.
     * Checks if the {@link #firestoreEventStartListeners} map contains the listener with the specified
     * registration ID. If found, the listener is removed from Firestore using
     * {@link DatabaseManager#getDatabaseManager()} and then removed from the map.
     *
     * @param registrationId The ID of the listener to be deleted.
     */
    public static void deleteEventStartListener(String registrationId) {
        if (firestoreEventStartListeners.containsKey(registrationId)) {
            DatabaseManager.getDatabaseManager().removeEventListenerFromFirestore(firestoreEventStartListeners.get(registrationId));
            firestoreEventStartListeners.remove(registrationId);
        }
    }


    /**
     * Adds a listener to monitor changes in the user registration state in the database.
     * <p>
     * This static method registers an EventListener to check the user registration state.
     * It first checks if the initial value exists and is not equal to 1. If so, it adds another
     * listener that checks for updates to the registration state. If the value updates to 1,
     * a notification is sent to the specified context. Logs are generated for any errors encountered
     * during the database operations.
     *
     * @param context The context in which the notification should be sent.
     */
    public static void addValueAccountCreationEventListener(MainActivity context) {
        final AtomicInteger lastKnownValue = new AtomicInteger(-1);

        // Listener for USER_REGISTRATION_STATE
        EventListener<DocumentSnapshot> registrationStateListener = (dataSnapshot, error) -> {
            if (error != null) {
                Log.e("DatabaseListener", "Listen failed: " + error);
                return;
            }
            // Ensure the document exists
            if (dataSnapshot == null || !dataSnapshot.exists()) return;
            // Get the current value of USER_REGISTRATION_STATE
            String currentValue = dataSnapshot.getString(USER_REGISTRATION_STATE);
            if (currentValue == null) return;
            // If the value has changed, proceed
            if (!mapAtomicIntToRegistration(lastKnownValue).equals(currentValue)) {
                // Send notifications if needed
                if ((currentValue.equals(User.ACCEPTED)) && mapAtomicIntToRegistration(lastKnownValue).equals(User.WAITLISTED)) {
                    Notification.sendMessageNotification(context, "Account Accepted",
                            "Your account has been accepted. Please check your in the application for details.");
                    // set the user representation to accepted
                    UserSession.getInstance().getUserRepresentation().setUserRegistrationState(User.ACCEPTED);
                    context.getNavController().navigate(R.id.account);
                } else if ((currentValue.equals(User.REJECTED)) && mapAtomicIntToRegistration(lastKnownValue).equals(User.WAITLISTED)) {
                    Notification.sendMessageNotification(context, "Account Rejected",
                            "Your account has been rejected. Please check your in the application for details.");
                    // set the user representation to rejected
                    UserSession.getInstance().getUserRepresentation().setUserRegistrationState(User.REJECTED);
                    context.getNavController().navigate(R.id.account);
                } else if ((currentValue.equals(User.ACCEPTED)) && mapAtomicIntToRegistration(lastKnownValue).equals(User.REJECTED)) {
                    Notification.sendMessageNotification(context, "Account Accepted",
                            "Your account has been accepted. Please check your in the application for details.");
                    // set the user representation to accepted
                    UserSession.getInstance().getUserRepresentation().setUserRegistrationState(User.ACCEPTED);
                    context.getNavController().navigate(R.id.account);
                } else {
                    Log.e("DatabaseListener", "Invalid value change: " + currentValue + " LastKnown: " + lastKnownValue.get());
                }
                // Update the last known value
                setRegistrationState(currentValue, lastKnownValue);
            }
        };
        // Add the listener to the specific field
        firestoreListeners.add(DatabaseManager.getDatabaseManager().addValueEventListenerToFirestoreUserData(registrationStateListener, USER_REGISTRATION_STATE));
    }

    /**
     * Adds a listener for monitoring the start of a specific event and sends a notification
     * when the event is within 24 hours of starting for a user with an accepted registration state.
     * This method creates a listener for changes in the `USER_REGISTRATION_STATE` of a specific user,
     * and if the user is accepted and the event start time is within 24 hours, a notification is scheduled
     * to be sent to the user at the 24-hour mark before the event starts.
     * The listener is added to the {@link #firestoreEventStartListeners} map for future reference and management.
     *
     * @param context      The {@link MainActivity} context used to send the notification.
     * @param event        The {@link Event} object containing details about the event (such as start time).
     * @param registration The {@link Registration} object containing the registration details of the user.
     */
    public static void addEventStartListener(MainActivity context, Event event, Registration registration) {

        // Listener for USER_REGISTRATION_STATE
        EventListener<DocumentSnapshot> registrationStateListener = (dataSnapshot, error) -> {
            if (error != null) {
                Log.e("DatabaseListener", "Listen failed: " + error);
                return;
            }
            // Ensure the document exists
            if (dataSnapshot == null || !dataSnapshot.exists()) return;
            // Get the current value of USER_REGISTRATION_STATE
            String currentValue = dataSnapshot.getString(EVENT_REGISTRATION_STATUS);
            if (currentValue == null) return;
            // If the user is accepted and the event starts in more than 24 hours, send a notification at the 24 hour mark
            Date datePlus24Hours = new Date(event.getStartTime().toDate().getTime() + 24 * 60 * 60 * 1000);
//            Log.d("DatabaseListener", "UserSession current value " + currentValue + " event start time: " + event.getStartTime().toDate().getTime() + " datePlus24Hours: " + datePlus24Hours.getTime());
//            Log.d("DatabaseListener", "UserSession dateafter: " + event.getStartTime().toDate().before(datePlus24Hours));
            if (User.ACCEPTED.equals(currentValue) && event.getStartTime().toDate().before(datePlus24Hours)) {
                // Delay until 24 hours before the event start
                Log.d("DatabaseListener", "Event UserSession start notification in : " + (event.getStartTime().toDate().getTime() - new Date().getTime() - 24 * 60 * 60 * 1000) + " milliseconds or " + (event.getStartTime().toDate().getTime() - new Date().getTime() - 24 * 60 * 60 * 1000) / 1000 / 60 + " minutes");
                // TODO save this in a runnable to avoid weird edge cases with double relog or things like that
                new android.os.Handler().postDelayed(() -> {
                    // only trigger if it can be found in the database listener hashmap
                    if (!firestoreEventStartListeners.containsKey(registration.getRegistrationId())) {
                        Log.d("DatabaseListener", "Event start notification not sent could not find listener");
                        return;
                    }
                    Notification.sendMessageNotification(context, "Event starting soon", "Get ready! The event: " + event.getTitle() + " has 24 hours left before it starts!");
                }, (event.getStartTime().toDate().getTime() - new Date().getTime() - 24 * 60 * 60 * 1000));
            }
        };
        // Add the listener to the specific field
        firestoreEventStartListeners.put(registration.getRegistrationId(), DatabaseManager.getDatabaseManager().addValueEventListenerToFirestoreRegistration(registration.getRegistrationId(), registrationStateListener, EVENT_REGISTRATION_STATUS));
    }

    /**
     * Maps an {@link AtomicInteger} value to a corresponding user registration state.
     * The method converts the integer value of the {@link AtomicInteger} into a string representing
     * the registration state, based on predefined states:
     *
     * <ul>
     *     <li>0 -> {@link User#WAITLISTED}</li>
     *     <li>1 -> {@link User#ACCEPTED}</li>
     *     <li>2 -> {@link User#REJECTED}</li>
     * </ul>
     * If the value is not one of these expected values, it returns "Unknown".
     *
     * @param atomicInt The {@link AtomicInteger} representing the registration state value.
     * @return A string representing the registration state, or "Unknown" if the value does not match any known state.
     */
    private static String mapAtomicIntToRegistration(AtomicInteger atomicInt) {
        return switch (atomicInt.get()) {
            case 0 -> User.WAITLISTED;
            case 1 -> User.ACCEPTED;
            case 2 -> User.REJECTED;
            default -> "Unknown";
        };
    }

    /**
     * Sets the registration state in an {@link AtomicInteger} based on a provided registration state string.
     * The method updates the {@link AtomicInteger} to reflect the state based on the following mapping:
     *
     * <ul>
     *     <li>{@link User#WAITLISTED} -> 0</li>
     *     <li>{@link User#ACCEPTED} -> 1</li>
     *     <li>{@link User#REJECTED} -> 2</li>
     * </ul>
     * If the provided registration state is not recognized, the value is set to -1, and a log is generated.
     *
     * @param registrationState The registration state as a string (e.g., "WAITLISTED", "ACCEPTED", "REJECTED").
     * @param lastKnownValue    The {@link AtomicInteger} that holds the current registration state value to be updated.
     */
    private static void setRegistrationState(String registrationState, AtomicInteger lastKnownValue) {
        Log.d("DatabaseListener", "setRegistrationState: " + registrationState + " from: " + mapAtomicIntToRegistration(lastKnownValue));
        switch (registrationState) {
            case User.WAITLISTED:
                lastKnownValue.set(0);
                break;
            case User.ACCEPTED:
                lastKnownValue.set(1);
                break;
            case User.REJECTED:
                lastKnownValue.set(2);
                break;
            default:
                lastKnownValue.set(-1);
                Log.d("DatabaseListener", "Unset registration state: " + registrationState);
                break;
        }
    }
}