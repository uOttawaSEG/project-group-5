package com.example.projectgroup5.users;

import com.example.projectgroup5.events.Event;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an attendee user in the system.
 * <p>
 * This class extends from the `User` class and adds functionality specific to attendees,
 * such as managing event registrations and maintaining an event cache.
 * An attendee can register for events, and this class provides methods for adding and
 * removing registrations, as well as caching events the attendee is registered for.
 */
public class Attendee extends User {

    private List<DocumentReference> attendeeRegistrations = new ArrayList<>();
    private List<Event> eventCache = new ArrayList<>();

    /**
     * Constructs an Attendee object with the specified user ID.
     *
     * @param userId The unique identifier for the user (attendee).
     */
    public Attendee(String userId) {
        super(userId);
    }

    // ------------------------Getters and setters---------------------

    /**
     * Returns the list of event registrations (DocumentReferences).
     *
     * @return List of DocumentReferences representing the attendee's event registrations.
     */
    public List<DocumentReference> getAttendeeRegistrations() {
        return attendeeRegistrations;
    }

    /**
     * Sets the list of event registrations (DocumentReferences).
     *
     * @param attendeeEvents A list of DocumentReferences representing the event registrations to set.
     */
    public void setAttendeeRegistrations(List<DocumentReference> attendeeEvents) {
        this.attendeeRegistrations = attendeeEvents;
    }

    /**
     * Returns the cached list of events the attendee is registered for.
     *
     * @return List of Event objects representing events the attendee is registered for.
     */
    public List<Event> getEventCache() {
        return eventCache;
    }

    /**
     * Adds an event to the attendee's event cache if it's not already present.
     *
     * @param event The event to add to the cache.
     */
    public void addEventToCache(Event event) {
        if (this.eventCache == null) {
            this.eventCache = new ArrayList<>();
        }
        if (this.eventCache.contains(event)) {
            return;
        }
        this.eventCache.add(event);
    }

    /**
     * Clears the attendee's event cache, removing all cached events.
     */
    public void clearEventCache() {
        this.eventCache.clear();
    }

    /**
     * Adds a registration (DocumentReference) to the list of attendee registrations.
     *
     * @param attendeeRegistration The DocumentReference representing the event registration to add.
     */
    public void addRegistration(DocumentReference attendeeRegistration) {
        this.attendeeRegistrations.add(attendeeRegistration);
    }
}
