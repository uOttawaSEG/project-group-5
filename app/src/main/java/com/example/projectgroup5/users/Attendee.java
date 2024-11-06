package com.example.projectgroup5.users;

import com.google.firebase.firestore.DocumentReference;

import java.util.List;

public class Attendee extends User {

    private List<DocumentReference> attendeeEvents;

    public Attendee(String userId) {
        super(userId);
    }

    // ------------------------Getters and setters---------------------

    public List<DocumentReference> getAttendeeEvents() {
        return attendeeEvents;
    }

    public void setAttendeeEvents(List<DocumentReference> attendeeEvents) {
        this.attendeeEvents = attendeeEvents;
    }

}
