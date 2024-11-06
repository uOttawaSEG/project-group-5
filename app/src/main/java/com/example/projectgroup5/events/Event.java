package com.example.projectgroup5.events;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;

import java.util.List;

public class Event {
    // An event has a date, start time, end time, a title, and an address.
    // The times are in 30 minute intervals.
    // A new event must be created with a time that has not yet passed.
    // However we can instantiate old events with a time that has already passed.

    private String title;
    private String description;
    private String address;
    private Timestamp startTime;
    private Timestamp endTime;
    private boolean autoAccept;
    private List<DocumentReference> participants; // this is a list of reference to the list of users, format is: /users/{uid}
    private DocumentReference organizer; // This is a reference to the organizer, format is /users/{uid}

    /**
     * This constructor is used to create a new event.
     * It should not be called directly.
     * Instead use the {@link EventOption#newEvent(String, String, String, Timestamp, Timestamp, boolean, List, DocumentReference)}
     * <p>or:          {@link EventOption#oldEvent(String, String, String, Timestamp, Timestamp, boolean, List, DocumentReference)}</p>
     * @param title
     * @param address
     * @param startTime
     * @param endTime
     * @param autoAccept
     * @param participants
     * @param organizer
     * @
     */
    protected Event(String title, String address, Timestamp startTime, Timestamp endTime, boolean autoAccept, List<DocumentReference> participants, DocumentReference organizer) {
        this.title = title;
        this.address = address;
        this.startTime = startTime;
        this.endTime = endTime;
        this.autoAccept = autoAccept;
        this.participants = participants;
        this.organizer = organizer;
    }




}
