package com.example.projectgroup5.events;

import android.util.Log;

import com.example.projectgroup5.database.DatabaseManager;
import com.example.projectgroup5.users.UserSession;

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

    /**
     * Fetches the events associated with the organizer and filters them by the specified time status.
     * This method retrieves the events that the organizer is associated with and checks if each event's
     * time status matches the given status (e.g., "past", "current", "future"). It then invokes the callback
     * with the list of events that match the specified time status.
     *
     * @param callback The callback that will be called with the filtered list of events.
     * @param eventTimeStatus The time status to filter events by. This can be "past", "current", or "future".
     */
    public static void getEventsWithTimeStatus(EventsCallback callback, String eventTimeStatus) {
        List<Event> events = new ArrayList<>();
        DatabaseManager databaseManager = DatabaseManager.getDatabaseManager();
        databaseManager.getOrganizerEvents(UserSession.getInstance().getUserId(), task -> {
            if (!task.isSuccessful()) {
                Log.e("EventOptions", "Failed to get organizer events");
                callback.onDataReceived(events);
            } else {
                List<Event> eventIds = task.getResult();
                // Create a counter to track completed event data retrieval
                AtomicInteger remainingCalls = new AtomicInteger(eventIds.size());
                Log.d("EventOptions", "Got " + eventIds.size() + " events");
                for (Event event : eventIds) {
                    DatabaseManager.getDatabaseManager().getEvent(event.getEventID(), task2 -> {
                        if (task2.getResult() == null || !task2.isSuccessful()) {
                            Log.e("EventOptions", "Failed to create event from database, event ID: " + event);
                            if (remainingCalls.decrementAndGet() == 0) {
                                // Call the callback with the retrieved pending events
                                callback.onDataReceived(events);
                            }
                            return;
                        }
                        // check if the event is in the correct time status
                        if (event.getTimeStatus().equals(eventTimeStatus)) {
                            events.add(event);
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


    /**
     * Fetches the current events for the organizer and invokes the callback with the filtered list.
     * This method internally calls {@link #getEventsWithTimeStatus(EventsCallback, String)} with the
     * time status set to {@link Event#CURRENT}.
     *
     * @param callback The callback that will be called with the list of current events.
     */
    public static void getCurrentEvents(EventOption.EventsCallback callback) {
        getEventsWithTimeStatus(callback, Event.CURRENT);
    }

    /**
     * Fetches the past events for the organizer and invokes the callback with the filtered list.
     * This method internally calls {@link #getEventsWithTimeStatus(EventsCallback, String)} with the
     * time status set to {@link Event#PAST}.
     *
     * @param callback The callback that will be called with the list of past events.
     */
    public static void getPastEvents(EventOption.EventsCallback callback) {
        getEventsWithTimeStatus(callback, Event.PAST);
    }

    /**
     * Fetches the future events for the organizer and invokes the callback with the filtered list.
     * This method internally calls {@link #getEventsWithTimeStatus(EventsCallback, String)} with the
     * time status set to {@link Event#FUTURE}.
     *
     * @param callback The callback that will be called with the list of future events.
     */
    public static void getFutureEvents(EventOption.EventsCallback callback) {
        getEventsWithTimeStatus(callback, Event.FUTURE);
    }

}
