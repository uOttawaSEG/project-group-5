package com.example.projectgroup5.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.projectgroup5.R;
import com.example.projectgroup5.database.DatabaseManager;
import com.example.projectgroup5.databinding.FragmentOrganizerRegistrationListBinding;
import com.example.projectgroup5.events.Event;
import com.example.projectgroup5.events.Registration;
import com.example.projectgroup5.users.User;
import com.example.projectgroup5.users.RegistrationAdapterForOrganizerView;
import com.example.projectgroup5.users.UserOptions;
import com.example.projectgroup5.users.UserSession;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.List;


public class OrganizerRegistrationList extends Fragment {

    private static Event selectedEvent;
    private static boolean onlyEvent = false;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentOrganizerRegistrationListBinding binding = FragmentOrganizerRegistrationListBinding.inflate(inflater, container, false);
        // Initialize the ListView
        ListView listView = binding.organizerRegistrationListLayout;

        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);

        Spinner spinner = binding.getRoot().findViewById(R.id.organizerRegistrationSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.user_registration_status
                , android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        Switch acceptAllUserSwitch = binding.getRoot().findViewById(R.id.acceptAllUserSwitch);
        // set the switch to the correct state
        if (onlyEvent) {
            acceptAllUserSwitch.setChecked(selectedEvent.isAutoAccept());
        } else {
            acceptAllUserSwitch.setChecked(false);
        }

        acceptAllUserSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // if the switch is checked, we accept all the registrations if only event is false, otherwise we accept all the registrations for the selected event
            // then we update the database autoAccept field
            if (isChecked){
                if (onlyEvent) {
                    DatabaseManager.getDatabaseManager().changeEventAutoAccept(DatabaseManager.getDatabaseManager().getEventReference(selectedEvent.getEventID()), true);
                    // then we update all the registrations to accepted
                    UserOptions.getRegistrationsWithStatusToEvent(registrationsresult -> {
                        for (Registration registration : registrationsresult) {
                            DatabaseManager.getDatabaseManager().changeAttendeeStatus(DatabaseManager.getDatabaseManager().getRegistrationReference(registration.getRegistrationId()), User.ACCEPTED);
                        }
                        // now we reload the list by refreshing the fragment
                        navController.navigate(R.id.action_organizer_registration_list_self);
                    },
                            User.WAITLISTED,
                            selectedEvent);
                } else {
                    DatabaseManager.getDatabaseManager().getOrganizerEvents(UserSession.getInstance().getUserId(), task -> {
                        List<Event> events;
                        if (task == null || !task.isSuccessful()) {
                            Log.e("EventOptions", "Failed to get organizer events");
                            return;
                        } else {
                            events = task.getResult();

                            UserOptions.getRegistrationWithStatusToEvent(registrationsresult -> {
                                for (Registration registration : registrationsresult) {
                                    DatabaseManager.getDatabaseManager().changeAttendeeStatus(DatabaseManager.getDatabaseManager().getRegistrationReference(registration.getRegistrationId()), User.ACCEPTED);
                                }
                                // now we reload the list by refreshing the fragment
                                navController.navigate(R.id.action_organizer_registration_list_self);
                            }, User.WAITLISTED, events);
                        }

                });
            }} else if (onlyEvent) {
                DatabaseManager.getDatabaseManager().changeEventAutoAccept(DatabaseManager.getDatabaseManager().getEventReference(selectedEvent.getEventID()), false);
                // then we update all the registrations to accepted
            }

        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedOption = parentView.getItemAtPosition(position).toString();
                Log.d("OrganizerEventList", "Selected option: " + selectedOption);
                List<Registration> registrationsList = new ArrayList<>();

                // Fetch all the user with the corresponding status
                if (onlyEvent) {
                    switch (position) {
                        case 0:
                            UserOptions.getRegistrationsWithStatusToEvent(registrationsresult -> {
                                        registrationsList.addAll(registrationsresult);
                                        RegistrationAdapterForOrganizerView userAdapter = new RegistrationAdapterForOrganizerView(getContext(), registrationsList);
                                listView.setAdapter(userAdapter);
                            },
                                    User.ACCEPTED,
                                    selectedEvent
                            );
                            break;
                        case 1:
                            UserOptions.getRegistrationsWithStatusToEvent(registrationsresult -> {
                                        registrationsList.addAll(registrationsresult);
                                RegistrationAdapterForOrganizerView userAdapter = new RegistrationAdapterForOrganizerView(getContext(), registrationsList);
                                listView.setAdapter(userAdapter);
                            }
                                    ,
                                    User.WAITLISTED,
                                    selectedEvent);
                            break;
                        case 2:
                            UserOptions.getRegistrationsWithStatusToEvent(registrationsresult -> {
                                        registrationsList.addAll(registrationsresult);
                                RegistrationAdapterForOrganizerView userAdapter = new RegistrationAdapterForOrganizerView(getContext(), registrationsList);
                                listView.setAdapter(userAdapter);
                            },
                                    User.REJECTED,
                                    selectedEvent);
                            break;
                        default:
                            // Default case
                            break;
                    }
                } else {
                    // get all the events and their registrations for each user
                    // get all the events of the organizer from the database
                    DatabaseManager.getDatabaseManager().getOrganizerEvents(UserSession.getInstance().getUserId(), task -> {
                            List<Event> events;
                            if (task == null || !task.isSuccessful()) {
                            Log.e("EventOptions", "Failed to get organizer events");
                            return;
                        } else {
                                events = task.getResult();
                        }
                        switch (position) {

                            case 0:
                                UserOptions.getRegistrationWithStatusToEvent(registrations -> {
                                            for (Registration registration : registrations) {
                                                // get the user associated with the registration
                                            }
                                        },
                                        User.ACCEPTED,
                                        events);
                            case 1:
                                UserOptions.getRegistrationWithStatusToEvent(registrations -> {
                                            for (Registration registration : registrations) {
                                                // get the user associated with the registration
                                            }
                                        },
                                        User.WAITLISTED,
                                        events);
                            case 2:
                                UserOptions.getRegistrationWithStatusToEvent(registrations -> {
                                            for (Registration registration : registrations) {
                                                // get the user associated with the registration
                                            }
                                        },
                                        User.REJECTED,
                                        events);



                        }
                    });
                }
                // Set the Long Click Listener for the ListView items
                listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parentView, View view, int position, long id) {
                        // Handle long click event
                        Event selectedEvent = (Event) parentView.getItemAtPosition(position);
                        if (selectedEvent.getRegistrations() != null && selectedEvent.getRegistrations().size() > 0) {
                            Toast.makeText(getContext(), "Registrations for event", Toast.LENGTH_SHORT).show();
                            // we display move to the list of registrations

                        } else {
                        Toast.makeText(getContext(), "No registrations for event", Toast.LENGTH_SHORT).show();

                            // we display the option to delete the event and delete it

                        }
                        return true; // Return true to indicate the event is consumed
                    }
                });
            }



            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Handle the case where nothing is selected
            }
        });

        return binding.getRoot();
    }

    public static Event getSelectedEvent() {
        return selectedEvent;
    }

    public static void setSelectedEvent(Event event) {
        setOnlyEvent(false);
        selectedEvent = event;
    }

    public static void setGlobal(Event event) {
        setOnlyEvent(false);
    }

    public static void setOnlyEvent(boolean onlyEvent) {
        OrganizerRegistrationList.onlyEvent = onlyEvent;
    }
}
