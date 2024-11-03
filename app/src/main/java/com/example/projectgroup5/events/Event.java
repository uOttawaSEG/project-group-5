package com.example.projectgroup5.events;

import com.google.firebase.Timestamp;

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



}
