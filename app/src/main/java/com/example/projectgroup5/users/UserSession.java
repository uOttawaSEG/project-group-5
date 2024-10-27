package com.example.projectgroup5.users;

import android.util.Log;
import androidx.navigation.NavController;
import com.example.projectgroup5.MainActivity;
import com.example.projectgroup5.R;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.OnCompleteListener;

public class UserSession {
    public static final String USER_TYPE = "UserType";
    public static final String USER_EMAIL = "UserEmail";
    public static final String USER_PHONE = "UserPhone";
    public static final String USER_ADDRESS = "UserAddress";
    public static final String USER_FIRST_NAME = "UserFirstName";
    public static final String USER_LAST_NAME = "UserLastName";
    public static final String USER_REGISTRATION_STATE = "UserRegistrationState";
    public static final int REJECTED = 2;
    public static final int ACCEPTED = 1;
    public static final int WAITLISTED = 0;
    public static final String CHANNEL_ID = "account_creation_channel";
    public static final String USER_ORGANIZATION_NAME = "UserOrganizationName";
    private static UserSession instance;
    private String userId;
    public final static int USER_TYPE_ORGANIZER = 1;
    public final static int USER_TYPE_USER = 2;
    public final static int USER_TYPE_ADMIN = 0;
    private static User userRepresentation;
    private static NavController navController; // TODO make this not static

    private UserSession(NavController navController) {
        UserSession.navController = navController;
        // check if the user is already logged in
        instantiateUserRepresentation();
    }

    public interface FirebaseCallback<T> {
        void onCallback(T value);
    }

    /**
     * This creates a new User and assigns it to userRepresentation
     * It also takes care of the updating the data from firebase
     * For now it only updates the user type and email
     */
    public void instantiateUserRepresentation() {
        FirebaseUser user = DatabaseManager.getDatabaseManager().getCurrentUser();
        if (user == null) {
            Log.e("UserSession", "User is null");
            return;
        }
        userId = user.getUid();
        // update all the data from the database
        DatabaseManager.getDatabaseManager().getUserData(userId, USER_TYPE, userType -> {
            if (userType != null) {
                // Create a User representation based on the user type
                userRepresentation = User.newUser(userId, (int) (long) ((Long) userType));
                if (userRepresentation == null) {
                    Log.e("UserSession", "User representation is null 1");
//                            return;
                }
                instantiateEmailForUser(user);
                navController.navigate(R.id.account_management);
                Log.d("UserSession", "User type: " + userType);
            } else {
                Log.e("UserSession", "User type not found");
            }
        });
    }

    // Login the user using email and password with the DatabaseManager
    public void login(String email, String password, OnCompleteListener<AuthResult> listener) {
        DatabaseManager.getDatabaseManager().login(email, password, listener);
    }

    private void instantiateEmailForUser(FirebaseUser user) {
        //  set the user email
        DatabaseManager.getDatabaseManager().storeValue(USER_EMAIL, user.getEmail(), (task) -> {
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

    // Create a new user with email and password
    public void createUser(String email, String password, OnCompleteListener<AuthResult> listener) {
        DatabaseManager.getDatabaseManager().createUserWithEmailAndPassword(email, password, task -> DatabaseManager.getDatabaseManager().getUserData(USER_TYPE, userType -> {
            if (userType != null) {
                // Create a User representation based on the user type
                userRepresentation = User.newUser(userId, (int) (long) ((Long) userType));
                if (userRepresentation == null) {
                    Log.e("UserSession", "User representation is null 1");
//                            return;
                }
                instantiateEmailForUser(DatabaseManager.getDatabaseManager().getCurrentUser());
                navController.navigate(R.id.account);
                Log.d("UserSession", "User type: " + userType);
            } else {
                Log.e("UserSession", "User type not found");
            }
        }));
    }

    // Logout the current user
    public void logout() {
        DatabaseManager.getDatabaseManager().logout();
        if (userRepresentation != null)
            userRepresentation = null;
        clear(); // Clear user ID
    }

}


