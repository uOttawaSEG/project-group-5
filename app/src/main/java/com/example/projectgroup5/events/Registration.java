package com.example.projectgroup5.events;

import android.util.Log;

import com.google.firebase.firestore.DocumentReference;

import java.util.UUID;

public class Registration {
    private final DocumentReference attendee;
    private final String registrationStatus;
    private final String registrationId;
    private final DocumentReference event;

    /**
     * Constructs a new {@link Registration} with a randomly generated registration ID.
     *
     * @param attendee The {@link DocumentReference} representing the attendee who registered for the event.
     * @param registrationStatus The status of the registration (e.g., "Accepted", "Waitlisted", "Rejected").
     * @param event The {@link DocumentReference} representing the event the attendee is registering for.
     */
    public Registration(DocumentReference attendee, String registrationStatus, DocumentReference event) {
        this.attendee = attendee;
        this.registrationStatus = registrationStatus;
        Log.d("Registration", "Registration constructor called registration event has been created");
        this.registrationId = UUID.randomUUID().toString();
        this.event = event;
    }

    /**
     * Constructs an existing {@link Registration} with a given registration ID.
     *
     * @param registrationId The unique ID of the registration.
     * @param attendee The {@link DocumentReference} representing the attendee who registered for the event.
     * @param registrationStatus The status of the registration (e.g., "Accepted", "Waitlisted", "Rejected").
     * @param event The {@link DocumentReference} representing the event the attendee is registering for.
     */
    public Registration(String registrationId, DocumentReference attendee, String registrationStatus, DocumentReference event) {
        this.attendee = attendee;
        this.registrationStatus = registrationStatus;
        Log.d("Registration", "old Registration constructor called registration event has been created id: " + registrationId);
        this.registrationId = registrationId;
        this.event = event;
    }

    /**
     * Retrieves the registration ID of the registration.
     *
     * @return The unique registration ID.
     */
    public String getRegistrationId() {
        return registrationId;
    }

    /**
     * Retrieves the {@link DocumentReference} for the attendee associated with the registration.
     *
     * @return The {@link DocumentReference} of the attendee.
     */
    public DocumentReference getAttendee() {
        return attendee;
    }

    /**
     * Retrieves the registration status for the attendee (e.g., "Accepted", "Waitlisted", "Rejected").
     *
     * @return The registration status.
     */
    public String getRegistrationStatus() {
        return registrationStatus;
    }

    /**
     * Retrieves the {@link DocumentReference} for the event associated with the registration.
     *
     * @return The {@link DocumentReference} of the event.
     */
    public DocumentReference getEvent() {
        return event;
    }
}

