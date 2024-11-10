package com.example.projectgroup5.users;

import com.example.projectgroup5.events.Event;
import com.example.projectgroup5.events.Registration;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.List;

public class Attendee extends User {

    private List<DocumentReference> attendeeRegistrations = new ArrayList<>();
    private List<Event> eventCache = new ArrayList<>();

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

    public void setEventCache(List<Event> eventCache) {
        this.eventCache = eventCache;
    }

    public List<Event> getEventCache() {
        return eventCache;
    }

    public void addEventToCache(Event event) {
        if (this.eventCache == null) {
            this.eventCache = new ArrayList<>();
        }
        if (this.eventCache.contains(event)) {
            return;
        }
        this.eventCache.add(event);
    }

    //TODO if this is called, event cache should also be updated
    public void addRegistration(DocumentReference attendeeRegistration) {
        this.attendeeRegistrations.add(attendeeRegistration);
    }

    public void removeRegistration(DocumentReference attendeeRegistration) {
        this.attendeeRegistrations.remove(attendeeRegistration);
    }

}
