package com.example.projectgroup5.users;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import java.util.List;
import java.util.Map;

public class DatabaseManager {
    public static final String USER_TYPE = "UserType";
    public static final String USER_EMAIL = "UserEmail";
    public static final String USER_PHONE = "UserPhone";
    public static final String USER_ADDRESS = "UserAddress";
    public static final String USER_FIRST_NAME = "UserFirstName";
    public static final String USER_LAST_NAME = "UserLastName";
    public static final String USER_REGISTRATION_STATE = "UserRegistrationState";
    public static final String USER_ORGANIZATION_NAME = "UserOrganizationName";
    private static final DatabaseManager databaseManager = new DatabaseManager();
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();

    /**
     * Retrieves the singleton instance of the DatabaseManager.
     * <p>
     * This static method returns the current instance of DatabaseManager,
     * providing access to database operations. It is designed to ensure that
     * only one instance of DatabaseManager is used throughout the application.
     *
     * @return The singleton instance of DatabaseManager.
     */
    public static DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    /**
     * Private constructor for the DatabaseManager class.
     * <p>
     * This constructor is used to prevent the instantiation of the DatabaseManager class
     * from outside the class. It is typically employed in singleton design patterns to ensure
     * that only one instance of the class is created.
     */
    private DatabaseManager() {
    }

    /**
     * Authenticates a user with the provided email and password.
     * <p>
     * This method attempts to log in a user by using Firebase Authentication. If the email
     * or password is null or empty, it logs an error and calls the provided listener with
     * an exception. Upon successful login, it instantiates the user representation.
     *
     * @param email    The email of the user attempting to log in.
     * @param password The password of the user attempting to log in.
     * @param listener A listener that will be notified when the login operation is complete.
     */
    public void login(String email, String password, OnCompleteListener<AuthResult> listener) {
        logout();
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

    /**
     * Logs out the currently authenticated user.
     * <p>
     * This method checks if there is a currently signed-in user. If so, it signs out the user
     * from Firebase Authentication, effectively ending the user's session.
     */
    public void logout() {
        if (firebaseAuth.getCurrentUser() != null) {
            firebaseAuth.signOut();
        }
    }

    /**
     * Retrieves the currently authenticated user.
     * <p>
     * This method returns the FirebaseUser object representing the user
     * who is currently signed in. If no user is signed in, it returns null.
     *
     * @return The currently authenticated FirebaseUser, or null if no user is signed in.
     */
    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    /**
     * Creates a new user account using the provided email and password.
     * <p>
     * This method first checks if there is a currently signed-in user; if so, it signs them out.
     * It then attempts to create a new user account with Firebase Authentication. Upon successful
     * creation of the account, it instantiates the user representation.
     *
     * @param email    The email address for the new user account.
     * @param password The password for the new user account.
     * @param listener A listener that will be notified when the account creation operation is complete.
     */
    public void createUserWithEmailAndPassword(String email, String password, OnCompleteListener<AuthResult> listener) {
        if (firebaseAuth.getCurrentUser() != null) {
            firebaseAuth.signOut();
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(listener).addOnSuccessListener(task -> UserSession.getInstance().instantiateUserRepresentation());
        }
    }

    /**
     * Deletes the currently authenticated user account.
     * <p>
     * This method first retrieves the currently signed-in user. If a user is logged in, it removes
     * their data from the database and deletes the user from Firebase Authentication. After the
     * deletion, it logs out the user. If no user is logged in, it calls the provided listener
     * with an exception indicating that no user is logged in.
     *
     * @param listener A listener that will be notified when the user deletion operation is complete.
     */
    public void deleteUser(OnCompleteListener<Void> listener) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            DatabaseReference ref = database.getReference("users").child(user.getUid());
            ref.removeValue().addOnCompleteListener(listener);
            // Delete the user from Firebase Authentication
            user.delete().addOnCompleteListener(listener);
            // Clear the user session
            UserSession.getInstance().logout();
        } else {
            // Edge case where no user is logged in
            listener.onComplete(Tasks.forException(new Exception("No user logged in")));
        }
    }

    /**
     * @param userId   The ID of the user whose data is to be retrieved.
     * @param key      the key for the user data to be retrieved.
     * @param callback The allowed return types for the data are as follows:
     *                 <ul>
     *                   <li><code>Boolean</code></li>
     *                   <li><code>String</code></li>
     *                   <li><code>Long</code></li>
     *                   <li><code>Double</code></li>
     *                   <li><code>Map&lt;String, Object&gt;</code></li>
     *                   <li><code>List&lt;Object&gt;</code></li>
     *                 </ul>
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

    /**
     * Retrieves all user data for the specified user ID.
     * <p>
     * This method checks if the provided user ID is null. If it is, it logs an error and invokes
     * the callback with null. If the user ID is valid, it adds a listener to a database reference
     * to retrieve the user's data. On successful retrieval, it passes the data to the callback.
     * If the user data does not exist or if the database operation is cancelled, it calls the callback
     * with null.
     *
     * @param userId   The ID of the user whose data is to be retrieved.
     * @param callback A callback to be invoked with the retrieved user data or null if an error occurs.
     *                 The allowed return types for the data are as follows:
     *                 <ul>
     *                   <li><code>Map&lt;String, Object&gt;</code></li>
     *                   <li><code>null</code></li>
     *                 </ul>
     */
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

    /**
     * Retrieves user data for the currently authenticated user using the specified key.
     * <p>
     * This method calls another version of `getUserData` with the current user's ID and the provided key.
     * It is a convenience method for accessing user data without requiring the caller to provide the user ID.
     *
     * @param key      The key for the user data to be retrieved.
     * @param callback A callback to be invoked with the retrieved user data or null if an error occurs.
     *                 The allowed return types for the data are as follows:
     *                 <ul>
     *                   <li><code>Boolean</code></li>
     *                   <li><code>String</code></li>
     *                   <li><code>Long</code></li>
     *                   <li><code>Double</code></li>
     *                   <li><code>Map&lt;String, Object&gt;</code></li>
     *                   <li><code>List&lt;Object&gt;</code></li>
     *                 </ul>
     */
    public void getUserData(String key, final UserSession.FirebaseCallback<Object> callback) {
        getUserData(UserSession.getInstance().getUserId(), key, callback);
    }

    /**
     * Adds a ValueEventListener to the specified key for the currently authenticated user.
     * <p>
     * This method retrieves a reference to the user's data in the database using their user ID
     * and the specified key, then attaches the provided ValueEventListener to listen for changes.
     * Any updates to the data at this reference will trigger the listener's methods.
     *
     * @param valueEventListener The listener to be added for value events.
     * @param key                The key for the user data to listen to.
     */
    public void addValueEventListener(ValueEventListener valueEventListener, String key) {
        if (UserSession.getInstance().getUserId() == null) {
            Log.e("UserSession", "User ID is null");
            return;
        }
        DatabaseReference ref = database.getReference("users").child(UserSession.getInstance().getUserId()).child(key);
        ref.addValueEventListener(valueEventListener);
    }

    /**
     * Removes a previously attached ValueEventListener from the Firebase Realtime Database reference.
     *
     * <p>This method is used to detach a listener that was previously registered with
     * {@link #addValueEventListener(ValueEventListener, String)}. It is important to call this
     * method when the listener is no longer needed to avoid memory leaks and
     * unintended behavior, especially in lifecycle-aware components such as Activities or Fragments.</p>
     *
     * @param valueEventListener The ValueEventListener to be removed.
     *                           It must be the same instance that was previously added.
     *                           If the listener was not added, this method has no effect.
     */
    public void removeEventListener(ValueEventListener valueEventListener) {
        database.getReference().removeEventListener(valueEventListener);
    }

    /**
     * Callback interface for receiving a list of user IDs.
     * <p>
     * This interface provides a method to handle the asynchronous retrieval of user IDs.
     * Implementations of this interface should define what happens when the data is received.
     */
    public interface DataCallback {
        /**
         * Called when the data is successfully retrieved.
         *
         * @param userIds A list of user IDs received.
         */
        void onDataReceived(List<String> userIds);
    }

    /**
     * Retrieves user IDs that match a specified key-value pair from the database.
     * <p>
     * This method queries the user data in the database and checks for entries where the specified
     * key matches the given value. It collects the user IDs of all matching entries and invokes the
     * provided callback with the list of these user IDs. If the query is cancelled or encounters an
     * error, it returns an empty list via the callback.
     *
     * @param key           The key to match against in the user data.
     * @param matchingValue The value to be matched for the specified key.
     * @param callback      A callback to be invoked with the list of matching user IDs.
     */
    public void getUserIdByMatchingData(String key, String matchingValue, DataCallback callback) {
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
                            if (!userMap.containsKey(key)) {
                                continue;
                            }

                            Object userEntryValue = userMap.get(key);
                            if (userEntryValue == null) {
                                continue;
                            }

                            if (!userEntryValue.toString().equals(matchingValue)) {
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

    /**
     * Stores a value in the database under the specified user ID and type.
     * <p>
     * This method writes the provided value to the database at the specified path,
     * which is constructed using the user ID and type. Upon completion of the write
     * operation, it invokes the provided listener to notify about the result.
     *
     * @param userId   The ID of the user for whom the value is being stored.
     * @param type     The type/category under which the value is stored.
     * @param value    The value to be stored in the database; can be null.
     * @param listener A listener to be notified when the store operation is complete.
     */
    public void storeUserValue(String userId, String type, @Nullable Object value, @Nullable OnCompleteListener<Void> listener) {
        DatabaseReference ref = database.getReference().child("users").child(userId).child(type);
        Task<Void> task = ref.setValue(value);
        if (listener != null) task.addOnCompleteListener(listener);
    }

    /**
     * Stores a value for the currently authenticated user under the specified type.
     * <p>
     * This method calls another version of `storeValue` with the current user's ID, the provided
     * type, and the value to be stored. It serves as a convenience method for storing user data
     * without needing to specify the user ID explicitly.
     *
     * @param type     The type/category under which the value is stored.
     * @param value    The value to be stored in the database; can be null.
     * @param listener A listener to be notified when the store operation is complete.
     */
    public void storeUserValue(String type, @Nullable Object value, OnCompleteListener<Void> listener) {
        storeUserValue(UserSession.getInstance().getUserId(), type, value, listener);
    }
}
