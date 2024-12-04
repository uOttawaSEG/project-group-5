package com.example.projectgroup5.database;

import android.util.Log;

import com.example.projectgroup5.events.Event;
import com.example.projectgroup5.events.EventOption;
import com.example.projectgroup5.events.EventOptional;
import com.example.projectgroup5.events.Registration;
import com.example.projectgroup5.users.Attendee;
import com.example.projectgroup5.users.Organizer;
import com.example.projectgroup5.users.User;
import com.example.projectgroup5.users.UserSession;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
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
import java.util.concurrent.atomic.AtomicInteger;

public class DatabaseManager {
    //---------------------------------------------Constants-------------------------------------------------------------------
    public static final String USER_TYPE = "UserType";
    public static final String USER_EMAIL = "UserEmail";
    public static final String USER_PHONE = "UserPhone";
    public static final String USER_ADDRESS = "UserAddress";
    public static final String USER_FIRST_NAME = "UserFirstName";
    public static final String USER_LAST_NAME = "UserLastName";
    public static final String USER_REGISTRATION_STATE = "UserRegistrationState";
    public static final String USER_ORGANIZATION_NAME = "UserOrganizationName";
    public static final String USER_ATTENDEE_REGISTRATIONS = "UserAttendeeRegistrations";
    public static final String USER_ORGANIZER_EVENTS = "UserOrganizerEvents";
    public static final String EVENT_TITLE = "EventTitle";
    public static final String EVENT_DESCRIPTION = "EventDescription";
    public static final String EVENT_ADDRESS = "EventAddress";
    public static final String EVENT_START_TIME = "EventStartTime";
    public static final String EVENT_END_TIME = "EventEndTime";
    public static final String EVENT_AUTO_ACCEPT = "AutoAccept";
    public static final String EVENT_REGISTRATIONS = "EventRegistration"; // this is a list of registrations, they can be found in /registrations/{registrationID}
    public static final String EVENT_ORGANIZER = "EventOrganizer";
    public static final String REGISTRATION_ATTENDEE = "attendee";
    public static final String EVENT_REGISTRATION_STATUS = "eventRegistrationStatus";
    public static final String REGISTRATION_EVENT = "event";

    //-------------------------------------------DB related instances-------------------------------------------------------
    private static final DatabaseManager databaseManager = new DatabaseManager();
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore firestoreDatabase = FirebaseFirestore.getInstance();

    //---------------------------------------------USER RELATED DB METHODS-------------------------------------------------------------------
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
            Log.e("DatabaseManager", "Email or password is empty");
            // make a call back to the listener with a no success
            listener.onComplete(Tasks.forException(new Exception("Email or password is empty")));
            return;
        }
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(listener);
    }

    /**
     * Retrieves the unique authentication ID (UID) of the currently authenticated user.
     * This method fetches the current user's UID from Firebase Authentication. It is used
     * to identify the user within the system for operations that require user-specific data.
     *
     * @return The UID of the currently authenticated user as a {@link String}.
     */
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
     * Fetches user data from Firestore for a specific user and key.
     * This method retrieves data from the "users" collection in Firestore based on the provided user ID and key.
     * If the user document exists and the key is found, the associated value is returned via the callback.
     * If the document does not exist or an error occurs, the callback is invoked with a `null` value.
     *
     * @param userId The unique identifier for the user whose data is being retrieved.
     * @param key The key representing the specific data field to retrieve from the user's document.
     * @param callback The callback to handle the retrieved data or handle errors. It accepts an {@link Object} containing the data or `null` if retrieval fails.
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
                    Log.d("DatabaseManager", "All the Data retrieved: " + data); // Debug log
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
     * @param key           The key for the user data to listen to.
     */
    public ListenerRegistration addValueEventListenerToFirestoreUserData(String userId, EventListener<DocumentSnapshot> eventListener, String key) {

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

    /**
     * Adds a value event listener to Firestore user data for the currently authenticated user.
     * This method calls the overloaded version of the function, passing the current user's ID
     * along with the provided event listener and key to monitor changes to a specific field of the user's data.
     *
     * @param eventListener The {@link EventListener} to handle changes to the user's data document.
     * @param key The key in the user's document whose changes will be listened to.
     * @return A {@link ListenerRegistration} object used to manage and remove the event listener.
     */
    public ListenerRegistration addValueEventListenerToFirestoreUserData(EventListener<DocumentSnapshot> eventListener, String key) {
        return addValueEventListenerToFirestoreUserData(UserSession.getInstance().getUserId(), eventListener, key);
    }

    /**
     * Removes a previously attached EventListener from the Firestore Database reference.
     *
     * <p>This method is used to detach a listener that was previously registered with
     * {@link #addValueEventListenerToFirestoreUserData(EventListener, String)}. It is important to call this
     * method when the listener is no longer needed to avoid memory leaks and
     * unintended behavior, especially in lifecycle-aware components such as Activities or Fragments.</p>
     *
     * @param listenerRegistration The EventListener to be removed.
     *                             It must be the same instance that was previously added.
     *                             If the listener was not added, this method has no effect.
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
     * @param field         The key to match against in the user data.
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

    /**
     * Retrieves a Firestore {@link DocumentReference} for the currently authenticated user.
     * This method calls another overloaded version of the function, passing the current user's ID
     * to obtain the reference to the user's document in Firestore.
     *
     * @return A {@link DocumentReference} pointing to the current user's document in Firestore.
     */
    public DocumentReference getCurrentUserReference() {
        return getUserReference(UserSession.getInstance().getUserId());
    }

    /**
     * Retrieves a Firestore {@link DocumentReference} for a specific user based on their user ID.
     * This method returns a reference to the user's document in the "users" collection in Firestore
     * using the provided user ID.
     *
     * @param userId The unique identifier of the user whose document reference is to be retrieved.
     * @return A {@link DocumentReference} pointing to the specified user's document in Firestore.
     */
    public DocumentReference getUserReference(String userId) {
        return firestoreDatabase.collection("users").document(userId);
    }

    /**
     * Creates a new user or updates an existing user from the ground up in Firestore.
     * This method first attempts to create a new user using email and password, and then stores the user's data in Firestore.
     * It handles the user creation and stores user information like type, address, email, phone number, name, and registration state.
     * Depending on the type of user (Organizer or Attendee), additional data may also be stored, such as organization name and event registrations.
     * This method is intended to be called when creating a completely new user or when updating an existing user's data entirely.
     *
     * @param user The {@link User} object containing the user details to be saved.
     * @param password The password to be associated with the new user account.
     * @param listener The {@link OnCompleteListener} to be notified upon completion of the user creation and data storage process.
     *                 If the process fails, an exception is passed to the listener; otherwise, the task is marked as successful.
     */
    public void createNewUser(User user, String password, OnCompleteListener<DocumentReference> listener) {
        databaseManager.createUserWithEmailAndPassword(user.getUserEmail(), password, task -> {
            // now we have tried to create the user, lets check if it was successful
            if (!task.isSuccessful()) {
                listener.onComplete(Tasks.forException(task.getException()));
                Log.e("DatabaseManager", "User creation failed: " + task.getException());
                return;
            } else {
                Log.d("DatabaseManager", "User creation successful");
            }
            // now we have created the user, lets store the user data
            // we must first make sure that the UserSession userid is set
            UserSession.getInstance().setUserId(task.getResult().getUser().getUid());

            // Create a reference to the document
            DocumentReference itemReference = firestoreDatabase.collection("users").document(UserSession.getInstance().getUserId());
            // Initialize a counter for the number of tasks
            int totalTasks = user instanceof Organizer ? 9 : 8; // Number of Firestore tasks
            AtomicInteger tasksCompleted = new AtomicInteger(0); // Use AtomicInteger for thread safety

            databaseManager.storeUserValueToFirestore(
                    DatabaseManager.USER_TYPE,
                    user instanceof Organizer ? User.USER_TYPE_ORGANIZER : User.USER_TYPE_ATTENDEE,
                    (task0) -> handleTaskCompletion(task0, tasksCompleted, totalTasks, itemReference, listener) //, "storeUserTypeError")
            );

            databaseManager.storeUserValueToFirestore(
                    DatabaseManager.USER_ADDRESS,
                    user.getUserAddress(),
                    (task0) -> handleTaskCompletion(task0, tasksCompleted, totalTasks, itemReference, listener) // , "storeUserAddressError")
            );

            databaseManager.storeUserValueToFirestore(
                    DatabaseManager.USER_EMAIL,
                    user.getUserEmail(),
                    (task0) -> handleTaskCompletion(task0, tasksCompleted, totalTasks, itemReference, listener) //  , "storeUserEmailError")
            );

            databaseManager.storeUserValueToFirestore(
                    DatabaseManager.USER_PHONE,
                    user.getPhoneNumber(),
                    (task0) -> handleTaskCompletion(task0, tasksCompleted, totalTasks, itemReference, listener) // , "storeUserPhoneError")
            );

            databaseManager.storeUserValueToFirestore(
                    DatabaseManager.USER_FIRST_NAME,
                    user.getFirstName(),
                    (task0) -> handleTaskCompletion(task0, tasksCompleted, totalTasks, itemReference, listener) // , "storeUserFirstNameError")
            );

            databaseManager.storeUserValueToFirestore(
                    DatabaseManager.USER_LAST_NAME,
                    user.getLastName(),
                    (task0) -> handleTaskCompletion(task0, tasksCompleted, totalTasks, itemReference, listener) // , "storeUserLastNameError")
            );

            databaseManager.storeUserValueToFirestore(
                    DatabaseManager.USER_REGISTRATION_STATE,
                    User.WAITLISTED,
                    (task0) -> handleTaskCompletion(task0, tasksCompleted, totalTasks, itemReference, listener) // , "storeUserUserRegistrationState")
            );

            if (user instanceof Organizer organizer) {
                databaseManager.storeUserValueToFirestore(
                        USER_ORGANIZATION_NAME,
                        organizer.getUserOrganizationName(),
                        (task0) -> handleTaskCompletion(task0, tasksCompleted, totalTasks, itemReference, listener) // , "storeUserOrganisationError")
                );

                databaseManager.storeUserValueToFirestore(
                        USER_ORGANIZER_EVENTS,
                        organizer.getOrganizerEvents(),
                        (task0) -> handleTaskCompletion(task0, tasksCompleted, totalTasks, itemReference, listener) // , "storeUserOrganizerEventsError")
                );
            }

            if (user instanceof Attendee attendee) {
                databaseManager.storeUserValueToFirestore(
                        USER_ATTENDEE_REGISTRATIONS,
                        attendee.getAttendeeRegistrations(),
                        (task0) -> handleTaskCompletion(task0, tasksCompleted, totalTasks, itemReference, listener));
            }
        });
    }



    /**
     * Adds an event to the specified organizer's event list in Firestore.
     * This method retrieves the user data for the specified organizer, checks if the user is an
     * instance of {@link Organizer}, and then adds the event reference to the organizer's list of events in Firestore.
     * Additionally, the event reference is added to the {@link Organizer} object locally.
     *
     * @param organizerId The unique identifier of the organizer to whom the event will be added.
     * @param eventRef The {@link DocumentReference} of the event to be added to the organizer's event list.
     * @param listener The {@link OnCompleteListener} to handle the result of the operation.
     *                 It will be triggered once the event is successfully added or if the operation fails.
     */
    public void addEventToOrganizer(String organizerId, DocumentReference eventRef, OnCompleteListener<Void> listener) {
        User.newUserFromDatabase(organizerId, userTask -> {
            if (userTask.isSuccessful()) {
                Log.d("DatabaseManager", "User retrieved: " + userTask.getResult());
                User user = userTask.getResult();
                Log.d("DatabaseManager", "User type: " + user.getUserType());
                if (user instanceof Organizer organizer) {
                    getUserReference(organizerId).update(USER_ORGANIZER_EVENTS, FieldValue.arrayUnion(eventRef)).addOnCompleteListener(listener);
                    Log.d("DatabaseManager", "Event added to organizer: " + eventRef);
                    organizer.addEvent(eventRef);
                }
            }
        });
    }

    /**
     * Adds an event to the current user's organizer collection in Firestore.
     * This method calls the overloaded version of the function, passing the current user's ID
     * along with the event reference to add the event to the user's organizer data.
     *
     * @param eventRef The {@link DocumentReference} of the event to be added to the organizer's collection.
     * @param listener The {@link OnCompleteListener} to handle the result of the operation.
     *                 It will be triggered once the event is successfully added or if the operation fails.
     */
    public void addEventToOrganizer(DocumentReference eventRef, OnCompleteListener<Void> listener) {
        addEventToOrganizer(getCurrentUserReference().getId(), eventRef, listener);
    }

    /**
     * Adds a registration to the specified attendee's registration list in Firestore.
     * This method retrieves the user data for the specified attendee, checks if the user is an
     * instance of {@link Attendee}, and then adds the registration reference to the attendee's list
     * of registrations in Firestore. Additionally, the registration reference is added to the
     * {@link Attendee} object locally.
     *
     * @param attendeeId The unique identifier of the attendee to whom the registration will be added.
     * @param registrationId The {@link DocumentReference} of the registration to be added to the attendee's list.
     * @param listener The {@link OnCompleteListener} to handle the result of the operation.
     *                 It will be triggered once the registration is successfully added or if the operation fails.
     */
    public void addRegistrationToAttendee(String attendeeId, DocumentReference registrationId, OnCompleteListener<Void> listener) {
        User.newUserFromDatabase(attendeeId, userTask -> {
            if (userTask.isSuccessful()) {
                User user = userTask.getResult();
                if (user instanceof Attendee attendee) {
                    getUserReference(attendeeId).update(USER_ATTENDEE_REGISTRATIONS, FieldValue.arrayUnion(registrationId)).addOnCompleteListener(listener).addOnCompleteListener(task -> attendee.addRegistration(registrationId)).addOnCompleteListener(task -> Log.d("DatabaseManager", "Registration added to attendee in addRegistrationToAttendee: " + registrationId));
                }
            }
        });
    }

    /**
     * Removes an event from the current organizer's event list in Firestore.
     * This method checks if the current user is an {@link Organizer}, and if so, it removes the event
     * reference from the organizer's event list in Firestore. Additionally, the event reference is removed
     * from the {@link Organizer} object locally.
     *
     * @param eventRef The {@link DocumentReference} of the event to be removed from the organizer's event list.
     * @param listener The {@link OnCompleteListener} to handle the result of the operation.
     *                 It will be triggered once the event is successfully removed or if the operation fails.
     */
    public void removeEventFromOrganizer(DocumentReference eventRef, OnCompleteListener<Void> listener) {
        if (UserSession.getInstance().getUserRepresentation() instanceof Organizer organizer) {
            // now we have an organizer we have to add the event to him in the database
            Log.d("DatabaseManager", "Removing event from organizer: " + eventRef);
            getCurrentUserReference().update(USER_ORGANIZER_EVENTS, FieldValue.arrayRemove(eventRef)).addOnCompleteListener(listener).addOnCompleteListener(task -> organizer.removeEvent(eventRef));
        }
    }

    /**
     * Retrieves the list of registrations for the specified attendee.
     * This method retrieves the user data for the specified user ID, checks if the user is an
     * instance of {@link Attendee}, and if so, it returns the list of event registrations associated
     * with the attendee.
     *
     * @param userId The unique identifier of the attendee whose registrations will be retrieved.
     * @param listener The {@link OnCompleteListener} to handle the result of the operation.
     *                 It will be triggered once the attendee's registrations are successfully fetched
     *                 or if the operation fails.
     */
    public void getAttendeeRegistrations(String userId, OnCompleteListener<List<DocumentReference>> listener) {
        // get the user data from the database
        User.newUserFromDatabase(userId, userTask -> {
            if (userTask.isSuccessful()) {
                User user = userTask.getResult();
                if (user instanceof Attendee attendee) {
                    listener.onComplete(Tasks.forResult(attendee.getAttendeeRegistrations()));
                }
            }
        });
    }

    /**
     * Retrieves the list of events associated with the specified organizer.
     * This method fetches the list of event references for the specified organizer user ID, retrieves
     * the event details for each reference, and compiles a list of {@link Event} objects. Once all the
     * events are retrieved, the result is passed to the provided listener.
     *
     * @param userId The unique identifier of the organizer whose events will be retrieved.
     * @param listener The {@link OnCompleteListener} to handle the result of the operation.
     *                 It will be triggered once the list of events has been successfully retrieved
     *                 or if the operation fails.
     */
    public void getOrganizerEvents(String userId, OnCompleteListener<List<Event>> listener) {
        getOrganizerEventsReference(userId, task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<Event> events = new ArrayList<>();
                AtomicInteger remainingCalls = new AtomicInteger(task.getResult().size());
                for (DocumentReference eventRef : task.getResult()) {
                    DatabaseManager.getDatabaseManager().getEvent(eventRef.getId(), task2 -> {
                        if (task2.isSuccessful() && task2.getResult() != null && task2.getResult().holdsAnEvent()) {
                            events.add(task2.getResult().getEvent());
                        }
                        if (remainingCalls.decrementAndGet() == 0) {
                            listener.onComplete(Tasks.forResult(events));
                        }
                });
                }
            }
        });
    }

    /**
     * Retrieves the list of events references associated with the specified organizer.
     * This method fetches the list of event references for the specified organizer user ID, retrieves
     * the event details for each reference, and compiles a list of {@link Event} objects. Once all the
     * events are retrieved, the result is passed to the provided listener.
     *
     * @param userId The unique identifier of the organizer whose events will be retrieved.
     * @param listener The {@link OnCompleteListener} to handle the result of the operation.
     *                 It will be triggered once the list of events has been successfully retrieved
     *                 or if the operation fails.
     */
    public void getOrganizerEventsReference(String userId, OnCompleteListener<List<DocumentReference>> listener) {
        // get the user data from the database
        User.newUserFromDatabase(userId, userTask -> {
            if (userTask.isSuccessful()) {
                User user = userTask.getResult();
                Log.d("DatabaseManager", "User retrieved in getOrganizerEvents: " + userTask.getResult());
                if (user instanceof Organizer organizer) {
                    Log.d("DatabaseManager", "Organizer events retrieved in getOrganizerEvents: " + organizer.getOrganizerEvents());
                    listener.onComplete(Tasks.forResult(organizer.getOrganizerEvents()));
                }
            }
        });
    }


//---------------------------------------EVENT------------------------------------------------

    /**
     * Retrieves a reference to a specific event document in Firestore.
     * This method returns a {@link DocumentReference} to the Firestore document associated
     * with the specified event ID, allowing further interactions (e.g., reading or updating the event data).
     *
     * @param eventId The unique identifier of the event whose document reference is to be retrieved.
     * @return A {@link DocumentReference} pointing to the event document in Firestore.
     */
    public DocumentReference getEventReference(String eventId) {
        return firestoreDatabase.collection("events").document(eventId);
    }

    /**
     * Stores a specified value in the Firestore document for the given event.
     * This method stores a value for a specific field (type) in the event document, identified by
     * the provided event ID. It uses {@link SetOptions#merge()} to ensure that only the specified field
     * is updated without overwriting other existing fields in the document. The provided listener will
     * be notified once the operation completes.
     *
     * @param eventID The unique identifier of the event whose document will be updated.
     * @param type The field name in the event document where the value will be stored.
     * @param value The value to store in the specified field. Can be {@code null}.
     * @param listener An optional listener that will be triggered upon completion of the operation.
     *                 It can be {@code null} if no callback is needed.
     */
    public void storeEventValueToFirestore(String eventID, String type, @Nullable Object value, @Nullable OnCompleteListener<Void> listener) {
        // Create a reference to the document
        DocumentReference docRef = firestoreDatabase.collection("events").document(eventID);

        // Prepare the data to store as a map
        Map<String, Object> data = new HashMap<>();
        data.put(type, value);

        Log.d("DatabaseManager", "Storing data in storeEventValueToFirestore: " + data); // Log the data being stored

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
     * Stores a specified value in the Firestore document for the given registration.
     * This method stores a value for a specific field (type) in the registration document, identified by
     * the provided registration ID. It uses {@link SetOptions#merge()} to ensure that only the specified field
     * is updated without overwriting other existing fields in the document. The provided listener will
     * be notified once the operation completes.
     *
     * @param registrationID The unique identifier of the registration whose document will be updated.
     * @param type The field name in the registration document where the value will be stored.
     * @param value The value to store in the specified field. Can be {@code null}.
     * @param listener An optional listener that will be triggered upon completion of the operation.
     *                 It can be {@code null} if no callback is needed.
     */
    public void storeRegistrationValueToFirestore(String registrationID, String type, @Nullable Object value, @Nullable OnCompleteListener<Void> listener) {
        // Create a reference to the document
        DocumentReference docRef = firestoreDatabase.collection("registrations").document(registrationID);

        // Prepare the data to store as a map
        Map<String, Object> data = new HashMap<>();
        data.put(type, value);

        Log.d("DatabaseManager", "Storing data in storeEventValueToFirestore: " + data); // Log the data being stored

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
     * Creates a new event in the Firestore database by storing the event details in a document.
     * This method stores various details of the event (such as title, description, address, start time,
     * end time, auto-accept status, registrations, and organizer) into the Firestore document identified
     * by the event's ID. The method uses multiple Firestore tasks to store each piece of information
     * and ensures all tasks complete successfully before invoking the provided listener.
     *
     * @param event The {@link Event} object containing the details of the event to be stored.
     * @param listener The listener to be called when all tasks have completed. If any task fails, the listener will be
     *                 notified of the failure.
     */
    public void createNewEvent(Event event, OnCompleteListener<DocumentReference> listener) {
        // Initialize a counter for the number of tasks
        int totalTasks = 9; // Number of Firestore tasks
        AtomicInteger tasksCompleted = new AtomicInteger(0); // Use AtomicInteger for thread safety

        DocumentReference referenceToItem = firestoreDatabase.collection("events").document(event.getEventID());

        databaseManager.storeEventValueToFirestore(
                event.getEventID(),
                DatabaseManager.EVENT_TITLE,
                event.getTitle(),
                (task0) -> handleTaskCompletion(task0, tasksCompleted, totalTasks, referenceToItem, listener) //, "storeUserTypeError")
        );

        databaseManager.storeEventValueToFirestore(
                event.getEventID(),
                DatabaseManager.EVENT_DESCRIPTION,
                event.getDescription(),
                (task0) -> handleTaskCompletion(task0, tasksCompleted, totalTasks, referenceToItem, listener) //, "storeUserTypeError")
        );

        databaseManager.storeEventValueToFirestore(
                event.getEventID(),
                DatabaseManager.EVENT_ADDRESS,
                event.getAddress(),
                (task0) -> handleTaskCompletion(task0, tasksCompleted, totalTasks, referenceToItem, listener) //, "storeUserTypeError")
        );

        databaseManager.storeEventValueToFirestore(
                event.getEventID(),
                DatabaseManager.EVENT_START_TIME,
                event.getStartTime(),
                (task0) -> handleTaskCompletion(task0, tasksCompleted, totalTasks, referenceToItem, listener) // , "storeUserAddressError")
        );

        databaseManager.storeEventValueToFirestore(
                event.getEventID(),
                DatabaseManager.EVENT_END_TIME,
                event.getEndTime(),
                (task0) -> handleTaskCompletion(task0, tasksCompleted, totalTasks, referenceToItem, listener) //  , "storeUserEmailError")
        );

        databaseManager.storeEventValueToFirestore(
                event.getEventID(),
                DatabaseManager.EVENT_AUTO_ACCEPT,
                event.isAutoAccept(),
                (task0) -> handleTaskCompletion(task0, tasksCompleted, totalTasks, referenceToItem, listener) // , "storeUserPhoneError")
        );

        databaseManager.storeEventValueToFirestore(
                event.getEventID(),
                DatabaseManager.EVENT_REGISTRATIONS,
                event.getRegistrations(),
                (task0) -> handleTaskCompletion(task0, tasksCompleted, totalTasks, referenceToItem, listener) // , "storeUserFirstNameError")
        );
        databaseManager.storeEventValueToFirestore(
                event.getEventID(),
                DatabaseManager.EVENT_ORGANIZER,
                event.getOrganizer(),
                (task0) -> handleTaskCompletion(task0, tasksCompleted, totalTasks, referenceToItem, listener) // , "storeUserFirstNameError")
        );
        databaseManager.storeEventValueToFirestore(
                event.getEventID(),
                DatabaseManager.EVENT_REGISTRATIONS,
                event.getRegistrations(),
                (task0) -> handleTaskCompletion(task0, tasksCompleted, totalTasks, referenceToItem, listener) // , "storeUserFirstNameError")
        );
    }

    /**
     * Deletes an event from Firestore using the provided event reference.
     * This method deletes an event document from the Firestore database using the provided
     * {@link DocumentReference} that identifies the event to be removed. It then invokes the provided
     * listener when the deletion operation is complete.
     *
     * @param eventReference The {@link DocumentReference} pointing to the event document in Firestore.
     * @param listener The listener to be notified when the deletion operation has completed. If the operation
     *                 is successful, the listener will be notified with a success status; otherwise, an error
     *                 status will be passed.
     */
    public void deleteEventFromFirestore(DocumentReference eventReference, OnCompleteListener<Void> listener) {
        deleteFromFirestore(eventReference, listener);
    }

    /**
     * Retrieves an event from Firestore by its event ID.
     * This method fetches the event document from the Firestore database using the provided event ID.
     * It extracts various fields such as the event title, description, address, start and end times,
     * auto-accept flag, registrations, and organizer. The retrieved event data is encapsulated in an
     * {@link EventOptional} object, which is passed to the provided listener upon successful retrieval.
     *
     * @param eventId The ID of the event to be retrieved from Firestore.
     * @param listener The listener to be notified once the event retrieval operation completes.
     *                 The listener will receive an {@link EventOptional} object with the event details
     *                 if the retrieval is successful, or an exception if the operation fails.
     */
    public void getEvent(String eventId, OnCompleteListener<EventOptional> listener) {
        // we want a datasnapshot of the event
        Log.d("DatabaseManager", "Getting event with ID: " + eventId);
        firestoreDatabase.collection("events").document(eventId).get().addOnCompleteListener(
                task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot snapshot = task.getResult();
                        String title = snapshot.getString(EVENT_TITLE);
                        String description = snapshot.getString(EVENT_DESCRIPTION);
                        String address = snapshot.getString(EVENT_ADDRESS);
                        Timestamp startTime = snapshot.getTimestamp(EVENT_START_TIME);
                        Timestamp endTime = snapshot.getTimestamp(EVENT_END_TIME);
                        Boolean autoAccept = snapshot.getBoolean(EVENT_AUTO_ACCEPT);
                        List<DocumentReference> registrations = (List<DocumentReference>) snapshot.get(EVENT_REGISTRATIONS);
                        DocumentReference organizer = snapshot.getDocumentReference(EVENT_ORGANIZER);
                        EventOptional eventOptional = EventOptional.oldEvent(eventId, title, description, address, startTime, endTime, autoAccept, registrations, organizer);
                        listener.onComplete(Tasks.forResult(eventOptional));
                    } else {
                        listener.onComplete(Tasks.forException(task.getException()));
                    }
                }
        );
    }

    /**
     * Retrieves an event from Firestore based on a registration reference.
     * This method fetches the event associated with a given registration reference. It first retrieves
     * the registration document from Firestore, extracts the reference to the event document, and then
     * calls the {@link #getEvent(String, OnCompleteListener)} method to retrieve the event's details
     * using the event's ID. The event data is passed to the provided listener once it is successfully
     * retrieved.
     *
     * @param registrationReference The Firestore document reference of the registration.
     * @param listener The listener to be notified once the event retrieval operation completes.
     *                 The listener will receive an {@link EventOptional} object containing the event
     *                 details if the retrieval is successful, or an exception if the operation fails.
     */
    public void getEventFromRegistration(DocumentReference registrationReference, OnCompleteListener<EventOptional> listener) {
        // we want a datasnapshot of the event
        registrationReference.get().addOnCompleteListener(
                task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot snapshot = task.getResult();
                        DocumentReference eventId = snapshot.getDocumentReference(REGISTRATION_EVENT);
                        getEvent(eventId.getId(), listener);
                        } else {
                        listener.onComplete(Tasks.forException(task.getException()));
                    }
                }
        );
    }

    /**
     * Adds an attendee to an event and updates their registration status based on the event's auto-accept setting.
     * This method checks if the event is set to auto-accept attendees. If the event has the auto-accept
     * feature enabled, it automatically accepts the attendee by changing their registration status. After
     * verifying the auto-accept status, the method either accepts the attendee or performs no further action.
     * The completion listener is notified once the operation is finished.
     *
     * @param eventReference The Firestore document reference of the event to which the attendee is being added.
     * @param registrationReference The Firestore document reference of the attendee's registration.
     * @param listener The listener to be notified once the operation is complete. If the operation is successful,
     *                 the listener will be notified with a `null` result, indicating that the operation completed.
     *                 If an error occurs, the listener will be notified with an exception.
     */
    public void addEventAttendee(DocumentReference eventReference, DocumentReference registrationReference, OnCompleteListener<Void> listener) {
        // check if the event is auto accept
        // if so, add the attendee to the event. get the event and check if the event is auto accept
        firestoreDatabase.collection("events").document(eventReference.getId()).get().addOnCompleteListener(
                task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot snapshot = task.getResult();
                        Boolean autoAccept = snapshot.getBoolean(EVENT_AUTO_ACCEPT);
                        if (autoAccept) {
                            changeAttendeeStatus(registrationReference, User.ACCEPTED);
                        }
                        listener.onComplete(Tasks.forResult(null));
                    } else {
                        listener.onComplete(Tasks.forException(task.getException()));
                    }
                }
        );
    }

    /**
     * Updates the registration status of an attendee for a specific event.
     * This method allows you to change the status of an attendee's registration in Firestore. The status
     * is updated in the registration document corresponding to the provided `registrationReference`.
     * Typically, this can be used to mark an attendee as accepted, rejected, or waitlisted.
     *
     * @param registrationReference The Firestore document reference pointing to the attendee's registration.
     *                              This reference is used to locate the registration document that will be updated.
     * @param status The new status to assign to the attendee's registration.
     *               This can be a value such as "ACCEPTED", "REJECTED", or any other status defined in the system.
     */
    public void changeAttendeeStatus(DocumentReference registrationReference, String status) {
        registrationReference.update(DatabaseManager.EVENT_REGISTRATION_STATUS, status);
    }

    /**
     * Retrieves the registration status of the current user for a specific event.
     * This method retrieves the registration document that links a user to an event and then fetches
     * the registration status for that specific user-event relationship. The status is returned via the
     * provided listener once the data is retrieved.
     *
     * @param eventReference A `DocumentReference` to the Firestore document representing the event.
     *                       This reference is used to locate the event's registration information.
     * @param listener The `OnCompleteListener<String>` listener that will be called with the registration status
     *                 once the operation completes. The status is returned as a `String`, typically values like
     *                 "ACCEPTED", "WAITLISTED", or "REJECTED".
     */
    public void getAttendanceToEvent(DocumentReference eventReference, OnCompleteListener<String> listener) {
        // take the registration that intersects both the event and the user, and return the registration status
        getRegistrationReferenceToEvent(eventReference, task -> task.getResult().get().addOnCompleteListener(task2 -> {
            if (task2.isSuccessful()) {
                DocumentSnapshot snapshot2 = task2.getResult();
                String status = snapshot2.getString(EVENT_REGISTRATION_STATUS);
                listener.onComplete(Tasks.forResult(status));
            } else {
                listener.onComplete(Tasks.forException(task2.getException()));
            }
        }));
    }

    /**
     * Updates the auto-accept status for an event.
     * This method allows you to change the auto-accept behavior of an event. When auto-accept is enabled,
     * attendees are automatically accepted into the event without needing manual approval. The method
     * updates the `EVENT_AUTO_ACCEPT` field in the event document in Firestore.
     *
     * @param eventReference A `DocumentReference` to the Firestore document representing the event whose
     *                       auto-accept status needs to be updated.
     * @param autoAccept A boolean indicating whether auto-accept should be enabled (`true`) or disabled (`false`).
     *                   If `true`, attendees will be automatically accepted into the event. If `false`,
     *                   attendees will require manual approval to be accepted.
     */
    public void changeEventAutoAccept(DocumentReference eventReference, boolean autoAccept) {
        eventReference.update(EVENT_AUTO_ACCEPT, autoAccept);
    }

    /**
     * Retrieves all events from the Firestore database.
     * This method fetches all the events stored in the "events" collection of Firestore. It processes each event
     * and filters the events based on the current user's registration status if the user is an attendee. If the
     * user is an attendee and the event contains registrations, it will only include events that have no matching
     * registration in common with the user's attendee registrations.
     *
     * @param listener A callback listener that receives the result of the operation, which will be a list of events
     *                 if the operation is successful. If the operation fails, the listener will receive an exception.
     */
    public void getEvents(OnCompleteListener<List<Event>> listener) {
        // get all the events
        firestoreDatabase.collection("events").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Event> events = new ArrayList<>();
                for (DocumentSnapshot document : task.getResult()) {
                    // get the data from the document and create an event using EventOptional.oldEvent
                    // if the user is an instance of Attendee, we need to filter based on the registrations
                    List<DocumentReference> registrations = (List<DocumentReference>) document.get(EVENT_REGISTRATIONS);
                    if (UserSession.getInstance().getUserRepresentation() instanceof Attendee attendee && registrations != null) {
                        // if the registration list has a single item in commpn with attendee.getAttendeeRegistrations() we return
                        // clone the registrations list and remove all the registrations that are not in attendee.getAttendeeRegistrations()
                        List<DocumentReference> newRegistrations = new ArrayList<>(registrations);
                        newRegistrations.retainAll(attendee.getAttendeeRegistrations());
                        if (newRegistrations.size() == 1) {
//                            Log.d("DatabaseManager", "Event has one registration");
                            continue;
                        } else if (newRegistrations.size() > 1) {
                            Log.e("DatabaseManager", "Event has multiple registrations");
                            continue;
                        }// else {
//                            Log.d("DatabaseManager", "Event has no registrations in common");
//                            // print the user registrations and the event registrations
//                            Log.d("DatabaseManager", "User event has registrations:  " + attendee.getAttendeeRegistrations());
//                            Log.d("DatabaseManager", "Event has registrations:       " + registrations);
//                        }
                    }

                    String title = document.getString(EVENT_TITLE);
                    String description = document.getString(EVENT_DESCRIPTION);
                    String address = document.getString(EVENT_ADDRESS);
                    Timestamp startTime = document.getTimestamp(EVENT_START_TIME);
                    Timestamp endTime = document.getTimestamp(EVENT_END_TIME);
                    Boolean autoAccept = document.getBoolean(EVENT_AUTO_ACCEPT);
                    DocumentReference organizer = document.getDocumentReference(EVENT_ORGANIZER);
                    EventOptional eventOptional = EventOptional.oldEvent(document.getId(), title, description, address, startTime, endTime, autoAccept, registrations, organizer);
                    events.add(eventOptional.getEvent());
                }
                listener.onComplete(Tasks.forResult(events));
            }        });
    }

    /**
     * Retrieves all events from the database and filters them based on the provided query.
     * This method fetches all events from the Firestore database and filters them based on a search query.
     * If the query is not null or empty, it checks if the event's title or description contains the query string
     * (case-insensitive). If the query is null or empty, all events will be returned without filtering.
     *
     * @param query The search query string used to filter events. If null or empty, no filtering is applied.
     * @param callback A callback interface to receive the list of events that match the query.
     *                 If no events match, an empty list will be passed to the callback.
     */
    public void getEventsThatMatchQuery(String query, EventOption.EventsCallback callback) {
        List<Event> events = new ArrayList<>();
        boolean check = query == null || query.isEmpty();

        // get all the events then filter them by the query
        getEvents(
                task -> {
                    if (task.isSuccessful()) {
                        Log.d("DatabaseManager", "Events has event unfiltered: " + task.getResult());
                        for (Event event : task.getResult()) {
                            if (event.getTitle().toLowerCase().contains(query.toLowerCase()) || check) {
                                events.add(event);
                            } else if (event.getDescription().toLowerCase().contains(query.toLowerCase())) {
                                events.add(event);
                            }
                        }
                        Log.d("DatabaseManager", "Events has event filtered: " + events);
                        callback.onDataReceived(events);
                    } else {
                        Log.e("DatabaseManager", "Error has event getting events: " + task.getException());
                        callback.onDataReceived(events);
                    }
                }
        );

    }


//---------------------------------------Registration------------------------------------------------

    /**
     * Retrieves a reference to a specific registration document in Firestore.
     * This method generates a reference to a registration document in the "registrations" collection
     * using the provided registration ID. This reference can then be used to perform further operations
     * such as fetching or updating the registration data in Firestore.
     *
     * @param registrationId The unique ID of the registration document in Firestore.
     * @return A `DocumentReference` pointing to the registration document with the specified ID.
     */
    public DocumentReference getRegistrationReference(String registrationId) {
        return firestoreDatabase.collection("registrations").document(registrationId);
    }

    /**
     * Retrieves all the registrations for a specific event.
     * This method queries the Firestore document for the specified event and retrieves the list of
     * registration document references associated with that event. The results are passed to the provided
     * listener once the operation is complete.
     *
     * @param eventReference A reference to the Firestore document of the event whose registrations are being retrieved.
     * @param listener The listener that will be notified once the registration data has been retrieved or if there is an error.
     */
    public void getAllRegistrationToEvent(DocumentReference eventReference, OnCompleteListener<List<DocumentReference>> listener) {
        // get all the registration to a specific event
        eventReference.get().addOnCompleteListener(
                task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot snapshot = task.getResult();
                        List<DocumentReference> registrations = (List<DocumentReference>) snapshot.get(EVENT_REGISTRATIONS);
                        listener.onComplete(Tasks.forResult(registrations));
                    }
                    else {
                        listener.onComplete(Tasks.forException(task.getException()));
                    }
                }
        );

    }

    /**
     * Adds a registration document reference to the list of registrations for a specific event.
     * This method updates the event document in Firestore by adding the provided registration reference
     * to the event's list of registrations. The operation is asynchronous, and the provided listener will
     * be notified once the update is complete.
     *
     * @param eventReference A reference to the Firestore document of the event where the registration is being added.
     * @param registrationReference A reference to the Firestore document of the registration that is being added to the event.
     * @param listener The listener that will be notified when the update operation completes or if there is an error.
     */
    public void addRegistrationToEvent(DocumentReference eventReference, DocumentReference registrationReference, OnCompleteListener<Void> listener) {
        eventReference.update(EVENT_REGISTRATIONS, FieldValue.arrayUnion(registrationReference)).addOnCompleteListener(listener);
    }

    /**
     * Deletes a registration document from the Firestore database and removes its references
     * from both the associated event and attendee documents.
     * This method performs the following steps:
     * <ul>
     *     <li>Retrieves the registration document from the database using the provided registration reference.</li>
     *     <li>Gets the associated event and attendee references from the registration.</li>
     *     <li>Removes the registration reference from the list of registrations in both the event and attendee documents.</li>
     *     <li>If the current user is an attendee, removes the registration reference from the attendee's local list of registrations.</li>
     *     <li>Deletes the registration document from the Firestore database.</li>
     * </ul>
     *
     * @param registrationReference A reference to the Firestore document representing the registration to be deleted.
     * @param listener The listener that will be notified once the deletion operation completes or if there is an error.
     */
    public void deleteRegistration(DocumentReference registrationReference, OnCompleteListener<Void> listener) {
        // must first get the registrations object
        getRegistration(registrationReference.getId(), task -> {
            if (task.isSuccessful()) {
                Registration registration = task.getResult();
                DocumentReference eventReference = registration.getEvent();
                DocumentReference attendeeReference = registration.getAttendee();
                // delete the reference from the event to the registration and from the attendee to the event
                attendeeReference.update(USER_ATTENDEE_REGISTRATIONS, FieldValue.arrayRemove(registrationReference));
                eventReference.update(EVENT_REGISTRATIONS, FieldValue.arrayRemove(registrationReference));
                if (UserSession.getInstance().getUserRepresentation() instanceof Attendee attendee) {
                    attendee.getAttendeeRegistrations().remove(registrationReference);
                }
                // now delete the registration object from the database
                deleteFromFirestore(registrationReference, listener);
            }
        });
    }

    /**
     * Retrieves the registration reference for the current attendee for a specific event.
     * This method checks if the current user is an attendee and attempts to find the registration
     * that intersects both the given event and the attendee's current registrations.
     * If the attendee has exactly one registration for the event, the method returns the corresponding
     * registration reference. Otherwise, an exception is thrown.
     * The method performs the following steps:
     * <ul>
     *     <li>Checks if the current user is an attendee.</li>
     *     <li>Retrieves the list of registrations for the provided event from Firestore.</li>
     *     <li>Filters the registrations to only include those that intersect with the attendee's list of registrations.</li>
     *     <li>If exactly one matching registration is found, returns the registration reference to the caller.</li>
     *     <li>If no registration or more than one registration is found, returns an error.</li>
     * </ul>
     *
     * @param eventReference A reference to the Firestore document representing the event.
     * @param listener The listener that will be notified once the operation completes, with either the registration reference or an error.
     */
    public void getRegistrationReferenceToEvent(DocumentReference eventReference, OnCompleteListener<DocumentReference> listener) {
        // take the registration that intersects both the event and the user, and return the registration status
        if (UserSession.getInstance().getUserRepresentation() instanceof Attendee attendee) {
            eventReference.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot snapshot = task.getResult();
                    List<DocumentReference> registrations = (List<DocumentReference>) snapshot.get(EVENT_REGISTRATIONS);
                    registrations.retainAll(attendee.getAttendeeRegistrations());
                    if (registrations.size() == 1) {
                        DocumentReference registrationReference = registrations.get(0);
                        listener.onComplete(Tasks.forResult(registrationReference));
                    } else {
                        listener.onComplete(Tasks.forException(new Exception("No registration found")));
                    }
                } else {
                    listener.onComplete(Tasks.forException(task.getException()));
                }
            });

        } else {
            listener.onComplete(Tasks.forException(new Exception("User is not an attendee")));
        }
    }

    /**
     * Creates a new registration in the Firestore database.
     * This method stores the details of a new registration, which includes the registration status,
     * attendee reference, and event reference. The method performs multiple Firestore operations to
     * store each of these values and ensures that all tasks are completed before notifying the listener.
     * Once all Firestore tasks are completed, the method calls the provided listener, passing the result
     * of the Firestore operations.
     *
     * @param registration The registration object that contains the details of the registration, including the status,
     *                     attendee, and event.
     * @param listener     The listener to be notified once all Firestore operations are completed, providing the document reference
     *                     of the newly created registration document.
     */
    public void createNewRegistration(Registration registration, OnCompleteListener<DocumentReference> listener) {
        int totalTasks = 3; // Number of Firestore tasks
        AtomicInteger tasksCompleted = new AtomicInteger(0); // Use AtomicInteger for thread safety

        DocumentReference referenceToItem = firestoreDatabase.collection("registrations").document(registration.getRegistrationId());

        databaseManager.storeRegistrationValueToFirestore(
                registration.getRegistrationId(),
                DatabaseManager.EVENT_REGISTRATION_STATUS,
                registration.getRegistrationStatus(),
                (task0) -> handleTaskCompletion(task0, tasksCompleted, totalTasks, referenceToItem, listener) //, "storeUserTypeError")
        );

        databaseManager.storeRegistrationValueToFirestore(
                registration.getRegistrationId(),
                DatabaseManager.REGISTRATION_ATTENDEE,
                registration.getAttendee(),
                (task0) -> handleTaskCompletion(task0, tasksCompleted, totalTasks, referenceToItem, listener) //, "storeUserTypeError")
        );

        databaseManager.storeRegistrationValueToFirestore(
                registration.getRegistrationId(),
                DatabaseManager.REGISTRATION_EVENT,
                registration.getEvent(),
                (task0) -> handleTaskCompletion(task0, tasksCompleted, totalTasks, referenceToItem, listener)
        );

    }

    /**
     * Retrieves a registration from the Firestore database based on the provided registration ID.
     * This method fetches a registration document from the "registrations" collection in Firestore.
     * It retrieves the registration status, attendee reference, and event reference from the document,
     * and then creates a `Registration` object with these values. Once the registration is successfully
     * retrieved, the listener is notified with the resulting `Registration` object.
     *
     * @param registrationId The unique ID of the registration document to retrieve.
     * @param listener       The listener to be notified with the retrieved registration object or any error encountered.
     */
    public void getRegistration(String registrationId, OnCompleteListener<Registration> listener) {
        // we want a datasnapshot of the registration
        firestoreDatabase.collection("registrations").document(registrationId).get().addOnCompleteListener(
                task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot snapshot = task.getResult();
                        String status = snapshot.getString(EVENT_REGISTRATION_STATUS);
                        DocumentReference attendee = snapshot.getDocumentReference(REGISTRATION_ATTENDEE);
                        DocumentReference event = snapshot.getDocumentReference(REGISTRATION_EVENT);
                        Registration registration = new Registration(registrationId, attendee, status, event);
                        listener.onComplete(Tasks.forResult(registration));
                    } else {
                        listener.onComplete(Tasks.forException(task.getException()));
                    }
                }
        );
    }

    /**
     * Adds a listener to a Firestore document to listen for changes to a specific field.
     * This method adds a snapshot listener to the "registrations" collection in Firestore, specifically
     * for the document identified by the provided registration ID. The listener listens for changes to the
     * document, and when the document is updated, it checks if the specified field (key) exists. If it does,
     * the provided `EventListener` is triggered with the updated document snapshot.
     *
     * @param registrationId The unique ID of the registration document to listen for changes.
     * @param eventListener  The event listener that will be called when the specified field is updated.
     * @param key            The field (key) in the document to listen for changes.
     * @return A `ListenerRegistration` object which can be used to remove the listener when no longer needed.
     */
    public ListenerRegistration addValueEventListenerToFirestoreRegistration(String registrationId, EventListener<DocumentSnapshot> eventListener, String key) {

        if (registrationId == null) {
            Log.e("DatabaseManager", "User ID is null");
            return null;
        }

        // Reference to Firestore
        DocumentReference docRef = firestoreDatabase.collection("registrations").document(registrationId);

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
                Log.e("DatabaseManager", "Document does not exist for user ID: " + registrationId);
            }
        });
    }

    //---------------------------------------MultiTaskHandler------------------------------------------------

    /**
     * Handles the completion of a Firestore task and manages task completion tracking.
     * This method is called whenever a Firestore task completes (successfully or with an error). It checks the
     * result of the task, logs any errors, and updates the count of completed tasks. Once all tasks are successfully
     * completed, it notifies the listener with the reference to the Firestore document.
     *
     * @param task                The Firestore task that has been completed.
     * @param tasksCompleted      An AtomicInteger used to safely track the number of completed tasks in a multithreaded context.
     * @param totalTasks          The total number of tasks that need to be completed. When all tasks are done, the listener is called.
     * @param referenceToItem     A reference to the Firestore document to be returned after all tasks are completed successfully.
     * @param listener            The listener that will be notified when all tasks are completed or if an error occurs.
     */
    synchronized private void handleTaskCompletion(Task<Void> task, AtomicInteger tasksCompleted, int totalTasks, DocumentReference referenceToItem, OnCompleteListener<DocumentReference> listener) {
        if (task.isSuccessful()) {
            Log.d("DatabaseManager", "Success: " + task.getResult());  // Logging success, if needed
        } else {
            // Log the error and notify the listener with the exception
            Log.e("DatabaseManager", "Error in handleTaskCompletion: " + task.getException());
            listener.onComplete(Tasks.forException(task.getException()));
            return; // Exit early if a task fails
        }
        // Increment the completed tasks count
        int completed = tasksCompleted.incrementAndGet();

        // Check if all tasks are completed
        if (completed == totalTasks) {
            Log.d("DatabaseManager", "All tasks completed successfully!");

            // Return the reference to the item after all tasks are completed successfully
            listener.onComplete(Tasks.forResult(referenceToItem));
        }
    }

    /**
     * Deletes a document from Firestore based on the provided document reference.
     * This method attempts to delete the document specified by the provided reference. If the reference is null,
     * it will immediately return with an exception. Upon successful deletion, it logs the success. If an error occurs
     * during the deletion process, it logs the error and notifies the listener with the exception.
     *
     * @param itemRef  A reference to the Firestore document to be deleted.
     * @param listener The listener that will be notified when the deletion task is completed.
     *                 If the deletion is successful, the listener will receive a result, otherwise, an exception.
     */
    private void deleteFromFirestore(DocumentReference itemRef, OnCompleteListener<Void> listener) {
        if (itemRef == null) {
            Log.e("DatabaseManager", "Reference is null");
            listener.onComplete(Tasks.forException(new Exception("Reference is null")));
            return;
        }
        itemRef.delete().addOnCompleteListener(listener).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("DatabaseManager", "Document deleted successfully");
            } else {
                Log.e("DatabaseManager", "Error deleting document: " + task.getException());
            }
        });
    }
}
