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
import java.util.Date;
import java.util.HashMap;
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
        clearEventStartListeners();
    }

    // list of event start listeners
    private static final HashMap<String, ListenerRegistration> firestoreEventStartListeners = new HashMap<>();

    public static void clearEventStartListeners() {
        // convert the firestoreEventStartListeners to a list
        List<ListenerRegistration> firestoreEventStartListenersList = new ArrayList<>(firestoreEventStartListeners.values());
        for (ListenerRegistration listener : firestoreEventStartListenersList) {
            Log.e("DatabaseListener", "clearing listener " + listener.toString());
            DatabaseManager.getDatabaseManager().removeEventListenerFromFirestore(listener);
        }
        firestoreEventStartListeners.clear();
    }

    // try to delete a listener to a specific event
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
            // If the user is accepted and the event starts in more than 24 hours, send a notification at the 24 hour mark
            Date datePlus24Hours = new Date(event.getStartTime().toDate().getTime() + 24 * 60 * 60 * 1000);
            Log.d("DatabaseListener", "UserSession current value " + currentValue + " event start time: " + event.getStartTime().toDate().getTime() + " datePlus24Hours: " + datePlus24Hours.getTime());
            Log.d("DatabaseListener", "UserSession dateafter: " + event.getStartTime().toDate().before(datePlus24Hours));
            if (User.ACCEPTED.equals(currentValue) && event.getStartTime().toDate().before(datePlus24Hours)) {
                // Delay until 24 hours before the event start
                Log.d("DatabaseListener", "Event UserSession start notification in : " + (event.getStartTime().toDate().getTime() - new Date().getTime()  - 24 * 60 * 60 * 1000) + " milliseconds or " + (event.getStartTime().toDate().getTime() - new Date().getTime() - 24 * 60 * 60 * 1000) / 1000 / 60  + " minutes");
                // TODO save this in a runnable to avoid weird edge cases with double relog or things like that
                new android.os.Handler().postDelayed(() -> {
                    // only trigger if it can be found in the database listener hashmap
                    if (!firestoreEventStartListeners.containsKey(registration.getRegistrationId())) {
                        Log.d("DatabaseListener", "Event start notification not sent could not find listener");
                        return;}
                    Notification.sendEventNotification(context, event);
//                    context.getNavController().navigate(R.id.search_event_dashboard);
                }, (event.getStartTime().toDate().getTime() - new Date().getTime()  - 24 * 60 * 60 * 1000));
            }
        };
        // Add the listener to the specific field
        firestoreEventStartListeners.put(registration.getRegistrationId(), DatabaseManager.getDatabaseManager().addValueEventListenerToFirestoreRegistration(registration.getRegistrationId(), registrationStateListener, EVENT_REGISTRATION_STATUS));
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