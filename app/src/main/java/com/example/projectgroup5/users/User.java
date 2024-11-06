package com.example.projectgroup5.users;

import static com.example.projectgroup5.database.DatabaseManager.USER_ORGANIZATION_NAME;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.projectgroup5.R;
import com.example.projectgroup5.database.DatabaseManager;

public abstract class User {
    public final static int USER_TYPE_ORGANIZER = 1;
    public final static int USER_TYPE_ATTENDEE = 2;
    public final static int USER_TYPE_ADMIN = 0;
    public static final int REJECTED = 2;
    public static final int ACCEPTED = 1;
    public static final int WAITLISTED = 0;
    private final String userId;
    private String userFirstName;
    private String userLastName;
    private String userEmail;
    private long userPhoneNumber;
    private String userAddress;
    private int userRegistrationState;
    private int userType;

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
    public static User newUser(String userId, int userType) {
        final User user;
        if (userType == USER_TYPE_ORGANIZER) {
            user = new Organizer(userId);
        } else if (userType == USER_TYPE_ATTENDEE) {
            user = new Attendee(userId);
        } else if (userType == USER_TYPE_ADMIN) {
            user = new Administrator(userId);
        } else {
            return null;
        }
        DatabaseManager.getDatabaseManager().getAllUserDataFromFirestore(userId, value ->{
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
                    user.setUserRegistrationState((int) (long) value.get(DatabaseManager.USER_REGISTRATION_STATE));
                }
                user.setUserType(userType);
            }
        });
        return user;
    }

    private void setUserRegistrationState(int userRegistrationState) {
        this.userRegistrationState = userRegistrationState;
    }

    public int getUserRegistrationState() {
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
    public void setUserType(int userType) {
        this.userType = userType;
    }

    /**
     * Retrieves the type of the user.
     *
     * @return The current user type, represented as an integer.
     */
    public int getUserType() {
        return userType;
    }

    /**
     * Sets the address of the user.
     * <p>
     * This method updates the user's address in the current user representation.
     *
     * @param userAddress The address to be set for the user.
     */
    private void setUserAddress(String userAddress) {
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
     * Adds a user entry to the specified layout, populating it with user data from the database.
     * <p>
     * This method inflates a custom view for a user entry and retrieves user data from the
     * Firebase database. It sets the values for various TextViews and manages the visibility
     * of buttons based on the user's registration state. Additionally, it sets up click listeners
     * for the accept and reject buttons.
     *
     * @param layout  The LinearLayout to which the user entry will be added.
     * @param context The context used to inflate the view and access resources.
     */
    public void addUserToLayout(LinearLayout layout, Context context) {

        View customView = LayoutInflater.from(context).inflate(R.layout.account_entry, layout, false);
        customView.setId(userId.hashCode());
        // set get the data from firebase if possible
        DatabaseManager.getDatabaseManager().getAllUserDataFromFirestore(userId, value -> {
            if (value != null) {
                if (value.containsKey(DatabaseManager.USER_ADDRESS)) {
                    setUserAddress(value.get(DatabaseManager.USER_ADDRESS).toString());
                    TextView userAddressTextView = customView.findViewById(R.id.homeAddressEntry);
                    userAddressTextView.setText(userAddress);
                }
                if (value.containsKey(USER_ORGANIZATION_NAME)) {
                    if (this instanceof Organizer organizer) {
                        organizer.setUserOrganizationName(value.get(USER_ORGANIZATION_NAME).toString());
                        TextView userOrganizationNameTextView = customView.findViewById(R.id.organizationNameEntry);
                        userOrganizationNameTextView.setVisibility(View.VISIBLE);
                        userOrganizationNameTextView.setText(organizer.getUserOrganizationName());
                    }
                }
                if (value.containsKey(DatabaseManager.USER_ADDRESS)) {
                    setUserAddress(value.get(DatabaseManager.USER_ADDRESS).toString());
                    TextView userAddressTextView = customView.findViewById(R.id.homeAddressEntry);
                    userAddressTextView.setText(userAddress);
                }
                if (value.containsKey(DatabaseManager.USER_FIRST_NAME)) {
                    setUserFirstName(value.get(DatabaseManager.USER_FIRST_NAME).toString());
                    TextView userFirstNameTextView = customView.findViewById(R.id.firstNameEntry);
                    userFirstNameTextView.setText(userFirstName);
                }
                if (value.containsKey(DatabaseManager.USER_LAST_NAME)) {
                    setUserLastName(value.get(DatabaseManager.USER_LAST_NAME).toString());
                    TextView userLastNameTextView = customView.findViewById(R.id.lastNameEntry);
                    userLastNameTextView.setText(userLastName);
                }
                if (value.containsKey(DatabaseManager.USER_EMAIL)) {
                    setUserEmail(value.get(DatabaseManager.USER_EMAIL).toString());
                    TextView userEmailTextView = customView.findViewById(R.id.emailAddressEntry);
                    userEmailTextView.setText(userEmail);
                }
                if (value.containsKey(DatabaseManager.USER_PHONE)) {
                    setUserPhoneNumber(Long.valueOf(value.get(DatabaseManager.USER_PHONE).toString().replace("\"", "")));
                    TextView userPhoneNumberTextView = customView.findViewById(R.id.phoneNumberEntry);
                    userPhoneNumberTextView.setText(userPhoneNumber + "");
                }
                if (value.containsKey(DatabaseManager.USER_REGISTRATION_STATE)) {
                    int userRegistrationState = (int) (long) value.get(DatabaseManager.USER_REGISTRATION_STATE);
                    switch (userRegistrationState) {
                        case REJECTED: {
                            Button rejectButton = customView.findViewById(R.id.rejectUserButton);
                            rejectButton.setVisibility(View.GONE);
                            Button acceptButton = customView.findViewById(R.id.acceptUserButton);
                            acceptButton.setVisibility(View.VISIBLE);
                            break;
                        }
                        case ACCEPTED: {
                            Button rejectButton = customView.findViewById(R.id.rejectUserButton);
                            rejectButton.setVisibility(View.GONE);
                            Button acceptButton = customView.findViewById(R.id.acceptUserButton);
                            acceptButton.setVisibility(View.GONE);
                            break;
                        }
                        case WAITLISTED: {
                            Button rejectButton = customView.findViewById(R.id.rejectUserButton);
                            rejectButton.setVisibility(View.VISIBLE);
                            Button acceptButton = customView.findViewById(R.id.acceptUserButton);
                            acceptButton.setVisibility(View.VISIBLE);
                            break;
                        }
                    }
                }


            }
        });

        Button rejectButton = customView.findViewById(R.id.rejectUserButton);
        rejectButton.setOnClickListener(v -> {
            removeUserFromLayout(layout);
            // Handle reject button click
            DatabaseManager.getDatabaseManager().storeUserValueToFirestore(userId, DatabaseManager.USER_REGISTRATION_STATE, REJECTED, null);
        });
        Button acceptButton = customView.findViewById(R.id.acceptUserButton);
        acceptButton.setOnClickListener(v -> {
            removeUserFromLayout(layout);
            // Handle accept button click
            DatabaseManager.getDatabaseManager().storeUserValueToFirestore(userId, DatabaseManager.USER_REGISTRATION_STATE, ACCEPTED, null);
        });


        layout.addView(customView);

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
}
