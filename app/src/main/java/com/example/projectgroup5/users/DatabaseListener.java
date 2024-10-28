package com.example.projectgroup5.users;

import static com.example.projectgroup5.users.DatabaseManager.USER_REGISTRATION_STATE;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class DatabaseListener {
    /**
     * Adds a listener to monitor changes in the user registration state in the database.
     * <p>
     * This static method registers a ValueEventListener to check the user registration state.
     * It first checks if the initial value exists and is not equal to 1. If so, it adds another
     * listener that checks for updates to the registration state. If the value updates to 1,
     * a notification is sent to the specified context. Logs are generated for any errors encountered
     * during the database operations.
     *
     * @param context The context in which the notification should be sent.
     */
    public static void addValueAccountCreationEventListener(Context context) {
        DatabaseManager.getDatabaseManager().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Check if the value exists and is not equal 1
                if (!dataSnapshot.exists()) return;
                Integer value = dataSnapshot.getValue(Integer.class);
                if (!(value == null || value != 1)) return;
                DatabaseManager.getDatabaseManager().addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // Check if the value exists and equals 1 (this means it has been updated to 1)
                        if (!dataSnapshot.exists()) return;
                        Integer value = dataSnapshot.getValue(Integer.class);
                        if (value != null && value == 1)
                            Notification.sendNotification(context);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("FirebaseError", databaseError.getMessage());
                    }
                }, USER_REGISTRATION_STATE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseError", databaseError.getMessage());
            }
        }, USER_REGISTRATION_STATE);
    }


}
