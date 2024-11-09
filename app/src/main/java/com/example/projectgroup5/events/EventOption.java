package com.example.projectgroup5.events;

import android.util.Log;

import com.example.projectgroup5.database.DatabaseManager;
import com.example.projectgroup5.users.UserSession;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class EventOption {
    /**
     * Callback interface for receiving a list of events.
     * <p>
     * This interface defines a method to be called when a list of event objects is available,
     * typically after querying a database or performing an asynchronous operation.
     */
    public interface EventsCallback {
        /**
         * Called when the data retrieval is complete and the event list is available.
         *
         * @param eventIds A list of event objects that were retrieved.
         */
        void onDataReceived(List<Event> eventIds);
    }


    public static void getEventsWithTimeStatus(EventOption.EventsCallback callback, String eventTimeStatus) {
        List<Event> events = new ArrayList<>();
        DatabaseManager databaseManager = DatabaseManager.getDatabaseManager();
        databaseManager.getOrganizerEvents(UserSession.getInstance().getUserId(), task -> {
            if (task == null || !task.isSuccessful()) {
                Log.e("EventOptions", "Failed to get organizer events");
                callback.onDataReceived(events);
                return;
            } else {
                List<DocumentReference> eventIds = task.getResult();
                // Create a counter to track completed event data retrieval
                AtomicInteger remainingCalls = new AtomicInteger(eventIds.size());
                Log.d("EventOptions", "Got " + eventIds.size() + " events");
                for (DocumentReference eventId : eventIds) {
                    DatabaseManager.getDatabaseManager().getEvent(eventId.getId(), task2 -> {
                        if (task2.getResult() == null || !task2.isSuccessful()) {
                            Log.e("EventOptions", "Failed to create event from database, event ID: " + eventId);
                            if (remainingCalls.decrementAndGet() == 0) {
                                // Call the callback with the retrieved pending events
                                callback.onDataReceived(events);
                            }
                            return;
                        }
                        if (task2.getResult().holdsAnEvent()) {
                            Event event = task2.getResult().getEvent();
                            // check if the event is in the correct time status
                            if (event.getTimeStatus().equals(eventTimeStatus)) {
                                events.add(event);
                            }
                        }
                        // Decrement the counter and check if all callbacks are complete
                        if (remainingCalls.decrementAndGet() == 0) {
                            // Call the callback with the retrieved pending events
                            callback.onDataReceived(events);
                        }
                    });
                }
                // If there are no events, callback immediately
                if (eventIds.isEmpty()) {
                    callback.onDataReceived(events);
                }
            }
        });
    }


    public static void getCurrentEvents(EventOption.EventsCallback callback) {
        getEventsWithTimeStatus(callback, Event.CURRENT);
    }

    public static void getPastEvents(EventOption.EventsCallback callback) {
        getEventsWithTimeStatus(callback, Event.PAST);
    }


    public static void getFutureEvents(EventOption.EventsCallback callback) {
        getEventsWithTimeStatus(callback, Event.FUTURE);
    }
}
