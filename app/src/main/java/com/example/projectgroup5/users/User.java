package com.example.projectgroup5.users;

import static com.example.projectgroup5.database.DatabaseManager.USER_ORGANIZATION_NAME;
import static com.example.projectgroup5.database.DatabaseManager.USER_REGISTRATION_STATE;

import android.util.Log;

import com.example.projectgroup5.database.DatabaseManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.List;

public abstract class User {
    public final static String USER_TYPE_ORGANIZER = "Organizer";
    public final static String USER_TYPE_ATTENDEE = "Attendee";
    public final static String USER_TYPE_ADMIN = "Admin";
    public static final String REJECTED = "Rejected";
    public static final String ACCEPTED = "Accepted";
    public static final String WAITLISTED = "Waitlisted";
    private final String userId;
    private String userFirstName;
    private String userLastName;
    private String userEmail;
    private long userPhoneNumber;
    private String userAddress;
    private String userRegistrationState;
    private String userType;

    /**
     * Creates a new User instance with the specified user ID.
     *
     * @param userId The unique identifier for the user.
     */
    protected User(String userId) {
        this.userId = userId;
    }

    /**
     * Creates a new User instance based on the specified user type.
     * <p>
     * This static method generates a user of a specific type (Organizer, Attendee, or Administrator)
     * based on the provided user ID and user type.
     *
     * @param userId The unique identifier for the user.
     *               one of the predefined user types:
     *               <ul>
     *                 <li><code>USER_TYPE_ORGANIZER</code></li>
     *                 <li><code>USER_TYPE_USER</code></li>
     *                 <li><code>USER_TYPE_ADMIN</code></li>
     *               </ul>
     */
    public static void newUserFromDatabase(String userId, OnCompleteListener<User> listener) {
        DatabaseManager.getDatabaseManager().getUserDataFromFirestore(userId, DatabaseManager.USER_TYPE, userType -> {
            final User user;
            switch (userType.toString()) {
                case USER_TYPE_ORGANIZER -> user = new Organizer(userId);
                case USER_TYPE_ATTENDEE -> user = new Attendee(userId);
                case USER_TYPE_ADMIN -> user = new Administrator(userId);
                default -> {
                    listener.onComplete(Tasks.forException(new Exception("Failed to create user from database, user ID: " + userId + " user type: " + userType)));
                    return;

                }
            }

            Log.d("User", "User data at database fetch: " + userId);
            DatabaseManager.getDatabaseManager().getAllUserDataFromFirestore(userId, value -> {
                Log.d("User", "User successful data at database fetch: " + value);
                if (value != null) {
                    Log.d("User", "User data done at database fetch: " + value);
                    if (value.containsKey(DatabaseManager.USER_FIRST_NAME)) {
                        user.setUserFirstName(value.get(DatabaseManager.USER_FIRST_NAME).toString());
                    }
                    if (value.containsKey(DatabaseManager.USER_LAST_NAME)) {
                        user.setUserLastName(value.get(DatabaseManager.USER_LAST_NAME).toString());
                    }
                    if (value.containsKey(DatabaseManager.USER_EMAIL)) {
                        user.setUserEmail(value.get(DatabaseManager.USER_EMAIL).toString());
                    }
                    if (value.containsKey(DatabaseManager.USER_PHONE)) {
                        user.setUserPhoneNumber(Long.parseLong(value.get(DatabaseManager.USER_PHONE).toString().replace("\"", "")));
                    }
                    if (value.containsKey(DatabaseManager.USER_ADDRESS)) {
                        user.setUserAddress(value.get(DatabaseManager.USER_ADDRESS).toString());
                    }
                    if (value.containsKey(USER_ORGANIZATION_NAME)) {
                        if (user instanceof Organizer organizer)
                            organizer.setUserOrganizationName(value.get(USER_ORGANIZATION_NAME).toString());
                    }
                    if (value.containsKey(DatabaseManager.USER_ORGANIZER_EVENTS)) {
                        if (user instanceof Organizer organizer) {
                            // try the cast to list of document references
                            Log.d("User", "User organizer events at database fetch: " + value.get(DatabaseManager.USER_ORGANIZER_EVENTS).toString());
                            ArrayList<DocumentReference> events = (ArrayList<DocumentReference>) value.get(DatabaseManager.USER_ORGANIZER_EVENTS);
                            Log.d("User", "After organizer events cast: " + events.toString());
                            organizer.setOrganizerEvents(events);
                            Log.d("User", "After organizer events set: " + organizer.getOrganizerEvents().toString());
                        }
                    }
                    if (value.containsKey(USER_REGISTRATION_STATE)) {
                        Log.d("User", "User registration state at database fetch: " + value.get(USER_REGISTRATION_STATE).toString());
                        user.setUserRegistrationState(value.get(USER_REGISTRATION_STATE).toString());
                        Log.d("User", "User registration state after fetch: " + user.getUserRegistrationState());
                    }
                    if (value.containsKey(DatabaseManager.USER_ATTENDEE_REGISTRATIONS)) {
                        if (user instanceof Attendee attendee) {
                            // try the cast to list of document references
                            List<DocumentReference> registrations = (List<DocumentReference>) value.get(DatabaseManager.USER_ATTENDEE_REGISTRATIONS);

                            attendee.setAttendeeRegistrations(registrations);
                        }
                    }
                    user.setUserType(userType.toString());

                    listener.onComplete(Tasks.forResult(user));
                }
            });
        });
    }

    /**
     * Creates a new user of the specified type with the provided details.
     * This method instantiates a user object based on the specified user type and sets
     * the user's details including name, email, phone number, address, and organization
     * (if applicable). The user type determines which subclass of the `User` class is
     * instantiated.
     *
     * @param userType The type of the user. This can be one of the following:
     *                 - `USER_TYPE_ORGANIZER`
     *                 - `USER_TYPE_ATTENDEE`
     *                 - `USER_TYPE_ADMIN`
     * @param firstName The first name of the user.
     * @param lastName The last name of the user.
     * @param email The email address of the user.
     * @param phoneNumber The phone number of the user.
     * @param address The address of the user.
     * @param organisation The organization name for organizers. Can be null for non-organizer users.
     *
     * @return A new `User` object corresponding to the specified user type, or `null` if the user type is invalid.
     */
    public static User newUser(String userType, String firstName, String lastName, String email, long phoneNumber, String address, String organisation) {
        final User user;
        switch (userType) {
            case USER_TYPE_ORGANIZER -> {
                user = new Organizer(null);
                ((Organizer) user).setUserOrganizationName(organisation);
            }
            case USER_TYPE_ATTENDEE -> user = new Attendee(null);
            case USER_TYPE_ADMIN -> user = new Administrator(null);
            default -> {
                return null;
            }
        }
        user.setUserFirstName(firstName);
        user.setUserLastName(lastName);
        user.setUserEmail(email);
        user.setUserPhoneNumber(phoneNumber);
        user.setUserAddress(address);
        return user;
    }


    /**
     * Returns the current registration state of the user.
     *
     * @return The registration state of the user (e.g., "accepted", "pending", "rejected").
     */
    public String getUserRegistrationState() {
        return userRegistrationState;
    }

    /**
     * Sets the registration state of the user.
     * <p>
     * This method updates the user's registration state, such as "accepted", "pending", or "rejected".
     *
     * @param userRegistrationState The registration state to be set for the user.
     */
    public void setUserRegistrationState(String userRegistrationState) {
        this.userRegistrationState = userRegistrationState;
    }

    /**
     * Sets the first name of the user.
     * <p>
     * This method updates the user's first name in the current user representation.
     *
     * @param userFirstName The first name to be set for the user.
     */
    public void setUserFirstName(String userFirstName) {
        this.userFirstName = userFirstName;
    }

    /**
     * Sets the last name of the user.
     * <p>
     * This method updates the user's last name in the current user representation.
     *
     * @param userLastName The last name to be set for the user.
     */
    public void setUserLastName(String userLastName) {
        this.userLastName = userLastName;
    }

    /**
     * Sets the phone number of the user.
     * <p>
     * This method updates the user's phone number in the current user representation.
     *
     * @param userPhoneNumber The phone number to be set for the user.
     */
    public void setUserPhoneNumber(long userPhoneNumber) {
        this.userPhoneNumber = userPhoneNumber;
    }

    /**
     * Retrieves the type of the user.
     *
     * @return The current user type, represented as an integer.
     */
    public String getUserType() {
        return userType;
    }

    /**
     * Sets the type of the user.
     * <p>
     * This method updates the user's type, which may represent different roles or permissions.
     *
     * @param userType The type to be set for the user, typically represented as an integer.
     */
    public void setUserType(String userType) {
        this.userType = userType;
    }

    /**
     * Retrieves the email address of the user.
     *
     * @return The email address of the user, represented as a String.
     */
    public String getUserEmail() {
        return userEmail;
    }

    /**
     * Sets the email address of the user.
     * <p>
     * This method updates the user's email address in the current user representation.
     *
     * @param email The email address to be set for the user.
     */
    public void setUserEmail(String email) {
        this.userEmail = email;
    }

    /**
     * Returns the unique identifier for the user.
     *
     * @return The user ID.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Returns the address of the user.
     *
     * @return The user's address.
     */
    public String getUserAddress() {
        return userAddress;
    }

    /**
     * Sets the address of the user.
     * <p>
     * This method updates the user's address in the current user representation.
     *
     * @param userAddress The address to be set for the user.
     */
    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    /**
     * Returns the first name of the user.
     *
     * @return The user's first name.
     */
    public String getFirstName() {
        return userFirstName;
    }

    /**
     * Returns the last name of the user.
     *
     * @return The user's last name.
     */
    public String getLastName() {
        return userLastName;
    }

    /**
     * Returns the phone number of the user.
     *
     * @return The user's phone number.
     */
    public long getPhoneNumber() {
        return userPhoneNumber;
    }
}
