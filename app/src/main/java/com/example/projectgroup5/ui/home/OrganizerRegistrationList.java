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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;


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
            if (isChecked) {
                AtomicReference<List<Event>> events = new AtomicReference<>(new ArrayList<>());
                if (onlyEvent) {
                    DatabaseManager.getDatabaseManager().changeEventAutoAccept(DatabaseManager.getDatabaseManager().getEventReference(selectedEvent.getEventID()), true);
                    // then we update all the registrations to accepted
                    events.get().add(selectedEvent);
                    acceptCorresponding(spinner, navController, events);
                } else {
                    DatabaseManager.getDatabaseManager().getOrganizerEvents(UserSession.getInstance().getUserId(), task -> {
                        if (task == null || !task.isSuccessful()) {
                            Log.e("EventOptions", "Failed to get organizer events");
                        } else {
                            Log.w("OrganizerRegistrationList", "Organizer Events: " + task.getResult());
                            events.set(task.getResult());
                            acceptCorresponding(spinner, navController, events);
                        }
                    });
                }
                // get the position of the spinner either 0 1 or 2


            } else if (onlyEvent) {
                // the switch has been set to false and we are in a single event mode
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
                                        registrationsList.clear();
                                        registrationsList.addAll(registrationsresult);
                                        RegistrationAdapterForOrganizerView userAdapter = new RegistrationAdapterForOrganizerView(getContext(), registrationsList);
                                        listView.setAdapter(userAdapter);
                                        Log.w("UserOptions", "Registrations accepted: " + registrationsList);
                                    },
                                    User.ACCEPTED,
                                    selectedEvent
                            );
                            break;
                        case 1:
                            UserOptions.getRegistrationsWithStatusToEvent(registrationsresult -> {
                                        registrationsList.clear();
                                        registrationsList.addAll(registrationsresult);
                                        RegistrationAdapterForOrganizerView userAdapter = new RegistrationAdapterForOrganizerView(getContext(), registrationsList);
                                        listView.setAdapter(userAdapter);
                                        Log.w("UserOptions", "Registrations waitlisted: " + registrationsList);
                                    }
                                    ,
                                    User.WAITLISTED,
                                    selectedEvent);
                            break;
                        case 2:
                            UserOptions.getRegistrationsWithStatusToEvent(registrationsresult -> {
                                        registrationsList.clear();
                                        registrationsList.addAll(registrationsresult);
                                        RegistrationAdapterForOrganizerView userAdapter = new RegistrationAdapterForOrganizerView(getContext(), registrationsList);
                                        listView.setAdapter(userAdapter);
                                        Log.w("UserOptions", "Registrations rejected: " + registrationsList);
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
                        Log.d("OrganizerRegistrationList", "All Organizer Events: " + events);
                        switch (position) {

                            case 0:
                                UserOptions.getRegistrationWithStatusToEvent(registrations -> {
                                            registrationsList.clear();
                                            registrationsList.addAll(registrations);
                                            RegistrationAdapterForOrganizerView userAdapter = new RegistrationAdapterForOrganizerView(getContext(), registrationsList);
                                            listView.setAdapter(userAdapter);
                                            Log.w("UserOptions", "Registrations accepted all: " + registrationsList);
                                        },
                                        User.ACCEPTED,
                                        events);
                                break;
                            case 1:
                                UserOptions.getRegistrationWithStatusToEvent(registrations -> {
                                            registrationsList.clear();
                                            registrationsList.addAll(registrations);
                                            RegistrationAdapterForOrganizerView userAdapter = new RegistrationAdapterForOrganizerView(getContext(), registrationsList);
                                            listView.setAdapter(userAdapter);
                                            Log.w("UserOptions", "Registrations waitlisted all: " + registrationsList);
                                        },
                                        User.WAITLISTED,
                                        events);
                                break;
                            case 2:
                                UserOptions.getRegistrationWithStatusToEvent(registrations -> {
                                            registrationsList.clear();
                                            registrationsList.addAll(registrations);
                                            RegistrationAdapterForOrganizerView userAdapter = new RegistrationAdapterForOrganizerView(getContext(), registrationsList);
                                            listView.setAdapter(userAdapter);
                                            Log.w("UserOptions", "Registrations rejected all: " + registrationsList);
                                        },
                                        User.REJECTED,
                                        events);
                                break;


                        }
                    });
                }
            }


            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Handle the case where nothing is selected
            }
        });

        return binding.getRoot();
    }

    private static void acceptCorresponding(Spinner spinner, NavController navController, AtomicReference<List<Event>> events) {
        String status = spinner.getSelectedItemPosition() == 0 ? User.ACCEPTED : spinner.getSelectedItemPosition() == 1 ? User.WAITLISTED : User.REJECTED;
        if (status.equals(User.ACCEPTED) || status.equals(User.WAITLISTED)) {
            Log.e("OrganizerRegistrationList", "Status of spinner: " + status);
            UserOptions.getRegistrationWithStatusToEvent(registrationsresult -> {
                for (Registration registration : registrationsresult) {
                    DatabaseManager.getDatabaseManager().changeAttendeeStatus(DatabaseManager.getDatabaseManager().getRegistrationReference(registration.getRegistrationId()), User.ACCEPTED);
                    Log.d("OrganizerRegistrationList", "Registration accepted: " + registration.getAttendee());
                }
                // now we reload the list by refreshing the fragment
                if (!registrationsresult.isEmpty()) {
                    //clear the event list and reload it
                    navController.popBackStack();
                    navController.navigate(R.id.organizer_registration_list);
                }
            }, User.WAITLISTED, events.get());
        }
        if (status.equals(User.ACCEPTED) || status.equals(User.REJECTED)) {
            UserOptions.getRegistrationWithStatusToEvent(registrationsresult -> {
                for (Registration registration : registrationsresult) {
                    DatabaseManager.getDatabaseManager().changeAttendeeStatus(DatabaseManager.getDatabaseManager().getRegistrationReference(registration.getRegistrationId()), User.ACCEPTED);
                    Log.d("OrganizerRegistrationList", "Registration accepted: " + registration.getAttendee());
                }
                // now we reload the list by refreshing the fragment
                if (!registrationsresult.isEmpty()) {
                    //clear the event list and reload it
                    navController.popBackStack();
                    navController.navigate(R.id.organizer_registration_list);
                }
            }, User.REJECTED, events.get());
        }
    }

    public static Event getSelectedEvent() {
        return selectedEvent;
    }

    public static void setSelectedEvent(Event event) {
        setOnlyEvent(true);
        selectedEvent = event;
    }

    public static void setGlobal() {
        setOnlyEvent(false);
    }

    private static void setOnlyEvent(boolean onlyEvent) {
        OrganizerRegistrationList.onlyEvent = onlyEvent;
    }
}
