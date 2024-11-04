package com.example.projectgroup5.users;

import com.google.firebase.firestore.DocumentReference;

import java.util.List;

public class Organizer extends User {


    private String userOrganizationName;
    private List<DocumentReference> organizerEvents;

    public Organizer(String userId) {
        super(userId);
    }

    // ------------------------Getters and setters---------------------

    public String getUserOrganizationName() {
        return userOrganizationName;
    }

    public void setUserOrganizationName(String userOrganizationName) {
        this.userOrganizationName = userOrganizationName;
    }

    public List<DocumentReference> getOrganizerEvents() {
        return organizerEvents;
    }

    public void setOrganizerEvents(List<DocumentReference> organizerEvents) {
        this.organizerEvents = organizerEvents;
    }


}
