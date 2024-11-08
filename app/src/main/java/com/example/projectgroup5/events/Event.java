package com.example.projectgroup5.events;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import java.util.UUID;

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
    private List<DocumentReference> registrations; // this is a list of registrations, they can be found in /registrations/{registrationID}
    private DocumentReference organizer;// This is a reference to the organizer, format is /users/{uid}
    private String eventID; // This is a reference to the event ID,

    /**
     * This constructor is used to create a new event.
     * It should not be called directly.
     * Instead use the {@link EventOption#newEvent(String, String, String, Timestamp, Timestamp, boolean, List, DocumentReference)}
     * <p>or:          {@link EventOption#oldEvent(String, String, String, String, Timestamp, Timestamp, boolean, List, DocumentReference)}</p>
     * @param title The title of the event
     * @param description The description of the event
     * @param address The address of the event
     * @param startTime The start time of the event
     * @param endTime The end time of the event
     * @param autoAccept Whether the registrations to the event should be automatically accepted
     * @param registrations The registrations to the event
     * @param organizer The organizer of the event
     * @
     */
    protected Event(String title,String description, String address, Timestamp startTime, Timestamp endTime, boolean autoAccept, List<DocumentReference> registrations, DocumentReference organizer) {
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
    protected Event(String title, String address, Timestamp startTime, Timestamp endTime, boolean autoAccept, List<DocumentReference> registrations, DocumentReference organizer,String eventID) {
        this.title = title;
        this.address = address;
        this.startTime = startTime;
        this.endTime = endTime;
        this.autoAccept = autoAccept;
        this.registrations = registrations;
        this.organizer = organizer;
        this.eventID = UUID.randomUUID().toString();

    }


    public String getEventID() {
        return eventID;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getAddress() {
        return address;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public boolean isAutoAccept() {
        return autoAccept;
    }

    public List<DocumentReference> getRegistrations() {
        return registrations;
    }

    public DocumentReference getOrganizer() {
        return organizer;
    }
}
