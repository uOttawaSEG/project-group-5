package com.example.projectgroup5.users;

import static com.example.projectgroup5.users.UserSession.*;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.projectgroup5.R;

import java.util.Map;

public abstract class User {
    String userId;
    String userFirstName;
    String userLastName;
    String userEmail;
    String userPhoneNumber;
    String userAddress;
    String userOrganizationName;

    public String getUserFirstName() {
        return userFirstName;
    }

    public void setUserFirstName(String userFirstName) {
        this.userFirstName = userFirstName;
    }

    public String getUserLastName() {
        return userLastName;
    }

    public void setUserLastName(String userLastName) {
        this.userLastName = userLastName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getUserPhoneNumber() {
        return userPhoneNumber;
    }

    public void setUserPhoneNumber(String userPhoneNumber) {
        this.userPhoneNumber = userPhoneNumber;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public String getUserOrganizationName() {
        return userOrganizationName;
    }

    public void setUserOrganizationName(String userOrganizationName) {
        this.userOrganizationName = userOrganizationName;
    }


    private int userType;

    public User(String userId) {
        this.userId = userId;
    }

    public void setUserType(int userType) {
        this.userType = userType;
    }

    public int getUserType() {
        return userType;
    }

    abstract void SaveLoginInfo();

    public interface FirebaseCallback<T> {
        void onCallback(T value);
    }

    public static User newUser(String userId, int userType) {
        final User user;
        if (userType == USER_TYPE_ORGANIZER) {
            user = new Organizer(userId);
        } else if (userType == USER_TYPE_USER) {
            user = new Attendee(userId);
        } else if (userType == USER_TYPE_ADMIN) {
            user = new Administrator(userId);
        } else {
            return null;
        }

        return user;
    }

    private void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public void setUserEmail(String email) {
        this.userEmail = email;
    }

    public void addUserToLayout(LinearLayout layout, Context context) {

        View customView = LayoutInflater.from(context).inflate(R.layout.account_entry, layout, false);
        customView.setId(userId.hashCode());
        // set get the data from firebase if possible
        DatabaseManager.getDatabaseManager().getAllUserData(userId, new UserSession.FirebaseCallback<Map<String, Object>>() {
            @Override
            public void onCallback(Map<String, Object> value) {
                if (value != null) {
                    if (value.containsKey(USER_ADDRESS)) {
                        setUserAddress(value.get(USER_ADDRESS).toString());
                        TextView userAddressTextView = customView.findViewById(R.id.homeAddressEntry);
                        userAddressTextView.setText(userAddress);
                    }
                    if (value.containsKey(USER_ORGANIZATION_NAME)) {
                        setUserOrganizationName(value.get(USER_ORGANIZATION_NAME).toString());
                        TextView userOrganizationNameTextView = customView.findViewById(R.id.organizationNameEntry);
                        userOrganizationNameTextView.setVisibility(View.VISIBLE);
                        userOrganizationNameTextView.setText(userOrganizationName);
                    }
                    if (value.containsKey(USER_ADDRESS)) {
                        setUserAddress(value.get(USER_ADDRESS).toString());
                        TextView userAddressTextView = customView.findViewById(R.id.homeAddressEntry);
                        userAddressTextView.setText(userAddress);
                    }
                    if (value.containsKey(USER_FIRST_NAME)) {
                        setUserFirstName(value.get(USER_FIRST_NAME).toString());
                        TextView userFirstNameTextView = customView.findViewById(R.id.firstNameEntry);
                        userFirstNameTextView.setText(userFirstName);
                    }
                    if (value.containsKey(USER_LAST_NAME)) {
                        setUserLastName(value.get(USER_LAST_NAME).toString());
                        TextView userLastNameTextView = customView.findViewById(R.id.lastNameEntry);
                        userLastNameTextView.setText(userLastName);
                    }
                    if (value.containsKey(USER_EMAIL)) {
                        setUserEmail(value.get(USER_EMAIL).toString());
                        TextView userEmailTextView = customView.findViewById(R.id.emailAddressEntry);
                        userEmailTextView.setText(userEmail);
                    }
                    if (value.containsKey(USER_PHONE)) {
                        setUserPhoneNumber(value.get(USER_PHONE).toString());
                        TextView userPhoneNumberTextView = customView.findViewById(R.id.phoneNumberEntry);
                        userPhoneNumberTextView.setText(userPhoneNumber);
                    }
                    if (value.containsKey(USER_REGISTRATION_STATE)) {
                        int userRegistrationState = (int) (long) value.get(USER_REGISTRATION_STATE);
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
            }
        });

        Button rejectButton = customView.findViewById(R.id.rejectUserButton);
        rejectButton.setOnClickListener(v -> {
            removeUserFromLayout(layout);
            // Handle reject button click
            DatabaseManager.getDatabaseManager().storeValue(userId, USER_REGISTRATION_STATE, REJECTED, null);
        });
        Button acceptButton = customView.findViewById(R.id.acceptUserButton);
        acceptButton.setOnClickListener(v -> {
            removeUserFromLayout(layout);
            // Handle accept button click
            DatabaseManager.getDatabaseManager().storeValue(userId, USER_REGISTRATION_STATE, ACCEPTED, null);
        });


        layout.addView(customView);

    }

    private void removeUserFromLayout(LinearLayout layout) {
        int viewId = userId.hashCode();
        View viewToRemove = layout.findViewById(viewId);
        if (viewToRemove != null) {
            layout.removeView(viewToRemove);
        }
    }
}
