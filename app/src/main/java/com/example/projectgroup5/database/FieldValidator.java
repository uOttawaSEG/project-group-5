package com.example.projectgroup5.database;

import android.telephony.PhoneNumberUtils;

public class FieldValidator {
    public static boolean checkIfPasswordIsInvalid(String password) {
        return password.length() < 6;
    }

    public static boolean checkIfPhoneNumberIsInvalid(String phoneNumber) {
        return !PhoneNumberUtils.isGlobalPhoneNumber(phoneNumber) || phoneNumber.length() != 10;
    }

    public static boolean checkIfIsNotAlphabet(String string) {
        return !string.matches("[a-zA-Z]+");
    }

    public static boolean checkIfIsNotAlphabetWithSpaces(String string) {
        return !string.matches("[a-zA-Z\\s]+");
    }


}
