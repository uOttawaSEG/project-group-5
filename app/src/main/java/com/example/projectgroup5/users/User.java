package com.example.projectgroup5.users;

import static com.example.projectgroup5.users.UserSession.USER_ADDRESS;
import static com.example.projectgroup5.users.UserSession.USER_EMAIL;
import static com.example.projectgroup5.users.UserSession.USER_FIRST_NAME;
import static com.example.projectgroup5.users.UserSession.USER_LAST_NAME;
import static com.example.projectgroup5.users.UserSession.USER_ORGANIZATION_NAME;
import static com.example.projectgroup5.users.UserSession.USER_PHONE;
import static com.example.projectgroup5.users.UserSession.USER_TYPE_ADMIN;
import static com.example.projectgroup5.users.UserSession.USER_TYPE_ORGANIZER;
import static com.example.projectgroup5.users.UserSession.USER_TYPE_USER;

import android.content.Context;
import android.location.Address;
import android.util.Log;
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
//        TextView userFirstNameTextView = customView.findViewById(R.id.firstNameEntry);
//        userFirstNameTextView.setText(userFirstName);
//        TextView userLastNameTextView = customView.findViewById(R.id.lastNameEntry);
//        userLastNameTextView.setText(userLastName);
//        TextView userEmailTextView = customView.findViewById(R.id.emailAddressEntry);
//        userEmailTextView.setText(userEmail);
//        TextView userPhoneNumberTextView = customView.findViewById(R.id.phoneNumberEntry);
//        userPhoneNumberTextView.setText(userPhoneNumber);
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

                }
            }
        });
//        TextView userAddressTextView = customView.findViewById(R.id.homeAddressEntry);
//        userAddressTextView.setText(userAddress);


        Button rejectButton = customView.findViewById(R.id.rejectUserButton);
        rejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeUserFromLayout(layout);
                // Handle reject button click
            }
        });
        Button acceptButton = customView.findViewById(R.id.acceptUserButton);
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeUserFromLayout(layout);
                // Handle accept button click
            }
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
