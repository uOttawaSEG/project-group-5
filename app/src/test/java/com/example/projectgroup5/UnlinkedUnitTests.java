package com.example.projectgroup5;

import org.junit.Test;

import static org.junit.Assert.*;

import com.example.projectgroup5.database.FieldValidator;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class UnlinkedUnitTests {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
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