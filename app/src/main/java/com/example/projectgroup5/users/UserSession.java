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

import java.util.concurrent.Executor;

public class UserSession {
    private static UserSession instance;
    private String userId;
    private FirebaseAuth firebaseAuth;

    private UserSession() {
        // Initialize Firebase Auth
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
}
