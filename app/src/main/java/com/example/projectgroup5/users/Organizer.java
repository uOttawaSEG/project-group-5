package com.example.projectgroup5.users;

import com.google.firebase.firestore.DocumentReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an organizer user in the system.
 * The `Organizer` class extends the `User` class and represents users who organize events.
 * Each `Organizer` can manage a list of events they are associated with, as well as the
 * organization they represent.
 */
public class Organizer extends User {

    private String userOrganizationName;
    private List<DocumentReference> organizerEvents = new ArrayList<>();

    /**
     * Constructs an Organizer object with the specified user ID.
     *
     * @param userId The unique identifier for the user (organizer).
     */
    public Organizer(String userId) {
        super(userId);
    }

    // ------------------------Getters and setters---------------------

    /**
     * Returns the name of the organization the organizer is associated with.
     *
     * @return The organization name.
     */
    public String getUserOrganizationName() {
        return userOrganizationName;
    }

    /**
     * Sets the name of the organization the organizer is associated with.
     *
     * @param userOrganizationName The name of the organization.
     */
    public void setUserOrganizationName(String userOrganizationName) {
        this.userOrganizationName = userOrganizationName;
    }

    /**
     * Returns the list of events that the organizer is associated with.
     *
     * @return The list of organizer's events (DocumentReference objects).
     */
    public List<DocumentReference> getOrganizerEvents() {
        return organizerEvents;
    }

    /**
     * Sets the list of events associated with the organizer.
     *
     * @param organizerEvents The list of events.
     */
    public void setOrganizerEvents(List<DocumentReference> organizerEvents) {
        this.organizerEvents = organizerEvents;
    }

    /**
     * Adds an event to the organizer's list of events.
     *
     * @param event The event to add (DocumentReference object).
     */
    public void addEvent(DocumentReference event) {
        organizerEvents.add(event);
    }

    /**
     * Removes an event from the organizer's list of events.
     *
     * @param event The event to remove (DocumentReference object).
     */
    public void removeEvent(DocumentReference event) {
        organizerEvents.remove(event);
    }

}
