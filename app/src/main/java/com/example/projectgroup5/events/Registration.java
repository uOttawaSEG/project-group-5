package com.example.projectgroup5.events;

import com.google.firebase.firestore.DocumentReference;

import java.util.UUID;

public class Registration {
    private DocumentReference attendee;
    private String registrationStatus;
    private String registrationId;
    private DocumentReference event;
    public static final String EVENT_REGISTERED = "Registered";
    public static final String EVENT_WAITLISTED = "Waitlisted";
    public static final String EVENT_REJECTED = "Rejected";
    public static final String EVENT_NOT_REGISTERED = "Not Registered";

    public Registration(DocumentReference attendee, String registrationStatus, DocumentReference event) {
        this.attendee = attendee;
        this.registrationStatus = registrationStatus;
        this.registrationId = UUID.randomUUID().toString();
        this.event = event;
    }

    public Registration(String registrationId, DocumentReference attendee, String registrationStatus, DocumentReference event) {
        this.attendee = attendee;
        this.registrationStatus = registrationStatus;
        this.registrationId = registrationId;
        this.event = event;
    }

    public String getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
    }

    public DocumentReference getAttendee() {
        return attendee;
    }

    public String getRegistrationStatus() {
        return registrationStatus;
    }

    public DocumentReference getEvent() {
        return event;
    }
}

