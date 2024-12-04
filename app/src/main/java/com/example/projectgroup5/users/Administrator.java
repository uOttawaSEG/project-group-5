package com.example.projectgroup5.users;

/**
 * Represents an administrator user in the system.
 * The `Administrator` class extends from the `User` class and serves as a specialized
 * user type with administrative privileges. This class does not introduce additional
 * properties or methods compared to the base `User` class, but it serves as a marker
 * or specialized role for users who are administrators in the system.
 */
public class Administrator extends User {

    /**
     * Constructs an Administrator object with the specified user ID.
     *
     * @param userId The unique identifier for the user (administrator).
     */
    public Administrator(String userId) {
        super(userId);
    }

}
