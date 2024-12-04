package com.example.projectgroup5.database;

import android.telephony.PhoneNumberUtils;

public class FieldValidator {
    /**
     * Checks if the provided password is invalid based on its length.
     * This method validates if the given password meets the minimum length requirement. The password is considered invalid
     * if its length is less than 6 characters.
     *
     * @param password The password string to be checked.
     * @return `true` if the password length is less than 6 characters, otherwise `false`.
     */
    public static boolean checkIfPasswordIsInvalid(String password) {
        return password.length() < 6;
    }

    /**
     * Checks if the provided phone number is invalid.
     * This method validates a phone number by checking if it is a globally valid phone number
     * and if its length is exactly 10 digits. The phone number is considered invalid if it does
     * not pass these checks.
     *
     * @param phoneNumber The phone number string to be checked.
     * @return `true` if the phone number is not globally valid or does not have exactly 10 digits,
     *         otherwise `false`.
     */
    public static boolean checkIfPhoneNumberIsInvalid(String phoneNumber) {
        return !PhoneNumberUtils.isGlobalPhoneNumber(phoneNumber) || phoneNumber.length() != 10;
    }

    /**
     * Checks if the given string contains characters other than alphabets.
     * This method checks if the input string contains anything other than alphabetic characters
     * (both lowercase and uppercase). If the string contains any non-alphabetic characters, it
     * returns `true`; otherwise, it returns `false`.
     *
     * @param string The string to be checked.
     * @return `true` if the string contains characters other than alphabets, otherwise `false`.
     */
    public static boolean checkIfIsNotAlphabet(String string) {
        return !string.matches("[a-zA-Z]+");
    }

    /**
     * Checks if the given string contains characters other than alphabets and spaces.
     * This method checks if the input string contains anything other than alphabetic characters
     * (both lowercase and uppercase) or spaces. If the string contains any non-alphabetic or
     * non-space characters, it returns `true`; otherwise, it returns `false`.
     *
     * @param string The string to be checked.
     * @return `true` if the string contains characters other than alphabets or spaces, otherwise `false`.
     */
    public static boolean checkIfIsNotAlphabetWithSpaces(String string) {
        return !string.matches("[a-zA-Z\\s]+");
    }
}
