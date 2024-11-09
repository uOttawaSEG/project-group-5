package com.example.projectgroup5.users;

import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.List;

public class Attendee extends User {

    private List<DocumentReference> attendeeRegistrations = new ArrayList<>();

    public Attendee(String userId) {
        super(userId);
    }

    // ------------------------Getters and setters---------------------

    public List<DocumentReference> getAttendeeRegistrations() {
        return attendeeRegistrations;
    }

    public void setAttendeeRegistrations(List<DocumentReference> attendeeEvents) {
        this.attendeeRegistrations = attendeeEvents;
    }

    public void addRegistration(DocumentReference attendeeRegistration) {
        this.attendeeRegistrations.add(attendeeRegistration);
    }

    public void removeRegistration(DocumentReference attendeeRegistration) {
        this.attendeeRegistrations.remove(attendeeRegistration);
    }

}
