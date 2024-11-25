package com.example.projectgroup5.ui.home;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.projectgroup5.R;
import com.example.projectgroup5.database.DatabaseListener;
import com.example.projectgroup5.database.DatabaseManager;
import com.example.projectgroup5.databinding.FragmentAttendeeEventListBinding;
import com.example.projectgroup5.events.Event;
import com.example.projectgroup5.events.EventAdapterForDisplay;
import com.example.projectgroup5.events.EventOption;
import com.example.projectgroup5.users.Attendee;
import com.example.projectgroup5.users.Organizer;
import com.example.projectgroup5.users.User;
import com.example.projectgroup5.users.UserOptions;
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

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentAttendeeEventListBinding binding = FragmentAttendeeEventListBinding.inflate(inflater, container, false);
        // Initialize the ListView
        ListView listView = binding.attendeeListLayout;
        // get the navigator
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);

        // get the events from the database
        List<Event> events = new ArrayList<>();
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
                            if (events.isEmpty()) {
                                // set the textview text to show that there are no events
                                binding.statusBarText.setText("No events");
                            } else {
                                binding.statusBarText.setText("My registrations");
                            }
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
                    view.findViewById(R.id.eventAddressEntry).setVisibility(View.GONE);}
                else {
                    view.findViewById(R.id.eventAddressEntry).setVisibility(View.VISIBLE);
                }
            }});


        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            // will try to unregister the user from the event if possible
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
                    } else if (selectedEvent.getStartTime().toDate().before(new java.util.Date())) { // if the start time is in less than 24 hours we cant unregister
                        Log.w("AttendeeEventList", "Selected event: " + selectedEvent);
                        Toast.makeText(getContext(), "Less than 24 hours left", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(getContext(), "Keep holding to delete", Toast.LENGTH_SHORT).show();
                        // we display the option to delete the event and delete it
                        prolongedPressRunnable = () -> {
                            // delete the registration from the user, the event and then the registration object from firestore
                            DocumentReference eventRef = DatabaseManager.getDatabaseManager().getEventReference(selectedEvent.getEventID());
                            DatabaseManager.getDatabaseManager().getRegistrationReferenceToEvent(eventRef, registrationRefTask -> {
                                if (registrationRefTask.isSuccessful()) {
                                    DatabaseManager.getDatabaseManager().deleteRegistration(registrationRefTask.getResult(), task -> {
                                        Log.d("AttendeeEventList", "Registration deleted from firestore " + registrationRefTask.getResult().getId());
                                    selectedEvent.getRegistrations().remove(registrationRefTask.getResult());
                                    // update the adapter
                                        events.remove(selectedEvent);
                                        // remove the listener
                                        DatabaseListener.deleteEventStartListener(selectedEvent.getEventID());
                                        EventAdapterForDisplay eventOrganizerAdapter = new EventAdapterForDisplay(getContext(), events);
                                        if (events.isEmpty()) {
                                            // set the textview text to show that there are no events
                                            binding.statusBarText.setText("No events");
                                        } else {
                                            binding.statusBarText.setText("My registrations");
                                        }
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
