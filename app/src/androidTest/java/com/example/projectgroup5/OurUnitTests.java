package com.example.projectgroup5;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import com.example.projectgroup5.database.FieldValidator;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class OurUnitTests {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.projectgroup5", appContext.getPackageName());
    }

    @Test
    public void testCheckIfPasswordIsInvalid_validPassword() {
        // Test a valid password (length > 6)
        String password = "validpwd";
        assertFalse("Password should be valid", FieldValidator.checkIfPasswordIsInvalid(password));
    }

    @Test
    public void testCheckIfPasswordIsInvalid_invalidPassword() {
        // Test an invalid password (length <= 6)
        String password = "short";
        assertTrue("Password should be invalid", FieldValidator.checkIfPasswordIsInvalid(password));
    }

    @Test
    public void testCheckIfPhoneNumberIsInvalid_validPhoneNumber() {
        // Test a valid phone number (10 digits and globally valid)
        String phoneNumber = "1234567890";
        assertFalse("Phone number should be valid", FieldValidator.checkIfPhoneNumberIsInvalid(phoneNumber));
    }

    @Test
    public void testCheckIfPhoneNumberIsInvalid_invalidPhoneNumber_length() {
        // Test invalid phone number (less than or more than 10 digits)
        String phoneNumber = "12345";
        assertTrue("Phone number should be invalid", FieldValidator.checkIfPhoneNumberIsInvalid(phoneNumber));
    }

    @Test
    public void testCheckIfPhoneNumberIsInvalid_invalidPhoneNumber_format() {
        // Test invalid phone number (invalid format, not a globally valid number)
        String phoneNumber = "abc1234567";
        assertTrue("Phone number should be invalid", FieldValidator.checkIfPhoneNumberIsInvalid(phoneNumber));
    }

    @Test
    public void testCheckIfIsNotAlphabet_alphabetString() {
        // Test string that contains only alphabets
        String input = "Hello";
        assertFalse("String should be valid alphabet", FieldValidator.checkIfIsNotAlphabet(input));
    }

    @Test
    public void testCheckIfIsNotAlphabet_nonAlphabetString() {
        // Test string that contains non-alphabet characters
        String input = "Hello123";
        assertTrue("String should be invalid alphabet", FieldValidator.checkIfIsNotAlphabet(input));
    }

    @Test
    public void testCheckIfIsNotAlphabetWithSpaces_alphabetWithSpaces() {
        // Test string that contains only alphabets and spaces
        String input = "Hello World";
        assertFalse("String should be valid alphabet with spaces", FieldValidator.checkIfIsNotAlphabetWithSpaces(input));
    }

    @Test
    public void testCheckIfIsNotAlphabetWithSpaces_nonAlphabetWithSpaces() {
        // Test string that contains non-alphabet characters, even with spaces
        String input = "Hello123 World";
        assertTrue("String should be invalid alphabet with spaces", FieldValidator.checkIfIsNotAlphabetWithSpaces(input));
    }

    @Test
    public void testCheckIfIsNotAlphabetWithSpaces_invalidStringWithNoSpaces() {
        // Test a string that contains non-alphabet characters and no spaces
        String input = "Hello@World";
        assertTrue("String should be invalid alphabet with spaces", FieldValidator.checkIfIsNotAlphabetWithSpaces(input));
    }

}