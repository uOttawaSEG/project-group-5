package com.example.projectgroup5.ui.home;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.projectgroup5.R;
import com.example.projectgroup5.database.DatabaseListener;
import com.example.projectgroup5.database.DatabaseManager;
import com.example.projectgroup5.databinding.FragmentAttendeeEventListBinding;
import com.example.projectgroup5.events.Event;
import com.example.projectgroup5.events.EventAdapterForDisplay;
import com.example.projectgroup5.users.Attendee;
import com.example.projectgroup5.users.User;
import com.example.projectgroup5.users.UserSession;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


public class AttendeeEventList extends Fragment {
    private static final int LONG_PRESS_THRESHOLD = 4000; // 5 second for second action
    private final Handler handler = new Handler();
    private Runnable prolongedPressRunnable;

    /**
     * Updates the status bar message based on the list of events. If no events are found,
     * the status bar displays a message indicating that there are no registrations.
     * Otherwise, it displays a message showing "My registrations".
     * This method is used to dynamically update the UI based on the current state of events.
     * If the user has no event registrations, it prompts the user to visit the event list to
     * register for an event. If the user has registrations, it shows a more relevant message.
     *
     * @param events A list of Event objects, which represents the current event registrations of the user.
     * @param binding The binding object that links the layout views to the fragment's code.
     */
    private static void change_status_bar_message(List<Event> events, FragmentAttendeeEventListBinding binding) {
        if (events.isEmpty()) {
            // set the textview text to show that there are no events
            binding.statusBarText.setText("No registrations, go to the event list and hold an event to register!");
        } else {
            binding.statusBarText.setText("My registrations");
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentAttendeeEventListBinding binding = FragmentAttendeeEventListBinding.inflate(inflater, container, false);
        // Initialize the ListView
        ListView listView = binding.attendeeListLayout;
        // get the navigator

        // get the events from the database
        List<Event> events = new ArrayList<>();
        change_status_bar_message(events, binding);
        DatabaseManager.getDatabaseManager().getAttendeeRegistrations(UserSession.getInstance().getUserRepresentation().getUserId(), registrations -> {
            AtomicInteger count = new AtomicInteger(registrations.getResult().size());
            events.clear();
            for (DocumentReference registrationRef : registrations.getResult()) {
                DatabaseManager.getDatabaseManager().getEventFromRegistration(registrationRef, task -> {
                    if (task.isSuccessful()) {
                        events.add(task.getResult().getEvent());
                        if (count.decrementAndGet() == 0) {
                            // now we must remove all the events that are already passed
                            events.removeIf(event -> event.getEndTime().toDate().before(new java.util.Date()));
                            // now we must sort the events by starting date
                            events.sort(Comparator.comparing(Event::getStartTime));
                            EventAdapterForDisplay eventAttendeeAdapter = new EventAdapterForDisplay(getContext(), events);
                            change_status_bar_message(events, binding);
                            listView.setAdapter(eventAttendeeAdapter);
                        }
                    }
                });
            }
        });

        listView.setOnItemClickListener((parentView, view, position, id) -> {
            // if the user is not an instanceof attendee return
            if (UserSession.getInstance().getUserRepresentation() == null || !(UserSession.getInstance().getUserRepresentation() instanceof Attendee)) {
                return;
            }
            // we toggle the description and adress visibility
            Event selectedEvent = (Event) parentView.getItemAtPosition(position);
            if (selectedEvent.getDescription() != null) {
                if (view.findViewById(R.id.eventDescriptionEntry).getVisibility() == (View.VISIBLE)) {
                    view.findViewById(R.id.eventDescriptionEntry).setVisibility(View.GONE);
                } else {
                    view.findViewById(R.id.eventDescriptionEntry).setVisibility(View.VISIBLE);
                }
            }
            if (selectedEvent.getAddress() != null) {
                if (view.findViewById(R.id.eventAddressEntry).getVisibility() == (View.VISIBLE)) {
                    view.findViewById(R.id.eventAddressEntry).setVisibility(View.GONE);
                } else {
                    view.findViewById(R.id.eventAddressEntry).setVisibility(View.VISIBLE);
                }
            }
        });


        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            // will try to unregister the user from the event if possible
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public boolean onItemLongClick(AdapterView<?> parentView, View view, int position, long id) {
                Log.d("AttendeeEventList", "Long click");
                // Handle long click event
                Event selectedEvent = (Event) parentView.getItemAtPosition(position);
                Log.e("AttendeeEventList", "Selected event: " + selectedEvent);
                DatabaseManager.getDatabaseManager().getAttendanceToEvent(DatabaseManager.getDatabaseManager().getEventReference(selectedEvent.getEventID()), attendanceValue -> {
                    Log.d("AttendeeEventList", "Called here");
                    if (!attendanceValue.getResult().equals(User.WAITLISTED)) {
                        Log.w("AttendeeEventList", "Selected event: " + selectedEvent);
                        Toast.makeText(getContext(), "You have been processed, you cannot unregister", Toast.LENGTH_SHORT).show();
                    } else if ((selectedEvent.getStartTime().toDate().getTime() - (new java.util.Date()).getTime()) < 24 * 60 * 60 * 1000) { // if the start time is in less than 24 hours we cant unregister
                        Log.w("AttendeeEventList", "Selected event: " + selectedEvent);
                        Toast.makeText(getContext(), "Less than 24 hours left", Toast.LENGTH_SHORT).show();
                    } else {
                        getView().performHapticFeedback(HapticFeedbackConstants.GESTURE_START);
                        Toast.makeText(getContext(), "Keep holding to remove", Toast.LENGTH_SHORT).show();
                        // we display the option to delete the event and delete it
                        prolongedPressRunnable = () -> {
                            // delete the registration from the user, the event and then the registration object from firestore
                            DocumentReference eventRef = DatabaseManager.getDatabaseManager().getEventReference(selectedEvent.getEventID());
                            DatabaseManager.getDatabaseManager().getRegistrationReferenceToEvent(eventRef, registrationRefTask -> {
                                if (registrationRefTask.isSuccessful()) {
                                    DatabaseManager.getDatabaseManager().deleteRegistration(registrationRefTask.getResult(), task -> {
                                        Log.d("AttendeeEventList", "Registration deleted from firestore " + registrationRefTask.getResult().getId());
                                        getView().performHapticFeedback(HapticFeedbackConstants.GESTURE_END);
                                        Toast.makeText(getContext(), "Event removed", Toast.LENGTH_SHORT).show();
                                        selectedEvent.getRegistrations().remove(registrationRefTask.getResult());
                                        // update the adapter
                                        events.remove(selectedEvent);
                                        // remove the listener
                                        DatabaseListener.deleteEventStartListener(selectedEvent.getEventID());
                                        EventAdapterForDisplay eventOrganizerAdapter = new EventAdapterForDisplay(getContext(), events);
                                        ((Attendee) UserSession.getInstance().getUserRepresentation()).clearEventCache();
                                        change_status_bar_message(events, binding);
                                        listView.setAdapter(eventOrganizerAdapter);
                                    });
                                }
                            });

                        };
                        listView.setOnTouchListener((v, event) -> {
                            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                                if (prolongedPressRunnable != null) {
                                    handler.removeCallbacks(prolongedPressRunnable); // Cancel prolonged press
                                }
                            }
                            return false; // Don't consume the touch event
                        });
                        // Start the delayed action
                        handler.postDelayed(prolongedPressRunnable, LONG_PRESS_THRESHOLD);
                    }
                });
                return true;
            }

        });

        return binding.getRoot();
    }
}
