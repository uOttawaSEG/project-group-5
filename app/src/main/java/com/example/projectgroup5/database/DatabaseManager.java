package com.example.projectgroup5.database;

import android.util.Log;


import com.example.projectgroup5.MainActivity;
import com.example.projectgroup5.users.User;
import com.example.projectgroup5.users.UserSession;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
    private final FirebaseFirestore firestoreDatabase = FirebaseFirestore.getInstance();

    // test function to make sure stuff saves to the firestore database
    public void test() {
        Log.d("DatabaseManager", "test() called");
        firestoreDatabase.collection("users").document("test").set(Collections.singletonMap("test", "test"));
        Log.d("DatabaseManager", "test() finished");
        // get the data from the database
        firestoreDatabase.collection("users").document("test").get().addOnCompleteListener(task ->
                Log.d("DatabaseManager", "test() finished: " + task.getResult().getData()));
        Log.d("DatabaseManager", "test() waiting for result");

        // Display all the users in the firestore
        CollectionReference usersRef = firestoreDatabase.collection("users");
        usersRef.limit(10).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d("DatabaseManager", "User: " + document.getId() + " => " + document.getData());
                        }
                    } else {
                        Log.d("DatabaseManager", "Error getting documents: ", task.getException());
                    }
                });
        Log.d("DatabaseManager", "test() finished");
        test2();
    }

    private void test2() {
        // test of the storeUserValueToFirestore method
        Log.d("DatabaseManager", "test2() called");
        storeUserValueToFirestore("userIdvaluetest", "testkey", "testvalue", null);
        storeUserValueToFirestore("userIdvaluetest", USER_TYPE, User.USER_TYPE_ATTENDEE, null);
        Log.d("DatabaseManager", "test2() finished");

        // test of the getUserDataFromFirestore method
        Log.d("DatabaseManager", "test2() called");
        getUserDataFromFirestore("userIdvaluetest", "testkey", (Object data) -> {
                    if (data != null) {
                        Log.d("DatabaseManager", "test2() finished: " + data);
                    } else {
                        Log.d("DatabaseManager", "test2() finished: null");
                    }
                });
        getUserDataFromFirestore("userIdvaluetest", "nokey", (Object data) -> {
            if (data != null) {
                Log.d("DatabaseManager", "test2() finished: " + data);
            } else {
                Log.d("DatabaseManager", "test2() finished for key \"noKey\": null");
            }
        });
        Log.d("DatabaseManager", "test2() finished");
    }



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
    public void login(String email, String password, MainActivity context, OnCompleteListener<AuthResult> listener, OnSuccessListener<AuthResult> listener2) {
        logout();
        if (email == null || password == null || email.isEmpty() || password.isEmpty()) {
            Log.e("DatabaseManager", "Email or password is empty");
            // make a call back to the listener with a no success
            listener.onComplete(Tasks.forException(new Exception("Email or password is empty")));
            return;
        }
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener(listener2);
    }

    public String getAuthID() {
        return firebaseAuth.getCurrentUser().getUid();
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
            // clear the listeners
            DatabaseListener.clearListeners();
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
        if (firebaseAuth.getCurrentUser() != null)
            firebaseAuth.signOut();
        if (email == null || password == null || email.isEmpty() || password.isEmpty()) {
            Log.e("DatabaseManager", "Email or password is empty");
            listener.onComplete(Tasks.forException(new Exception("Email or password is empty")));
            return;
        }

        Log.d("DatabaseManager", "Creating a new user with email: " + email + " and password: " + password);
        // we want to give a confirmation to the listener that the user has been created
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(listener);

    }

    /**
     * Deletes the currently authenticated user account from the {@link #firestoreDatabase}.
     * <p>
     * This method first retrieves the currently signed-in user. If a user is logged in, it removes
     * their data from the database and deletes the user from Firebase Authentication. After the
     * deletion, it logs out the user. If no user is logged in, it calls the provided listener
     * with an exception indicating that no user is logged in.
     *
     * @param listener A listener that will be notified when the user deletion operation is complete.
     */
    public void deleteUserFromFirestore(OnCompleteListener<Void> listener) {
        //TODO: Implement this method
    }

    /**
     * TODO definitions
     */
    public void getUserDataFromFirestore(String userId, String key, final UserSession.FirebaseCallback<Object> callback) {
        if (userId == null) {
            Log.e("DatabaseManager", "User ID is null");
            callback.onCallback(null);
            return;
        }

        // Reference to Firestore
        DocumentReference docRef = firestoreDatabase.collection("users").document(userId);

        Log.d("DatabaseManager", "Fetching user data for key: " + key); // Debug log

        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Object data = document.get(key); // Get the value associated with the key
                    Log.d("DatabaseManager", "Data retrieved: " + data); // Debug log
                    callback.onCallback(data);
                } else {
                    Log.e("DatabaseManager", "Document does not exist for user ID: " + userId); // Debug log
                    callback.onCallback(null);
                }
            } else {
                Log.e("DatabaseManager", "getUserDataFromFirestore failed"); // Log the error
                Log.e("FirebaseError", "Error getting document: " + task.getException().getMessage()); // Log the error
                callback.onCallback(null);
            }
        });
    }


    /**
     * Retrieves user data from {@link #firestoreDatabase} for the currently authenticated user using the specified key.
     * <p>
     * It is a convenience method for accessing user data without requiring the caller to provide the user ID.
     * TODO: add documentation
     */
    public void getUserDataFromFirestore(String key, final UserSession.FirebaseCallback<Object> callback) {
        getUserDataFromFirestore(UserSession.getInstance().getUserId(), key, callback);
    }


    /**
     * Retrieves all user data from {@link #firestoreDatabase} for the specified user ID.
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
    public void getAllUserDataFromFirestore(String userId, final UserSession.FirebaseCallback<Map<String, Object>> callback) {
        if (userId == null) {
            Log.e("DatabaseManager", "User ID is null");
            callback.onCallback(null);
            return;
        }

        // Reference to Firestore
        DocumentReference docRef = firestoreDatabase.collection("users").document(userId);

        Log.d("DatabaseManager", "Fetching all user data for user ID: " + userId); // Debug log

        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Map<String, Object> data = document.getData(); // Get all user data as a Map
                    Log.d("DatabaseManager", "Data retrieved: " + data); // Debug log
                    callback.onCallback(data);
                } else {
                    Log.e("DatabaseManager", "Document does not exist for user ID: " + userId); // Debug log
                    callback.onCallback(null);
                }
            } else {
                Log.e("DatabaseManager", "getAllUserDataFromFirestore failed"); // Log the error
                Log.e("FirebaseError", "Error getting document: " + task.getException().getMessage()); // Log the error
                callback.onCallback(null);
            }
        });
    }

    /**
     * Adds a ValueEventListener from the {@link #firestoreDatabase} to the specified key for the currently authenticated user.
     * <p>
     * This method retrieves a reference to the user's data in the database using their user ID
     * and the specified key, then attaches the provided ValueEventListener to listen for changes.
     * Any updates to the data at this reference will trigger the listener's methods.
     *
     * @param eventListener The listener to be added for value events.
     * @param key                The key for the user data to listen to.
     */
    public ListenerRegistration addValueEventListenerToFirestore(String userId, EventListener<DocumentSnapshot> eventListener, String key) {

        if (userId == null) {
            Log.e("DatabaseManager", "User ID is null");
            return null;
        }

        // Reference to Firestore
        DocumentReference docRef = firestoreDatabase.collection("users").document(userId);

        Log.d("DatabaseManager", "Adding listener for key: " + key); // Debug log

        // Use the event listener to listen for document changes
        // Debug log
        // Call the provided eventListener
        return docRef.addSnapshotListener((snapshot, error) -> {
            if (error != null) {
                Log.e("FirebaseError", "Listen failed: " + error.getMessage());
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                Map<String, Object> data = snapshot.getData();
                if (data != null && data.containsKey(key)) {
                    Log.d("DatabaseManager", "Data updated for key " + key + ": " + data.get(key)); // Debug log
                    eventListener.onEvent(snapshot, null); // Call the provided eventListener
                } else {
                    Log.e("DatabaseManager", "Key does not exist in document: " + key);
                }
            } else {
                Log.e("DatabaseManager", "Document does not exist for user ID: " + userId);
            }
        });
    }

    public ListenerRegistration addValueEventListenerToFirestore(EventListener<DocumentSnapshot> eventListener, String key) {
        return addValueEventListenerToFirestore(UserSession.getInstance().getUserId(), eventListener, key);
    }

    /**
     * Removes a previously attached EventListener from the Firestore Database reference.
     *
     * <p>This method is used to detach a listener that was previously registered with
     * {@link #addValueEventListenerToFirestore(EventListener, String)}. It is important to call this
     * method when the listener is no longer needed to avoid memory leaks and
     * unintended behavior, especially in lifecycle-aware components such as Activities or Fragments.</p>
     *
     * @param listenerRegistration The EventListener to be removed.
     *                           It must be the same instance that was previously added.
     *                           If the listener was not added, this method has no effect.
     */
    public void removeEventListenerFromFirestore(ListenerRegistration listenerRegistration) {
        if (listenerRegistration != null) {
            listenerRegistration.remove(); // Detach the listener
            Log.d("DatabaseManager", "Listener removed from Firestore");
        } else {
            Log.e("DatabaseManager", "No listener to remove from Firestore");
        }
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
     * Retrieves user IDs that match a specified key-value pair from firestore.
     * <p>
     * This method queries the user data in the database and checks for entries where the specified
     * key matches the given value. It collects the user IDs of all matching entries and invokes the
     * provided callback with the list of these user IDs. If the query is cancelled or encounters an
     * error, it returns an empty list via the callback.
     *
     * @param field           The key to match against in the user data.
     * @param matchingValue The value to be matched for the specified key.
     * @param callback      A callback to be invoked with the list of matching user IDs.
     */
    public void getUserIdByMatchingDataFromFirestore(String field, Object matchingValue, DataCallback callback) {
        CollectionReference usersCollection = firestoreDatabase.collection("users");

        // Create a query that filters documents based on the key and matching value
        usersCollection.whereEqualTo(field, matchingValue).get()
                .addOnCompleteListener(task -> {
                    List<String> userIds = new ArrayList<>();

                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            userIds.add(document.getId()); // Get the user ID (document ID)
                        }
                        Log.d("DatabaseManager", "Done retrieving data from Firestore"); // Log completion
                    } else {
                        Log.e("DatabaseManager", "getUserIdByMatchingDataFromFirestore failed"); // Log the error
                        Log.e("FirestoreError", "Error getting documents: ", task.getException());
                    }
                    // Log the retrieved user IDs
                    Log.d("DatabaseManager", "Retrieved user IDs: " + userIds);

                    // Trigger the callback with the retrieved user IDs
                    callback.onDataReceived(userIds);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Query failed: " + e.getMessage());
                    // Return an empty list on failure
                    callback.onDataReceived(Collections.emptyList());
                });
    }

    /**
     * Stores a value in the firestore database under the specified user ID and type.
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
    public void storeUserValueToFirestore(String userId, String type, @Nullable Object value, @Nullable OnCompleteListener<Void> listener) {
        // Create a reference to the document
        DocumentReference docRef = firestoreDatabase.collection("users").document(userId);

        // Prepare the data to store as a map
        Map<String, Object> data = new HashMap<>();
        data.put(type, value);

        Log.d("DatabaseManager", "Storing data in storeUserValueToFirestore: " + data); // Log the data being stored

        // Use set() with SetOptions.merge() to store the value without overwriting existing data
        docRef.set(data, SetOptions.merge())
                .addOnCompleteListener(task -> {
                    if (listener != null) {
                        listener.onComplete(task); // Notify listener on completion
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Failed to store value: " + e.getMessage());
                    if (listener != null) {
                        listener.onComplete(Tasks.forException(e)); // Notify listener with failure
                    }
                });

        Log.d("DatabaseManager", "Done storing data"); // Log completion
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
    public void storeUserValueToFirestore(String type, @Nullable Object value, OnCompleteListener<Void> listener) {
        storeUserValueToFirestore(UserSession.getInstance().getUserId(), type, value, listener);
    }
}
