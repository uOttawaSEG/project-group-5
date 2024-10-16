package com.example.projectgroup5.users;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;

import com.example.projectgroup5.MainActivity;
import com.example.projectgroup5.R;
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
import com.google.firebase.database.annotations.Nullable;

public class UserSession {
    public static final String USER_TYPE = "UserType";
    public static final String USER_UID = "UserUID";
    public static final String USER_EMAIL = "UserEmail";
    public static final String USER_PHONE = "UserPhone";
    public static final String USER_ADDRESS = "UserAddress";
    public static final String USER_FIRST_NAME = "UserFirstName";
    public static final String USER_LAST_NAME = "UserLastName";
    private static UserSession instance;
    private String userId;
    private final FirebaseAuth firebaseAuth;
    public final static int USER_TYPE_ORGANIZER = 1;
    public final static int USER_TYPE_USER = 2;
    public final static int USER_TYPE_ADMIN = 0;
    private static FirebaseDatabase database;
    private static User userRepresentation;
    private static NavController navController;

    private UserSession(NavController navController) {
        // Initialize Firebase Auth
        database = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        this.navController = navController;
        // must setup the configuration of the firebase
        // check if the user is already logged in
        instantiateUserRepresentation();
    }

    /**
     * This creates a new User and assigns it to userRepresentation
     * It also takes care of the updating the data from firebase
     * For now it only updates the user type and email
     */
    public void instantiateUserRepresentation() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            Log.e("UserSession", "User is null");
            return;
        }
            userId = user.getUid();
            Log.d("UserSession", "UserSession: " + userId);
            Log.d("UserSession", "UserSession: " + firebaseAuth.getCurrentUser());
            // update all the data from the database
            getUserData(USER_TYPE, new FirebaseCallback<Object>() {
                @Override
                public void onCallback(Object userType) {
                    if (userType != null) {
                        // Create a User representation based on the user type
                        userRepresentation = User.newUser(userId, (int)(long)((Long) userType));
                        if (userRepresentation == null) {
                            Log.e("UserSession", "User representation is null 1");
//                            return;
                        }
                        instantiateEmailForUser(user);
                        navController.navigate(R.id.account);
                        Log.d("UserSession", "User type: " + userType);
                    } else {
                        Log.e("UserSession", "User type not found");
                    }
                }
            });
    }

    private void instantiateEmailForUser(FirebaseUser user) {
        //  set the user email
        storeValue(USER_EMAIL, user.getEmail(), (task) -> {
            if (task.isSuccessful()) {
                Log.d("UserSession", "Success instantiateEmailForUser: success");
            } else {
                Log.d("UserSession", "storeUserEmailError: " + task.getException());
            }
        });
        if (userRepresentation != null) {
            Log.d("UserSession", "Set the user email in the user representation to: " + user.getEmail());
            userRepresentation.setUserEmail(user.getEmail());
        } else {
            Log.e("UserSession", "User representation is null");
        }
    }

    public User getUserRepresentation() {
        return userRepresentation;
    }

    public static void initialize(MainActivity activity, NavController navController) {
        if (instance == null) {
            instance = new UserSession(navController);
            Log.d("UserSession", "initialize: " + instance);
            FirebaseApp.initializeApp(activity);
        }
    }

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession(navController);
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
                .addOnCompleteListener(listener).addOnSuccessListener(task -> instantiateUserRepresentation());
    }

    // Create a new user with email and password
    public void createUser(String email, String password, OnCompleteListener<AuthResult> listener) {
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(listener).addOnSuccessListener(task -> {getUserData(USER_TYPE, new FirebaseCallback<Object>() {
            @Override
            public void onCallback(Object userType) {
                if (userType != null) {
                    // Create a User representation based on the user type
                    userRepresentation = User.newUser(userId, (int)(long)((Long) userType));
                    if (userRepresentation == null) {
                        Log.e("UserSession", "User representation is null 1");
//                            return;
                    }
                    instantiateEmailForUser(firebaseAuth.getCurrentUser());
                    navController.navigate(R.id.account);
                    Log.d("UserSession", "User type: " + userType);
                } else {
                    Log.e("UserSession", "User type not found");
                }
            }
        });});


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
        userRepresentation = null;
        clear(); // Clear user ID
    }

    // Check if the user is currently logged in
    public boolean isLoggedIn() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        return user != null;
    }

    public void setUserType(int userType) {
        this.userRepresentation.setUserType(userType);
    }

    public interface FirebaseCallback<T> {
        void onCallback(T value);
    }

    public void storeValue(String type, @Nullable Object value, OnCompleteListener<Void> listener) {
        DatabaseReference ref = database.getReference().child("users").child(userId).child(type);
        ref.setValue(value).addOnCompleteListener(listener);
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
    public void getUserData(String key, final FirebaseCallback<Object> callback) {
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

}


