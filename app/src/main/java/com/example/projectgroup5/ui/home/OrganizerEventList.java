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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.projectgroup5.R;
import com.example.projectgroup5.database.DatabaseManager;
import com.example.projectgroup5.databinding.FragmentOrganizerEventListBinding;
import com.example.projectgroup5.events.Event;
import com.example.projectgroup5.events.EventAdapterForDisplay;
import com.example.projectgroup5.events.EventOption;
import com.example.projectgroup5.users.Organizer;
import com.example.projectgroup5.users.User;
import com.example.projectgroup5.users.UserOptions;
import com.example.projectgroup5.users.UserSession;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.List;


public class OrganizerEventList extends Fragment {
    private static final int LONG_PRESS_THRESHOLD = 3000; // 5 second for second action
    private final Handler handler = new Handler();
    private Runnable prolongedPressRunnable;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentOrganizerEventListBinding binding = FragmentOrganizerEventListBinding.inflate(inflater, container, false);
        // Initialize the ListView
        ListView listView = binding.organizerListLayout;
        // get the navigator
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);

        Spinner spinner = binding.getRoot().findViewById(R.id.organizerEventSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.event_type_for_organizer
                , android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedOption = parentView.getItemAtPosition(position).toString();
                Log.d("OrganizerEventList", "Selected option: " + selectedOption);
                List<Event> events = new ArrayList<>();

                // Fetch required events based on the selected option
                switch (position) {
                    case 0:
                        EventOption.getFutureEvents(eventIds -> {
                            events.clear();
                            events.addAll(eventIds);
                            EventAdapterForDisplay eventOrganizerAdapter = new EventAdapterForDisplay(getContext(), events);
                            listView.setAdapter(eventOrganizerAdapter);
                        });
                        break;
                    case 1:
                        EventOption.getPastEvents(eventIds -> {
                            events.clear();
                            events.addAll(eventIds);
                            EventAdapterForDisplay eventOrganizerAdapter = new EventAdapterForDisplay(getContext(), events);
                            listView.setAdapter(eventOrganizerAdapter);
                        });
                        break;
                    case 2:
                        EventOption.getCurrentEvents(eventIds -> {
                            events.clear();
                            events.addAll(eventIds);
                            EventAdapterForDisplay eventOrganizerAdapter = new EventAdapterForDisplay(getContext(), events);
                            listView.setAdapter(eventOrganizerAdapter);
                        });
                        break;
                    default:
                        // Default case
                        break;
                }
                // Set the Long Click Listener for the ListView items

                listView.setOnItemClickListener((parentView1, view, position1, id1) -> {
                    // go to the list of registrations if it is not empty
                    Event selectedEvent = (Event) parentView1.getItemAtPosition(position1);
                    if (selectedEvent.getRegistrations() != null && !selectedEvent.getRegistrations().isEmpty()) {
                        OrganizerRegistrationList.setSelectedEvent(selectedEvent);
                        navController.navigate(R.id.action_organizer_event_list_to_organizer_registration_list);
                    } else {
                        Toast.makeText(getContext(), "No registrations", Toast.LENGTH_SHORT).show();
                    }

                });

                listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {


                    @RequiresApi(api = Build.VERSION_CODES.R)
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parentView, View view, int position, long id) {
                        Log.d("OrganizerEventList", "Long click");
                        // Handle long click event
                        Event selectedEvent = (Event) parentView.getItemAtPosition(position);
                        // get the number of accepted registrations for the event
//                        List<Registration> attendees = new ArrayList<>();
                        Log.d("OrganizerEventList", "Selected event: " + selectedEvent);
                        UserOptions.getRegistrationsWithStatusToEvent(registrations -> {
//                            attendees.addAll(registrations);
                            Log.d("OrganizerEventList", "Attendees: " + registrations);
                            if (selectedEvent.getRegistrations() != null && !selectedEvent.getRegistrations().isEmpty() && !registrations.isEmpty()) {
//                                Toast.makeText(getContext(), "Cannot delete event with registrations", Toast.LENGTH_SHORT).show();
                                // we display move to the list of registrations
                                OrganizerRegistrationList.setSelectedEvent(selectedEvent);
                                navController.navigate(R.id.action_organizer_event_list_to_organizer_registration_list);
                            } else {
                                Toast.makeText(getContext(), "Keep holding to delete", Toast.LENGTH_SHORT).show();
                                getView().performHapticFeedback(HapticFeedbackConstants.GESTURE_START);
                                // we display the option to delete the event and delete it
                                // Set up prolonged press detection
                                prolongedPressRunnable = () -> {
                                    // delete the event from the list and from the database and from the user
                                    DocumentReference eventRef = DatabaseManager.getDatabaseManager().getEventReference(selectedEvent.getEventID());
                                    Log.d("OrganizerEventList", "Event ref ID: " + selectedEvent);
                                    Log.d("OrganizerEventList", "Event deleted from firestore " + eventRef.getId());
                                    // get a list of all the registrations for the event and delete them
                                    DatabaseManager.getDatabaseManager().getAllRegistrationToEvent(eventRef, task -> {
                                        if (task.isSuccessful()) {
                                            for (DocumentReference registrationRef : task.getResult()) {
                                                // delete all these registrations
                                                DatabaseManager.getDatabaseManager().deleteRegistration(registrationRef, task1 -> {
                                                    Log.d("OrganizerEventList", "Registration deleted from firestore " + registrationRef.getId());
                                                });
                                            }
                                            // delete the event from the organizer on firestore
                                            ((Organizer) UserSession.getInstance().getUserRepresentation()).removeEvent(eventRef);
                                            DatabaseManager.getDatabaseManager().removeEventFromOrganizer(eventRef, task2 -> {
                                                // delete the event from the listview
                                                DatabaseManager.getDatabaseManager().deleteEventFromFirestore(eventRef, task1 -> {
                                                    events.remove(selectedEvent);
                                                    EventAdapterForDisplay eventOrganizerAdapter = new EventAdapterForDisplay(getContext(), events);
                                                    listView.setAdapter(eventOrganizerAdapter);
                                                    getView().performHapticFeedback(HapticFeedbackConstants.GESTURE_END);
                                                });
                                            });
                                        }
                                    });

                                };

                                // Start the delayed action
                                handler.postDelayed(prolongedPressRunnable, LONG_PRESS_THRESHOLD);
                            }
                        }, User.ACCEPTED, selectedEvent);
                        return true;
                    }

                });
                listView.setOnTouchListener((v, event) -> {
                    if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                        if (prolongedPressRunnable != null) {
                            handler.removeCallbacks(prolongedPressRunnable); // Cancel prolonged press
                        }
                    }
                    return false; // Don't consume the touch event
                });
            }


            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Handle the case where nothing is selected
            }
        });

        return binding.getRoot();
    }
}
