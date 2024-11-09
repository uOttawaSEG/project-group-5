package com.example.projectgroup5.events;

import com.google.firebase.firestore.DocumentReference;

public class Registration {
    private DocumentReference attendee;
    private String registrationStatus;
    private String registrationId;
    public static final String EVENT_REGISTERED = "Registered";
    public static final String EVENT_WAITLISTED = "Waitlisted";
    public static final String EVENT_REJECTED = "Rejected";
    public static final String EVENT_NOT_REGISTERED = "Not Registered";

    public Registration(DocumentReference attendee, String registrationStatus) {
        this.attendee = attendee;
        this.registrationStatus = registrationStatus;
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
}

