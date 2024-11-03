package com.example.projectgroup5.events;

import com.example.projectgroup5.users.User;
import com.google.firebase.Timestamp;

import java.util.List;

public class Event {
    // An event has a date, start time, end time, a title, and an address.
    // The times are in 30 minute intervals.
    // A new event must be created with a time that has not yet passed.
    // However we can instantiate old events with a time that has already passed.

    private String title;
    private String address;
    private Timestamp startTime;
    private Timestamp endTime;
    private Timestamp date;
    private List<User> participants;

    /**
     * This constructor is used to create a new event.
     * It should not be called directly.
     * Instead use the {@link EventOption#newEvent(String, String, Timestamp, Timestamp, Timestamp, List<User>)}
     * <p> or {@link EventOption#oldEvent(String, String, Timestamp, Timestamp, Timestamp)}</p>
     * @param title
     * @param address
     * @param startTime
     * @param endTime
     * @param date
     * @param participants
     */
    protected Event(String title, String address, Timestamp startTime, Timestamp endTime, Timestamp date, List<User> participants) {
        this.title = title;
        this.address = address;
        this.startTime = startTime;
        this.endTime = endTime;
        this.date = date;
        this.participants = participants;
    }




}
