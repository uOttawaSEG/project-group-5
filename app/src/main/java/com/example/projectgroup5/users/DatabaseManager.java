package com.example.projectgroup5.users;

import static com.example.projectgroup5.users.UserSession.USER_TYPE;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.projectgroup5.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseManager {

    private static final DatabaseManager databaseManager = new DatabaseManager();

    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();

    public static DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    // make it impossible to create more than one instance of the database manager
    private DatabaseManager() {
    }

    // Login the user using email and password with Firebase
    public void login(String email, String password, OnCompleteListener<AuthResult> listener) {
        if (firebaseAuth.getCurrentUser() != null) {
            firebaseAuth.signOut();
        }
        if (email == null || password == null || email.isEmpty() || password.isEmpty()) {
            Log.e("UserSession", "Email or password is empty");
            // make a call back to the listener with a no success
            listener.onComplete(Tasks.forException(new Exception("Email or password is empty")));
            return;
        }
        Log.d("UserSession", "Login: " + email + " " + password);


        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(listener).addOnSuccessListener(task -> UserSession.getInstance().instantiateUserRepresentation());
    }

    public void logout() {
        firebaseAuth.signOut();
    }


    // get current user
    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }


    public void createUserWithEmailAndPassword(String email, String password, OnCompleteListener<AuthResult> listener) {
        if (firebaseAuth.getCurrentUser() != null) {
            firebaseAuth.signOut();
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(listener).addOnSuccessListener(task -> UserSession.getInstance().instantiateUserRepresentation());
        }
    }

    // Delete the current user
    public void deleteUser(OnCompleteListener<Void> listener) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            user.delete().addOnCompleteListener(listener);
        } else {
            // Handle case where there is no current user
            listener.onComplete(Tasks.forException(new Exception("No user logged in")));
        }
    }


    /**
     * @param key
     * @param callback
     * The allowed return types for the data are as follows:
     * <ul>
     *   <li><code>Boolean</code></li>
     *   <li><code>String</code></li>
     *   <li><code>Long</code></li>
     *   <li><code>Double</code></li>
     *   <li><code>Map&lt;String, Object&gt;</code></li>
     *   <li><code>List&lt;Object&gt;</code></li>
     * </ul>
     */
    public void getUserData(String userId, String key, final UserSession.FirebaseCallback<Object> callback) {
        if (userId == null) {
            Log.e("UserSession", "User ID is null");
            callback.onCallback(null);
            return;
        }
        DatabaseReference ref = database.getReference("users").child(userId).child(key);

        Log.d("UserSession", "Fetching user data for key: " + key); // Add this line

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Object data = snapshot.getValue();
                    Log.d("UserSession", "Data retrieved: " + data); // Add this line
                    callback.onCallback(data);
                } else {
                    Log.e("UserSession", "Snapshot does not exist for key: " + key); // Add this line
                    callback.onCallback(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", error.getMessage());
                callback.onCallback(null);
            }
        });
    }

    public void getAllUserData(String userId, final UserSession.FirebaseCallback<Map<String, Object>> callback) {
        if (userId == null) {
            Log.e("UserSession", "User ID is null");
            callback.onCallback(null);
            return;
        }
        DatabaseReference ref = database.getReference("users").child(userId);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Object data = snapshot.getValue();
                    Log.d("UserSession", "Data retrieved: " + data); // Add this line
                    callback.onCallback((Map<String, Object>) data);
                } else {
                    callback.onCallback(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", error.getMessage());
                callback.onCallback(null);
            }
        });
    }

    public void getUserData(String key, final UserSession.FirebaseCallback<Object> callback) {
        getUserData(UserSession.getInstance().getUserId(), key, callback);
    }

    public interface DataCallback {
        void onDataReceived(List<String> userIds);
    }

    public void getUserIdByMatchingData(String entry, String pattern, DataCallback callback) {
        DatabaseReference ref = database.getReference("users");
        List<String> userIds = new ArrayList<>();

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Object data = snapshot.getValue();
                    if (data != null) {
                        Map<String, Object> userIdsMap = (Map<String, Object>) data;

                        for (Map.Entry<String, Object> userEntry : userIdsMap.entrySet()) {
                            if (userEntry.getValue() == null) {
                                continue;
                            }

                            Map<String, Object> userMap = (Map<String, Object>) userEntry.getValue();
                            if (!userMap.containsKey(entry)) {
                                continue;
                            }

                            Object userEntryValue = userMap.get(entry);
                            if (userEntryValue == null) {
                                continue;
                            }

                            if (!userEntryValue.toString().equals(pattern)) {
                                continue;
                            }

                            userIds.add(userEntry.getKey());
                        }

                        Log.d("DatabaseManager", "Done retrieving data"); // Log completion
                    }
                }
                // Trigger the callback with the retrieved user IDs
                callback.onDataReceived(userIds);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", error.getMessage());
                // Optionally, you could notify about the error here too
                callback.onDataReceived(Collections.emptyList()); // Return an empty list on error
            }
        });
    }


    public void storeValue(String userId, String type, @Nullable Object value, OnCompleteListener<Void> listener) {
        DatabaseReference ref = database.getReference().child("users").child(userId).child(type);
        ref.setValue(value).addOnCompleteListener(listener);
    }

    public void storeValue(String type, @Nullable Object value, OnCompleteListener<Void> listener) {
        storeValue(UserSession.getInstance().getUserId(), type, value, listener);
    }
}
