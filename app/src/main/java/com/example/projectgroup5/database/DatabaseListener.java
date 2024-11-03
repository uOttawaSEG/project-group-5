package com.example.projectgroup5.database;

import static com.example.projectgroup5.database.DatabaseManager.USER_REGISTRATION_STATE;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;

import com.example.projectgroup5.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class DatabaseListener {

    private static DatabaseListener instance;

    // list of listeners
    private static final List<ValueEventListener> realtimeListeners = new ArrayList<>();
    private static final List<ListenerRegistration> firestoreListeners = new ArrayList<>();

    public static void clearListeners() {
        for (ValueEventListener listener : realtimeListeners) {
            Log.e("DatabaseListener", "clearing listener " + listener.toString());
            DatabaseManager.getDatabaseManager().removeEventListenerFromRealTime(listener);
        }
        for (ListenerRegistration listener : firestoreListeners) {
            Log.e("DatabaseListener", "clearing listener " + listener.toString());
            DatabaseManager.getDatabaseManager().removeEventListenerFromFirestore(listener);
        }
        realtimeListeners.clear();
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
    public static void addValueAccountCreationEventListener(Context context, NavController navController) {
        EventListener<DocumentSnapshot> firstListener = new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot dataSnapshot, @Nullable FirebaseFirestoreException error0) {
                // Check if the value exists and is not equal 1
                if (!dataSnapshot.exists()) return;
//                Integer value = dataSnapshot.getValue(Integer.class);
                Integer value = dataSnapshot.getLong(USER_REGISTRATION_STATE).intValue();
                EventListener<DocumentSnapshot> secondListener;
                if (value == null || value == 1)
                    return;
                else if (value == 0) {
                    secondListener = (dataSnapshot1, error1) -> {
// Check if the value exists and equals 1 (this means it has been updated to 1)
                        Log.e("DatabaseListener", "secondListener onDataChange from 0");
                        if (!dataSnapshot1.exists()) return;
//                            Integer value = dataSnapshot.getValue(Integer.class);
                        Integer value1 = dataSnapshot1.getLong(USER_REGISTRATION_STATE).intValue();
                        if (value1 != null && value1 == 1)
                            Notification.sendAcceptedNotification(context);
                        if (value1 != null && value1 == 2)
                            Notification.sendRejectedNotification(context);
                        // DO NOT TOUCH THIS!!!!!!!!!!!!!
                        if (value1 != null && (value1 == 2 || value1 == 1))
                            navController.navigate(R.id.account);
                    };
                } else if (value == 2) {
                    secondListener = (dataSnapshot2, error2) -> {
                        Log.e("DatabaseListener", "secondListener onDataChange from 2");
                        // Check if the value exists and equals 1 (this means it has been updated to 1)
                        if (!dataSnapshot2.exists()) return;
//                            Integer value = dataSnapshot.getValue(Integer.class);
                        Integer value1 = dataSnapshot2.getLong(USER_REGISTRATION_STATE).intValue();
                        // DO NOT TOUCH THIS!!!!!!!!!!!!!!!!!
                        if (value != null && value == 1) {
                            Notification.sendAcceptedNotification(context);
                            navController.navigate(R.id.account);
                        } else if (value != null && value == 0) {
                            navController.navigate(R.id.account);
                        }
                    };
                } else {
                    Log.e("FirebaseError", "Invalid value: " + value);
                    return;
                }

                Log.e("DatabaseListener", "adding second listener " + secondListener.toString());
                firestoreListeners.add(DatabaseManager.getDatabaseManager().addValueEventListenerToFirestore(secondListener, USER_REGISTRATION_STATE));
            }

        };

        Log.e("DatabaseListener", "adding first listener " + firstListener.toString());
        firestoreListeners.add(DatabaseManager.getDatabaseManager().addValueEventListenerToFirestore(firstListener, USER_REGISTRATION_STATE));
    }

}


/*


     * Adds a listener to monitor changes in the user registration state in the database.
     * <p>
     * This static method registers an EventListener to check the user registration state.
     * It first checks if the initial value exists and is not equal to 1. If so, it adds another
     * listener that checks for updates to the registration state. If the value updates to 1,
     * a notification is sent to the specified context. Logs are generated for any errors encountered
     * during the database operations.
     *
     * @param context The context in which the notification should be sent.

public static void addValueAccountCreationEventListener(Context context, NavController navController) {
    ValueEventListener firstListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            // Check if the value exists and is not equal 1
            if (!dataSnapshot.exists()) return;
            Integer value = dataSnapshot.getValue(Integer.class);
            ValueEventListener secondListener;
            if (value == null || value == 1)
                return;
            else if (value == 0) {
                secondListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // Check if the value exists and equals 1 (this means it has been updated to 1)
                        Log.e("DatabaseListener", "secondListener onDataChange from 0");
                        if (!dataSnapshot.exists()) return;
                        Integer value = dataSnapshot.getValue(Integer.class);
                        if (value != null && value == 1)
                            Notification.sendAcceptedNotification(context);
                        if (value != null && value == 2)
                            Notification.sendRejectedNotification(context);
                        // DO NOT TOUCH THIS!!!!!!!!!!!!!
                        if (value != null && (value == 2 || value == 1))
                            navController.navigate(R.id.account);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("FirebaseError", databaseError.getMessage());
                    }
                };
            } else if (value == 2) {
                secondListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.e("DatabaseListener", "secondListener onDataChange from 2");
                        // Check if the value exists and equals 1 (this means it has been updated to 1)
                        if (!dataSnapshot.exists()) return;
                        Integer value = dataSnapshot.getValue(Integer.class);
                        // DO NOT TOUCH THIS!!!!!!!!!!!!!!!!!
                        if (value != null && value == 1) {
                            Notification.sendAcceptedNotification(context);
                            navController.navigate(R.id.account);
                        } else if (value != null && value == 0) {
                            navController.navigate(R.id.account);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("FirebaseError", databaseError.getMessage());
                    }
                };
            } else {
                Log.e("FirebaseError", "Invalid value: " + value);
                return;
            }

            Log.e("DatabaseListener", "adding second listener " + secondListener.toString());
            realtimeListeners.add(secondListener);
            DatabaseManager.getDatabaseManager().addValueEventListenerToFirestore(secondListener, USER_REGISTRATION_STATE);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.e("FirebaseError", databaseError.getMessage());
        }
    };

    Log.e("DatabaseListener", "adding first listener " + firstListener.toString());
    realtimeListeners.add(firstListener);
    DatabaseManager.getDatabaseManager().addValueEventListenerToFirestore(firstListener, USER_REGISTRATION_STATE);
}

 */