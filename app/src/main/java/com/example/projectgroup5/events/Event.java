package com.example.projectgroup5.events;

import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;

import java.util.List;
import java.util.UUID;

public class Event {
    private final String title;
    private final String description;
    private final String address;
    private final Timestamp startTime;
    private final Timestamp endTime;
    private final boolean autoAccept;
    private final List<DocumentReference> registrations; // this is a list of registrations, they can be found in /registrations/{registrationID}
    private final DocumentReference organizer;// This is a reference to the organizer, format is /users/{uid}
    private final String eventID; // This is a reference to the event ID,
    public static final String PAST = "past";
    public static final String CURRENT = "current";
    public static final String FUTURE = "future";

    /**
     * This constructor is used to create a new event.
     * It should not be called directly. Instead, use the
     * {@link EventOptional#newEvent(String, String, String, Timestamp, Timestamp, boolean, DocumentReference)}
     * method to create a new event.
     *
     * <p>Alternatively, use {@link EventOptional#oldEvent(String, String, String, String, Timestamp, Timestamp, boolean, List, DocumentReference)}
     * to create an event from an existing one.</p>
     *
     * @param title         The title of the event.
     * @param description   A detailed description of the event.
     * @param address       The physical or virtual address where the event will take place.
     * @param startTime     The start time of the event.
     * @param endTime       The end time of the event.
     * @param autoAccept    A boolean value indicating whether registrations to the event should be automatically accepted.
     * @param registrations A list of document references to the registrations for the event.
     * @param organizer     A document reference to the organizer of the event.
     */
    protected Event(String title, String description, String address, Timestamp startTime, Timestamp endTime, boolean autoAccept, List<DocumentReference> registrations, DocumentReference organizer) {
        this.title = title;
        this.description = description;
        this.address = address;
        this.startTime = startTime;
        this.endTime = endTime;
        this.autoAccept = autoAccept;
        this.registrations = registrations;
        this.organizer = organizer;
        this.eventID = UUID.randomUUID().toString();
    }

    /**
     * This constructor is used to create an event from existing data.
     * It should not be called directly. Use {@link EventOptional#oldEvent(String, String, String, String, Timestamp, Timestamp, boolean, List, DocumentReference)}
     * to create an event from existing data.
     *
     * <p>This constructor allows for the specification of an event ID.</p>
     *
     * @param title         The title of the event.
     * @param description   A detailed description of the event.
     * @param address       The physical or virtual address where the event will take place.
     * @param startTime     The start time of the event.
     * @param endTime       The end time of the event.
     * @param autoAccept    A boolean value indicating whether registrations to the event should be automatically accepted.
     * @param registrations A list of document references to the registrations for the event.
     * @param organizer     A document reference to the organizer of the event.
     * @param eventID       The unique identifier for the event.
     */
    protected Event(String title, String description, String address, Timestamp startTime, Timestamp endTime, boolean autoAccept, List<DocumentReference> registrations, DocumentReference organizer, String eventID) {
        this.title = title;
        this.description = description;
        this.address = address;
        this.startTime = startTime;
        this.endTime = endTime;
        this.autoAccept = autoAccept;
        this.registrations = registrations;
        this.organizer = organizer;
        this.eventID = eventID;

    }

    /**
     * Gets the unique identifier for the event.
     *
     * @return The unique event ID.
     */
    public String getEventID() {
        return eventID;
    }

    /**
     * Gets the title of the event.
     *
     * @return The title of the event.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the description of the event.
     *
     * @return The description of the event.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the address where the event is held.
     *
     * @return The address of the event.
     */
    public String getAddress() {
        return address;
    }

    /**
     * Gets the start time of the event.
     *
     * @return The start time of the event as a {@link Timestamp}.
     */
    public Timestamp getStartTime() {
        return startTime;
    }

    /**
     * Gets the end time of the event.
     *
     * @return The end time of the event as a {@link Timestamp}.
     */
    public Timestamp getEndTime() {
        return endTime;
    }

    /**
     * Checks if the event automatically accepts registrations.
     *
     * @return {@code true} if the event auto-accepts registrations, {@code false} otherwise.
     */
    public boolean isAutoAccept() {
        return autoAccept;
    }

    /**
     * Gets the list of document references for registrations to the event.
     *
     * @return A list of document references representing the registrations for the event.
     */
    public List<DocumentReference> getRegistrations() {
        return registrations;
    }

    /**
     * Gets the document reference to the organizer of the event.
     *
     * @return A document reference to the organizer.
     */
    public DocumentReference getOrganizer() {
        return organizer;
    }

    /**
     * Determines the current time status of the event.
     * The status could be:
     * - "FUTURE" if the event's start time is in the future,
     * - "CURRENT" if the event is ongoing (i.e., its start time has passed, but its end time is in the future),
     * - "PAST" if the event has already finished (i.e., its end time has passed).
     *
     * @return A string representing the time status of the event: "FUTURE", "CURRENT", or "PAST".
     */
    public String getTimeStatus() {
        // give out the time status of the event, either past, current, or future
        Timestamp now = Timestamp.now();
        if (now.compareTo(startTime) < 0) {
            return FUTURE;
        } else if (now.compareTo(startTime) >= 0 && now.compareTo(endTime) < 0) {
            return CURRENT;
        } else {
            return PAST;
        }
    }

    /**
     * Checks if there is a time conflict between this event and another event.
     * A time conflict is considered when the two events overlap in time.
     * This method checks if the start and end times of the events overlap,
     * meaning if the start time of one event is before the end time of the other
     * event and vice versa.
     *
     * @param otherEvent The other event to check for a time conflict with.
     * @return {@code true} if there is a time conflict, {@code false} otherwise.
     */
    public boolean timeConflict(Event otherEvent) {
        Log.e("Event", "Checking time conflict between " + this.getTitle() + " and " + otherEvent.getTitle());
        return this.startTime.compareTo(otherEvent.getEndTime()) < 0 && this.endTime.compareTo(otherEvent.getStartTime()) > 0;
    }

}
