package com.example.projectgroup5.users;

import android.util.Log;

import androidx.navigation.NavController;

import com.example.projectgroup5.MainActivity;
import com.example.projectgroup5.database.DatabaseListener;
import com.example.projectgroup5.database.DatabaseManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;

public class UserSession {
    private static UserSession instance;
    private String userId;
    private static User userRepresentation;

    /**
     * Constructs a new instance of the UserSession class and initializes it with a navigation controller.
     * <p>
     * This constructor sets the provided {@link NavController} for the session and checks if
     * a user is already logged in by invoking {@link #instantiateUserRepresentation(MainActivity, OnCompleteListener)}.
     */
    private UserSession(MainActivity context, OnCompleteListener<AuthResult> listener) {
//        UserSession.navController = navController;
        // check if the user is already logged in
        instantiateUserRepresentation(context, listener);
    }

    public interface FirebaseCallback<T> {
        void onCallback(T value);
    }

    /**
     * Instantiates the user representation based on the current logged-in user.
     * <p>
     * This method retrieves the current user from the database and updates the user ID.
     * It fetches the user's type from the database and creates a corresponding user representation
     * using the {@link User#newUserFromDatabase(String, OnCompleteListener)} method. If the user type is not found,
     * an error is logged. Additionally, it navigates to the account management screen
     * after successfully instantiating the user representation.
     * <p>
     * If no user is currently logged in, an error message is logged.
     */
    public void instantiateUserRepresentation(MainActivity context, OnCompleteListener<AuthResult> listener) {
        FirebaseUser user = DatabaseManager.getDatabaseManager().getCurrentUser();
        if (user == null) {
            Log.e("UserSession", "User is null");
            return;
        }
        userId = user.getUid();
        // update all the data from the database
        // Create a User representation based on the user type
        if (userRepresentation != null) {
            Log.e("UserSession", "User representation is not initially null");
            return;
        }
        User.newUserFromDatabase(userId, task -> {
            if (task.isSuccessful()) {
                userRepresentation = task.getResult();
                Log.d("UserSession", "User representation created");
                // Notification for prelogged in user
                DatabaseListener.clearListeners();
                DatabaseListener.addValueAccountCreationEventListener(context);
                // success on the listener
                listener.onComplete(Tasks.forResult(null));
            } else {
                Log.e("UserSession", "User representation not created");
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
    public void login(String email, String password, MainActivity context, OnCompleteListener<AuthResult> listener) {
        DatabaseManager.getDatabaseManager().login(email, password, context, listener, task -> {
            UserSession.getInstance().setUserId(DatabaseManager.getDatabaseManager().getAuthID());
            if (UserSession.getInstance().getUserRepresentation() == null)
                UserSession.getInstance().instantiateUserRepresentation(context, listener);
        });
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
     */
    public static void initialize(MainActivity activity, OnCompleteListener<AuthResult> listener) {
        if (instance == null) {
            instance = new UserSession(activity, listener);
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
            System.err.println("UserSession is null");
//            instance = new UserSession();
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


    /*
     * Creates a new user with the specified email and password.
     * <p>
     * This method uses Firebase Authentication to create the user account and
     * retrieves the user type from the database to instantiate the user representation.
     *
     * @param email    The email address for the new user.
     * @param password The password for the new user.
     */
//    public void createUser(String email, String password, MainActivity context, OnCompleteListener<AuthResult> listener) {
        // TODO check this out
//        Log.d("UserSession", "createUser: " + email + " " + password);
//        DatabaseManager.getDatabaseManager().createUserWithEmailAndPassword(email, password, context, task -> DatabaseManager.getDatabaseManager().getUserDataFromFirestore(DatabaseManager.USER_TYPE, userType -> {
//            if (userType != null) {
//                Log.d("UserSession", "User type: " + userType);
//                // Create a User representation based on the user type
//                userRepresentation = User.newUserFromDatabase(userId, (int) (long) ((Long) userType));
//                if (userRepresentation == null) {
//                    Log.e("UserSession", "User representation is null 1");
////                            return;
//                }
//                Log.d("UserSession", "The user representation is: " + userRepresentation.toString() + " with user type: " + userType);
//                instantiateEmailForUser(DatabaseManager.getDatabaseManager().getCurrentUser());
////                navController.navigate(R.id.account);
//                Log.d("UserSession", "User type: " + userType);
//            } else {
//                Log.e("UserSession", "User type not found");
//            }
//            Log.d("UserSession", "aaaaaaaaa: " + userType);
//            if (task.isSuccessful()) {
//                Log.d("UserSession", "createUserWithEmail:success");
//                listener.onComplete(task);
//            }
//            else {
//                Log.w("UserSession", "createUserWithEmail:failure", task.getException());
//                listener.onComplete(task);
//            }
//        }));
//        Log.d("UserSession", "created User: " + email + " " + password);
//    }

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


