package com.example.projectgroup5.database;

import static com.example.projectgroup5.database.DatabaseManager.EVENT_REGISTRATION_STATUS;
import static com.example.projectgroup5.database.DatabaseManager.REGISTRATION_EVENT;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DatabaseListener {

    // list of listeners
    private static final List<ListenerRegistration> firestoreListeners = new ArrayList<>();

    public static void clearListeners() {
        for (ListenerRegistration listener : firestoreListeners) {
            Log.e("DatabaseListener", "clearing listener " + listener.toString());
            DatabaseManager.getDatabaseManager().removeEventListenerFromFirestore(listener);
        }
        firestoreListeners.clear();
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
//                Log.d("DatabaseListener", "currentValue: " + currentValue);
            // If the value has changed, proceed
            if (!mapAtomicIntToRegistration(lastKnownValue).equals(currentValue)) {
                // Send notifications if needed
                if ((currentValue.equals(User.ACCEPTED)) && mapAtomicIntToRegistration(lastKnownValue).equals(User.WAITLISTED)) {
                    Notification.sendAcceptedNotification(context);
                    // set the user representation to accepted
                    UserSession.getInstance().getUserRepresentation().setUserRegistrationState(User.ACCEPTED);
                    context.getNavController().navigate(R.id.account);
                } else if ((currentValue.equals(User.REJECTED)) && mapAtomicIntToRegistration(lastKnownValue).equals(User.WAITLISTED)) {
                    Notification.sendRejectedNotification(context);
                    // set the user representation to rejected
                    UserSession.getInstance().getUserRepresentation().setUserRegistrationState(User.REJECTED);
                    context.getNavController().navigate(R.id.account);
                } else if ((currentValue.equals(User.ACCEPTED)) && mapAtomicIntToRegistration(lastKnownValue).equals(User.REJECTED)) {
                    Notification.sendAcceptedNotification(context);
                    // set the user representation to accepted
                    UserSession.getInstance().getUserRepresentation().setUserRegistrationState(User.ACCEPTED);
                    context.getNavController().navigate(R.id.account);
                } else {
                    Log.e("DatabaseListener", "Invalid value change: " + currentValue + " LastKnown: " + lastKnownValue.get());
                }
                Log.d("DatabaseListener", "Values: " + currentValue + " LastKnown: " + lastKnownValue.get());
                // Update the last known value
                setRegistrationState(currentValue, lastKnownValue);
            }
        };
        // Add the listener to the specific field
        firestoreListeners.add(DatabaseManager.getDatabaseManager().addValueEventListenerToFirestoreUserData(registrationStateListener, USER_REGISTRATION_STATE));
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
//                Log.d("DatabaseListener", "currentValue: " + currentValue);
            // If the value has changed, proceed
            if (User.ACCEPTED.equals(currentValue)) {
                // Send notifications if event starts in less than 24 hours
                if (event.getStartTime().isBefore(LocalDateTime.now().plusHours(24))) {
                    Notification.sendAcceptedNotification(context);
                    // set the user representation to accepted
                    UserSession.getInstance().getUserRepresentation().setUserRegistrationState(User.ACCEPTED);
                    context.getNavController().navigate(R.id.search_event_dashboard);
                }
                Log.d("DatabaseListener", "Values: " + currentValue + " LastKnown: " + lastKnownValue.get());
                // Update the last known value
            }
        };
        // Add the listener to the specific field
        firestoreListeners.add(DatabaseManager.getDatabaseManager().addValueEventListenerToFirestoreRegistration(registration.getRegistrationId(), registrationStateListener, EVENT_REGISTRATION_STATUS));
    }

    // map the atomicInt to a registration state
    private static String mapAtomicIntToRegistration(AtomicInteger atomicInt) {
        return switch (atomicInt.get()) {
            case 0 -> User.WAITLISTED;
            case 1 -> User.ACCEPTED;
            case 2 -> User.REJECTED;
            default -> "Unknown";
        };
    }

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