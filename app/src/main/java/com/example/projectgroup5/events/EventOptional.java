package com.example.projectgroup5.events;

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

    public Event getEvent() {
        return holdsAnEvent ? event : null;
    }

    private void setEvent(Event event) {
        this.event = event;
        this.holdsAnEvent = true;
    }

    private void setError(EventError error) {
        this.error = error;
        this.holdsAnEvent = false;
    }

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
        Event event = new Event(title, address, startTime, endTime, autoAccept, registrations, organizer, eventID);
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
    // TODO auto accept and address
    public static EventOptional newEvent(String title, String description, String address, Timestamp startTime, Timestamp endTime, boolean autoAccept, DocumentReference organizer) {
        EventOptional option = new EventOptional();

        if (checkFields(option, title,description, address, startTime, endTime, organizer)) {
            return option;
        }

        if (checkTimes(option, startTime, endTime)) {
            return option;
        }

        Event event = new Event(title, description, address, startTime, endTime, autoAccept, new ArrayList<DocumentReference>(), organizer);
        option.setEvent(event);
        return option;
    }

    public boolean holdsAnEvent() {
        return holdsAnEvent;
    }

    public EventError getError() {
        if (!holdsAnEvent) {
            return error;
        }
        return null;
    }
    private static boolean checkFields(EventOptional option, String title, String description, String address, Timestamp startTime, Timestamp endTime, DocumentReference organizer, String eventID){
        if (checkFields(option, title, description, address, startTime, endTime, organizer)) {
            return true;
        }

        if (eventID == null && eventID.isEmpty()) {
            option.setError(EventError.EVENT_ID_EMPTY);
            return true;
        }

        return false;
    }
    private static boolean checkFields(EventOptional option, String title, String description, String address, Timestamp startTime, Timestamp endTime, DocumentReference organizer) {
        if (title == null || title.isEmpty()) {
            option.setError(EventError.TITLE_EMPTY);
            return true;
        }
        if (!title.matches("[a-zA-Z]+")) {
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
}