package com.example.projectgroup5.events;

import com.example.projectgroup5.users.User;
import com.google.firebase.Timestamp;

import java.util.List;

public class EventOption {
    // option to define the different options that can be returned when creating an event
    // can either be an Event or an EventError
    Event event;
    EventError error;
    boolean holdsAnEvent;

    private void setEvent(Event event) {
        this.event = event;
        this.holdsAnEvent = true;
    }

    private void setError(EventError error) {
        this.error = error;
        this.holdsAnEvent = false;
    }

    /**
     * Returns one of the following:
     * {@link #event} contained in EventOption if {@link #holdsAnEvent} is true
     * <p>
     * or {@link #error} contained in EventOption if {@link #holdsAnEvent} is false of the type EventError
     * <ul>
     *  <li><code>ADDRESS_EMPTY_ERROR</code></li>
     *  <li><code>TITLE_EMPTY_ERROR</code></li>
     *  <li><code>DATE_EMPTY_ERROR</code></li>
     *  <li><code>START_TIME_EMPTY_ERROR</code></li>
     *  <li><code>END_TIME_EMPTY_ERROR</code></li>
     * </ul>
     */
    public static EventOption oldEvent(String title, String address, Timestamp startTime, Timestamp endTime, Timestamp date, List<User> participants) {
        EventOption option = new EventOption();

        if (checkEmpty(option, title, address, startTime, endTime, date)) {
            return option;
        }
        Event event = new Event(title, address, startTime, endTime, date, participants);
        option.setEvent(event);
        return option;
    }

    /**
     * Returns one of the following:
     * {@link #event} contained in EventOption if {@link #holdsAnEvent} is true
     * <p>
     * or {@link #error} contained in EventOption if {@link #holdsAnEvent} is false of the type EventError
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
    public static EventOption newEvent(String title, String address, Timestamp startTime, Timestamp endTime, Timestamp date, List<User> participants) {
        EventOption option = new EventOption();

        if (checkEmpty(option, title, address, startTime, endTime, date)) {
            return option;
        }

        if (checkTimes(option, startTime, endTime)) {
            return option;
        }

        Event event = new Event(title, address, startTime, endTime, date, participants);
        option.setEvent(event);
        return option;
    }

    private static boolean checkEmpty(EventOption option, String title, String address, Timestamp startTime, Timestamp endTime, Timestamp date) {
        if (title == null || title.isEmpty()) {
            option.setError(EventError.TITLE_EMPTY_ERROR);
            return true;
        }
        if (address == null || address.isEmpty()) {
            option.setError(EventError.ADDRESS_EMPTY_ERROR);
            return true;
        }
        if (startTime == null) {
            option.setError(EventError.START_TIME_EMPTY_ERROR);
            return true;
        }
        if (endTime == null) {
            option.setError(EventError.END_TIME_EMPTY_ERROR);
            return true;
        }
        if (date == null) {
            option.setError(EventError.DATE_EMPTY_ERROR);
            return true;
        }
        return false; // No errors found
    }

    private static boolean checkTimes(EventOption option, Timestamp startTime, Timestamp endTime) {
        if (startTime.compareTo(Timestamp.now()) < 0) {
            option.setError(EventError.START_TIME_PAST_ERROR);
            return true;
        }
        if (endTime.compareTo(Timestamp.now()) < 0) {
            option.setError(EventError.END_TIME_PAST_ERROR);
            return true;
        }
        if (endTime.compareTo(startTime) < 0) {
            option.setError(EventError.END_TIME_BEFORE_START_TIME_ERROR);
            return true;
        }
        return false; // No errors found
    }
}