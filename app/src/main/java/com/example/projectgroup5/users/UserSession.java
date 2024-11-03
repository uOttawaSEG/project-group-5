package com.example.projectgroup5.users;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import androidx.navigation.NavController;

import com.example.projectgroup5.MainActivity;
import com.example.projectgroup5.R;
import com.example.projectgroup5.database.DatabaseListener;
import com.example.projectgroup5.database.DatabaseManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;

public class UserSession {
    private static UserSession instance;
    private static Context context;
    private String userId;

    private static User userRepresentation;
    @SuppressLint("StaticFieldLeak")
    private static NavController navController;

    /**
     * Constructs a new instance of the UserSession class and initializes it with a navigation controller.
     * <p>
     * This constructor sets the provided {@link NavController} for the session and checks if
     * a user is already logged in by invoking {@link #instantiateUserRepresentation(Context)}.
     *
     * @param navController The navigation controller used for navigating between fragments or activities.
     *                      It must not be null.
     */
    private UserSession(NavController navController) {
        UserSession.navController = navController;
        // check if the user is already logged in
        instantiateUserRepresentation(context);
    }

    public interface FirebaseCallback<T> {
        void onCallback(T value);
    }

    /**
     * Instantiates the user representation based on the current logged-in user.
     * <p>
     * This method retrieves the current user from the database and updates the user ID.
     * It fetches the user's type from the database and creates a corresponding user representation
     * using the {@link User#newUser(String, int)} method. If the user type is not found,
     * an error is logged. Additionally, it navigates to the account management screen
     * after successfully instantiating the user representation.
     * <p>
     * If no user is currently logged in, an error message is logged.
     */
    public void instantiateUserRepresentation(Context context) {
        UserSession.context = context;
        FirebaseUser user = DatabaseManager.getDatabaseManager().getCurrentUser();
        if (user == null) {
            Log.e("UserSession", "User is null");
            return;
        }
        userId = user.getUid();
        // update all the data from the database
        DatabaseManager.getDatabaseManager().getUserData(userId, DatabaseManager.USER_TYPE, userType -> {
            if (userType != null) {
                // Create a User representation based on the user type
                if (userRepresentation != null) {
                    Log.e("UserSession", "User representation is not initially null");
                            return;
                }
                userRepresentation = User.newUser(userId, (int) (long) ((Long) userType));
                instantiateEmailForUser(user);
                // Notification for prelogged in user
                DatabaseListener.clearListeners();
                Log.d("DatabaseListener", "User type: " + userType);
                DatabaseListener.addValueAccountCreationEventListener(context, navController);
                //This was not working for some reason after 2 accounts logged in sequentially
//                navController.navigate(R.id.account_management);
                navController.navigate(R.id.account);
                Log.d("UserSession", "User type: " + userType);
            } else {
                Log.e("UserSession", "User type not found");
            }
        });
    }

    /**
     * Initiates the login process for a user with the specified email and password.
     * <p>
     * This method delegates the login operation to the {@link DatabaseManager} class,
     * allowing the user to authenticate. The provided listener will receive the result
     * of the login attempt.
     *
     * @param email    The email address of the user attempting to log in. Must not be null or empty.
     * @param password The password associated with the user's account. Must not be null or empty.
     * @param listener The listener to be notified of the login operation's completion,
     *                 containing the result of the authentication attempt.
     */
    public void login(String email, String password, OnCompleteListener<AuthResult> listener) {
        DatabaseManager.getDatabaseManager().login(email, password, context, listener);
        DatabaseListener.clearListeners();
//        DatabaseListener.addValueAccountCreationEventListener(context, navController);
    }

    /**
     * Instantiates and stores the email for the specified user in the user representation and database.
     * <p>
     * This method retrieves the email of the provided {@link FirebaseUser} and stores it in the database.
     * It also updates the email in the user representation if it exists.
     *
     * @param user The {@link FirebaseUser} whose email is to be instantiated.
     *             Must not be null.
     */
    private void instantiateEmailForUser(FirebaseUser user) {
        //  set the user email
        DatabaseManager.getDatabaseManager().storeUserValue(DatabaseManager.USER_EMAIL, user.getEmail(), (task) -> {
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

    /**
     * Retrieves the user representation associated with the current user session.
     *
     * @return The {@link User} object representing the current user, or null if no user is represented.
     */
    public User getUserRepresentation() {
        return userRepresentation;
    }


    /**
     * Initializes the {@link UserSession} instance with the provided activity and navigation controller.
     * This method sets up the Firebase application and ensures that the user session is instantiated only once.
     *
     * @param activity      The main activity context used to initialize Firebase.
     * @param navController The {@link NavController} used for navigation within the app.
     */
    public static void initialize(MainActivity activity, NavController navController, Context context) {
        UserSession.context = context;
        if (instance == null) {
            instance = new UserSession(navController);
            Log.d("UserSession", "initialize: " + instance);
            FirebaseApp.initializeApp(activity);
        }
    }

    /**
     * Retrieves the singleton instance of the {@link UserSession}.
     * If the instance is not already created, it initializes a new instance using the provided navigation controller.
     *
     * @return The current instance of {@link UserSession}.
     */
    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession(navController);
        }
        return instance;
    }

    /**
     * Sets the user ID for the current user session.
     *
     * @param userId The unique identifier for the user to be set.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Retrieves the user ID of the current user session.
     *
     * @return The unique identifier of the user, or {@code null} if not set.
     */
    public String getUserId() {
        return userId;
    }


    /**
     * Creates a new user with the specified email and password.
     * <p>
     * This method uses Firebase Authentication to create the user account and
     * retrieves the user type from the database to instantiate the user representation.
     *
     * @param email    The email address for the new user.
     * @param password The password for the new user.
     */
    public void createUser(String email, String password) {
        DatabaseManager.getDatabaseManager().createUserWithEmailAndPassword(email, password, context, task -> DatabaseManager.getDatabaseManager().getUserData(DatabaseManager.USER_TYPE, userType -> {
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

    /**
     * Logs out the current user from the application.
     * <p>
     * This method invokes the logout functionality of the DatabaseManager
     * to sign out the user from Firebase Authentication. It also clears
     * the user representation and user ID from the current session.
     */
    public void logout() {
        DatabaseManager.getDatabaseManager().logout();
        if (userRepresentation != null)
            userRepresentation = null;
        // clear all the event listeners
        DatabaseListener.clearListeners();
        userId = null;
    }

}


