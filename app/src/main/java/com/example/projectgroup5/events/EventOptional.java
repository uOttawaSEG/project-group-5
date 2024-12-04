package com.example.projectgroup5.events;

import com.example.projectgroup5.database.FieldValidator;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.List;

public class EventOptional {
    // option to define the different options that can be returned when creating an event
    // can either be an Event or an EventError
    Event event;
    EventError error;
    boolean holdsAnEvent;

    /**
     * This constructor is used to create a new event option. For an event that has already occurred.
     * Returns one of the following:
     * {@link #event} contained in EventOptional if {@link #holdsAnEvent} is true
     * <p>
     * or {@link #error} contained in EventOptional if {@link #holdsAnEvent} is false of the type EventError
     * <ul>
     *  <li><code>ADDRESS_EMPTY_ERROR</code></li>
     *  <li><code>TITLE_EMPTY_ERROR</code></li>
     *  <li><code>DATE_EMPTY_ERROR</code></li>
     *  <li><code>START_TIME_EMPTY_ERROR</code></li>
     *  <li><code>END_TIME_EMPTY_ERROR</code></li>
     * </ul>
     */
    public static EventOptional oldEvent(String eventID, String title, String description, String address, Timestamp startTime, Timestamp endTime, boolean autoAccept, List<DocumentReference> registrations, DocumentReference organizer) {
        EventOptional option = new EventOptional();

        if (checkFields(option, title, description, address, startTime, endTime, organizer, eventID)) {
            return option;
        }
        Event event = new Event(title, description, address, startTime, endTime, autoAccept, registrations, organizer, eventID);
        option.setEvent(event);
        return option;
    }

    /**
     * This creates a new event option. For an event that has not yet occurred. It is more strict than {@link #oldEvent}
     * Since it makes sure that the event is not in the past.
     * Returns one of the following:
     * {@link #event} contained in EventOptional if {@link #holdsAnEvent} is true
     * <p>
     * or {@link #error} contained in EventOptional if {@link #holdsAnEvent} is false of the type EventError
     * <ul>
     *  <li><code>START_TIME_PAST_ERROR</code></li>
     *  <li><code>END_TIME_PAST_ERROR</code></li>
     *  <li><code>END_TIME_BEFORE_START_TIME_ERROR</code></li>
     *  <li><code>ADDRESS_EMPTY_ERROR</code></li>
     *  <li><code>TITLE_EMPTY_ERROR</code></li>
     *  <li><code>DATE_EMPTY_ERROR</code></li>
     *  <li><code>START_TIME_EMPTY_ERROR</code></li>
     *  <li><code>END_TIME_EMPTY_ERROR</code></li>
     * </ul>
     */
    public static EventOptional newEvent(String title, String description, String address, Timestamp startTime, Timestamp endTime, boolean autoAccept, DocumentReference organizer) {
        EventOptional option = new EventOptional();

        if (checkFields(option, title, description, address, startTime, endTime, organizer)) {
            return option;
        }

        if (checkTimes(option, startTime, endTime)) {
            return option;
        }

        Event event = new Event(title, description, address, startTime, endTime, autoAccept, new ArrayList<>(), organizer);
        option.setEvent(event);
        return option;
    }

    /**
     * Validates the fields of an event, checking basic properties like title, description, address, start time,
     * end time, organizer, and event ID. If any field is invalid, an error is set on the provided {@link EventOptional} object.
     * <p>
     * This method first calls the overloaded {@link #checkFields(EventOptional, String, String, String, Timestamp, Timestamp, DocumentReference)}
     * to check basic fields. Then it checks if the event ID is null or empty and sets an appropriate error if it is.
     * </p>
     *
     * @param option The {@link EventOptional} object to set the error on if any field is invalid.
     * @param title The title of the event.
     * @param description The description of the event.
     * @param address The address where the event will take place.
     * @param startTime The start time of the event.
     * @param endTime The end time of the event.
     * @param organizer The {@link DocumentReference} representing the organizer of the event.
     * @param eventID The unique ID of the event.
     * @return {@code true} if any field is invalid, {@code false} if all fields are valid.
     */
    private static boolean checkFields(EventOptional option, String title, String description, String address, Timestamp startTime, Timestamp endTime, DocumentReference organizer, String eventID) {
        if (checkFields(option, title, description, address, startTime, endTime, organizer)) {
            return true;
        }

        if (eventID == null && eventID.isEmpty()) {
            option.setError(EventError.EVENT_ID_EMPTY);
            return true;
        }

        return false;
    }

    /**
     * Validates the basic fields of an event, including title, description, address, start time, end time, and organizer.
     * Sets appropriate error on the {@link EventOptional} object if any of the fields are invalid.
     * <p>
     * This method checks that all required fields are provided and formatted correctly. If any field is invalid,
     * an error is set on the {@link EventOptional} object and {@code true} is returned.
     * </p>
     *
     * @param option The {@link EventOptional} object to set the error on if any field is invalid.
     * @param title The title of the event.
     * @param description The description of the event.
     * @param address The address where the event will take place.
     * @param startTime The start time of the event.
     * @param endTime The end time of the event.
     * @param organizer The {@link DocumentReference} representing the organizer of the event.
     * @return {@code true} if any field is invalid, {@code false} if all fields are valid.
     */
    private static boolean checkFields(EventOptional option, String title, String description, String address, Timestamp startTime, Timestamp endTime, DocumentReference organizer) {
        if (title == null || title.isEmpty()) {
            option.setError(EventError.TITLE_EMPTY);
            return true;
        }
        if (FieldValidator.checkIfIsNotAlphabetWithSpaces(title)) {
            option.setError(EventError.TITLE_BADLY_FORMATTED);
            return true;
        }
        if (description == null || description.isEmpty()) {
            option.setError(EventError.DESCRIPTION_EMPTY);
            return true;
        }
        if (address == null || address.isEmpty()) {
            option.setError(EventError.ADDRESS_EMPTY);
            return true;
        }
        if (startTime == null) {
            option.setError(EventError.START_TIME_EMPTY);
            return true;
        }
        if (endTime == null) {
            option.setError(EventError.END_TIME_EMPTY);
            return true;
        }
        if (organizer == null) {
            option.setError(EventError.ORGANIZER_EMPTY);
            return true;
        }
        return false; // No errors found
    }

    /**
     * Validates the start and end times of an event.
     * Checks if the start time or end time is in the past and if the end time is before the start time.
     * Sets appropriate error on the {@link EventOptional} object if any of the conditions are violated.
     *
     * @param option The {@link EventOptional} object to set the error on if any of the times are invalid.
     * @param startTime The start time of the event.
     * @param endTime The end time of the event.
     * @return {@code true} if any time-related validation fails, {@code false} if the times are valid.
     */
    private static boolean checkTimes(EventOptional option, Timestamp startTime, Timestamp endTime) {
        if (startTime.compareTo(Timestamp.now()) < 0) {
            option.setError(EventError.START_TIME_PAST);
            return true;
        }
        if (endTime.compareTo(Timestamp.now()) < 0) {
            option.setError(EventError.END_TIME_PAST);
            return true;
        }
        if (endTime.compareTo(startTime) < 0) {
            option.setError(EventError.END_TIME_BEFORE_START_TIME);
            return true;
        }
        return false; // No errors found
    }

    /**
     * Retrieves the event associated with this {@link EventOptional} object.
     * If the object does not hold a valid event, {@code null} is returned.
     *
     * @return The event if present, otherwise {@code null}.
     */
    public Event getEvent() {
        return holdsAnEvent ? event : null;
    }

    /**
     * Sets the event for this {@link EventOptional} object. Marks the object as holding a valid event.
     *
     * @param event The event to set for this object.
     */
    private void setEvent(Event event) {
        this.event = event;
        this.holdsAnEvent = true;
    }

    /**
     * Checks whether this {@link EventOptional} object holds a valid event.
     *
     * @return {@code true} if the object holds a valid event, otherwise {@code false}.
     */
    public boolean holdsAnEvent() {
        return holdsAnEvent;
    }

    /**
     * Retrieves the error associated with this {@link EventOptional} object.
     * If the object does not hold a valid event, the error is returned. Otherwise, {@code null} is returned.
     *
     * @return The error if present, otherwise {@code null}.
     */
    public EventError getError() {
        if (!holdsAnEvent) {
            return error;
        }
        return null;
    }

    /**
     * Sets an error on this {@link EventOptional} object and marks it as not holding a valid event.
     *
     * @param error The error to set for this object.
     */
    private void setError(EventError error) {
        this.error = error;
        this.holdsAnEvent = false;
    }
}