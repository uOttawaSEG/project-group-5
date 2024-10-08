package com.example.projectgroup5.users;

import android.location.Address;

public abstract class User {
    String userFirstName;
    String userLastName;
    String userEmail;
    String userPasswordHash;
    String userPhoneNumber;
    Address userAddress;

    abstract void SaveLoginInfo();
}
