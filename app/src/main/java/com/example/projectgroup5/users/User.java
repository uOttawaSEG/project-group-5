package com.example.projectgroup5.users;

import static com.example.projectgroup5.database.DatabaseManager.USER_ORGANIZATION_NAME;

import android.view.View;
import android.widget.LinearLayout;

import com.example.projectgroup5.database.DatabaseManager;

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
     * based on the provided user ID and user type. If the user type does not match any known types,
     * it returns null.
     *
     * @param userId   The unique identifier for the user.
     * @param userType An integer representing the type of user to be created. This should match
     *                 one of the predefined user types:
     *                 <ul>
     *                   <li><code>USER_TYPE_ORGANIZER</code></li>
     *                   <li><code>USER_TYPE_USER</code></li>
     *                   <li><code>USER_TYPE_ADMIN</code></li>
     *                 </ul>
     * @return A new User instance of the specified type, or null if the user type is invalid.
     */
    public static User newUserFromDatabase(String userId, String userType) {
        final User user;
        if (userType.equals(USER_TYPE_ORGANIZER)) {
            user = new Organizer(userId);
        } else if (userType.equals(USER_TYPE_ATTENDEE)) {
            user = new Attendee(userId);
        } else if (userType.equals(USER_TYPE_ADMIN)) {
            user = new Administrator(userId);
        } else {
            return null;
        }
        DatabaseManager.getDatabaseManager().getAllUserDataFromFirestore(userId, value -> {
            if (value != null) {
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
                    user.setUserPhoneNumber(Long.valueOf(value.get(DatabaseManager.USER_PHONE).toString().replace("\"", "")));
                }
                if (value.containsKey(DatabaseManager.USER_ADDRESS)) {
                    user.setUserAddress(value.get(DatabaseManager.USER_ADDRESS).toString());
                }
                if (value.containsKey(USER_ORGANIZATION_NAME)) {
                    if (user instanceof Organizer organizer)
                        organizer.setUserOrganizationName(value.get(USER_ORGANIZATION_NAME).toString());
                }
                if (value.containsKey(DatabaseManager.USER_REGISTRATION_STATE)) {
                    user.setUserRegistrationState(value.get(DatabaseManager.USER_REGISTRATION_STATE).toString());
                }
                user.setUserType(userType);
            }
        });
        return user;
    }

    //TODO: add documentation
    public static User newUser(String userType, String firstName, String lastName, String email, long phoneNumber, String address, String organisation) {
        final User user;
        if (userType.equals(USER_TYPE_ORGANIZER)) {
            user = new Organizer(null);
            ((Organizer) user).setUserOrganizationName(organisation);
        } else if (userType.equals(USER_TYPE_ATTENDEE)) {
            user = new Attendee(null);
        } else if (userType.equals(USER_TYPE_ADMIN)) {
            user = new Administrator(null);
        } else {
            return null;
        }
        user.setUserFirstName(firstName);
        user.setUserLastName(lastName);
        user.setUserEmail(email);
        user.setUserPhoneNumber(phoneNumber);
        user.setUserAddress(address);
        return user;
    }

    public void setUserRegistrationState(String userRegistrationState) {
        this.userRegistrationState = userRegistrationState;
    }

    public String getUserRegistrationState() {
        return userRegistrationState;
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
     * Retrieves the type of the user.
     *
     * @return The current user type, represented as an integer.
     */
    public String getUserType() {
        return userType;
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
     * Sets the email address of the user.
     * <p>
     * This method updates the user's email address in the current user representation.
     *
     * @param email The email address to be set for the user.
     */
    public void setUserEmail(String email) {
        this.userEmail = email;
    }

    public String getUserEmail() {
        return userEmail;
    }

    /**
     * Removes a user entry view from the specified layout.
     * <p>
     * This method searches for the user entry view associated with the user's ID (hashed)
     * and removes it from the given LinearLayout if it exists.
     *
     * @param layout The LinearLayout from which the user entry will be removed.
     */
    private void removeUserFromLayout(LinearLayout layout) {
        int viewId = userId.hashCode();
        View viewToRemove = layout.findViewById(viewId);
        if (viewToRemove != null) {
            layout.removeView(viewToRemove);
        }
    }

    public String getUserId() {
        return userId;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public String getFirstName() {
        return userFirstName;
    }

    public String getLastName() {
        return userLastName;
    }

    public long getPhoneNumber() {
        return userPhoneNumber;
    }
}
