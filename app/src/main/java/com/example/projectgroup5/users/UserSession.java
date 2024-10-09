package com.example.projectgroup5.users;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.projectgroup5.MainActivity;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.Executor;

public class UserSession {
    private static final String USER_TYPE = "UserType";
    private static UserSession instance;
    private String userId;
    private FirebaseAuth firebaseAuth;
    public final static int USER_TYPE_ORGANIZER = 1;
    public final static int USER_TYPE_USER = 2;
    public final static int USER_TYPE_ADMIN = 0;
    private static FirebaseDatabase database;

    private UserSession() {
        // Initialize Firebase Auth
        database = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        // must setup the configuration of the firebase
        // check if the user is already logged in
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            userId = user.getUid();
            Log.d("UserSession", "UserSession: " + userId);
            Log.d("UserSession", "UserSession: " + firebaseAuth.getCurrentUser());
        }
    }

    public static void initialize(MainActivity activity) {
        if (instance == null) {
            instance = new UserSession();
            Log.d("UserSession", "initialize: " + instance);
            FirebaseApp.initializeApp(activity);
        }
    }

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void clear() {
        userId = null;
    }

    // Login the user using email and password with Firebase
    public void login(String email, String password, OnCompleteListener<AuthResult> listener) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(listener);
    }

    // Create a new user with email and password
    public void createUser(String email, String password, OnCompleteListener<AuthResult> listener) {
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(listener);
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

    // Logout the current user
    public void logout() {
        firebaseAuth.signOut();
        clear(); // Clear user ID
    }

    // Check if the user is currently logged in
    public boolean isLoggedIn() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        return user != null;
    }

    public interface FirebaseCallback {
        void onCallback(String value);
    }

    public void storeData(String type, String data, OnCompleteListener<Void> listener) {
        DatabaseReference ref = database.getReference().child("users").child(userId).child(type);
        ref.setValue(data).addOnCompleteListener(listener);
    }

    // Use a callback to get the value corresponding to the key asynchronously
//    public void getUserType(final FirebaseCallback callback) {
//        DatabaseReference ref = database.getReference(userId);
//        ref.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (snapshot.exists()) {
//                    // Get the value as a string
//                    String data = snapshot.getValue(String.class);
//                    callback.onCallback(data);  // Return the data via callback
//                } else {
//                    callback.onCallback("Unknown");  // Handle case where the key doesn't exist
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                callback.onCallback("Error");  // Handle the error case
//            }
//        });
//    }

    public int getUserType() {
        final int[] userType = {USER_TYPE_USER};
        database.getReference().child("users").child(userId).child(USER_TYPE).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else {
                    Log.d("firebase", String.valueOf(task.getResult().getValue()));
                    userType[0] =Integer.parseInt((String) task.getResult().getValue());
                }
            }
        });
        return userType[0];
    }

}


