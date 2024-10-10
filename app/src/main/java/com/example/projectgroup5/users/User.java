package com.example.projectgroup5.users;

import static com.example.projectgroup5.users.UserSession.USER_TYPE_ORGANIZER;
import static com.example.projectgroup5.users.UserSession.USER_TYPE_USER;

import android.location.Address;

public abstract class User {
    String userId;
    String userFirstName;
    String userLastName;
    String userEmail;
    String userPasswordHash;
    String userPhoneNumber;
    Address userAddress;
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

    static User newUser(String userId, int userType) {
        if (userType == USER_TYPE_ORGANIZER) {
            return new Organizer(userId);
        } else if (userType == USER_TYPE_USER) {
            return new Attendee(userId);
        }
        return null;
    }

    public void setUserEmail(String email) {
        this.userEmail = email;
    }
}
