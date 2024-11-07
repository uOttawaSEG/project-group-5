package com.example.projectgroup5.database;

import static com.example.projectgroup5.database.DatabaseManager.USER_REGISTRATION_STATE;

import android.util.Log;

import androidx.annotation.Nullable;

import com.example.projectgroup5.MainActivity;
import com.example.projectgroup5.R;
import com.example.projectgroup5.users.User;
import com.example.projectgroup5.users.UserSession;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DatabaseListener {

    private static DatabaseListener instance;

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
        EventListener<DocumentSnapshot> registrationStateListener = new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot dataSnapshot, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("DatabaseListener", "Listen failed: " + error);
                    return;
                }
                // Ensure the document exists
                if (dataSnapshot == null || !dataSnapshot.exists()) return;
                // Get the current value of USER_REGISTRATION_STATE
                Integer currentValue = dataSnapshot.getLong(USER_REGISTRATION_STATE).intValue();
                if (currentValue == null) return;

                // If the value has changed, proceed
                if (lastKnownValue.get() != currentValue) {
                    // Send notifications if needed
                    if (currentValue == 1 && lastKnownValue.get() == 0) {
                        Notification.sendAcceptedNotification(context);
                        // set the user representation to accepted
                        UserSession.getInstance().getUserRepresentation().setUserRegistrationState(User.ACCEPTED);
                        context.getNavController().navigate(R.id.account);
                    } else if (currentValue == 2 && lastKnownValue.get() == 0) {
                        Notification.sendRejectedNotification(context);
                        // set the user representation to rejected
                        UserSession.getInstance().getUserRepresentation().setUserRegistrationState(User.REJECTED);
                        context.getNavController().navigate(R.id.account);
                    } else if (currentValue == 1 && lastKnownValue.get() == 2) {
                        Notification.sendAcceptedNotification(context);
                        // set the user representation to accepted
                        UserSession.getInstance().getUserRepresentation().setUserRegistrationState(User.ACCEPTED);
                        context.getNavController().navigate(R.id.account);
                    } else {
                        Log.e("DatabaseListener", "Invalid value: " + currentValue + " LastKnown: " + lastKnownValue.get());
                    }
                    Log.d("DatabaseListener", "Values: " + currentValue + " LastKnown: " + lastKnownValue.get());
                    // Update the last known value
                    lastKnownValue.set(currentValue);
                }
            }
        };
        // Add the listener to the specific field
        firestoreListeners.add(DatabaseManager.getDatabaseManager().addValueEventListenerToFirestore(registrationStateListener, USER_REGISTRATION_STATE));
    }
}