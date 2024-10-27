package com.example.projectgroup5.users;

import static com.example.projectgroup5.users.UserSession.USER_REGISTRATION_STATE;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class DatabaseListener {
    // account status listener, if the account status listener changes from 0 to 1, trigger the code
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
