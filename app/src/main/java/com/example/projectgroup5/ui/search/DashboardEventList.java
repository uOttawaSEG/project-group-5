package com.example.projectgroup5.ui.search;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SearchEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.projectgroup5.MainActivity;
import com.example.projectgroup5.R;
import com.example.projectgroup5.database.DatabaseListener;
import com.example.projectgroup5.database.DatabaseManager;
import com.example.projectgroup5.databinding.FragmentDashboardEventListBinding;
import com.example.projectgroup5.events.Event;
import com.example.projectgroup5.events.EventAdapterForDisplay;
import com.example.projectgroup5.events.EventOption;
import com.example.projectgroup5.events.Registration;
import com.example.projectgroup5.ui.home.OrganizerRegistrationList;
import com.example.projectgroup5.users.Attendee;
import com.example.projectgroup5.users.Organizer;
import com.example.projectgroup5.users.User;
import com.example.projectgroup5.users.UserOptions;
import com.example.projectgroup5.users.UserSession;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class DashboardEventList extends Fragment {
    private static final int LONG_PRESS_THRESHOLD = 2000; // 5 second for second action
    private final Handler handler = new Handler();
    private Runnable prolongedPressRunnable;

    @RequiresApi(api = Build.VERSION_CODES.R)
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentDashboardEventListBinding binding = FragmentDashboardEventListBinding.inflate(inflater, container, false);
        // Initialize the SwipeRefreshLayout
        SwipeRefreshLayout swipeRefreshLayout = binding.getRoot().findViewById(R.id.swipeRefreshLayout);
        // Initialize the ListView
        ListView listView = binding.searchListLayout;
        // get the navigator
        SearchView searchView = binding.getRoot().findViewById(R.id.event_search_view);
        searchView.setQueryHint("Search for items");
        List<Event> events = new ArrayList<>();
        DatabaseManager.getDatabaseManager().getEvents(eventIds -> {
            Log.d("DashboardEventList", "Event before ids: " + eventIds.getResult());
            if (eventIds == null) {
            } else {
                Log.d("DashboardEventList", "Event ids: " + eventIds.getResult());
                events.addAll(eventIds.getResult());
                // remove the past events
                events.removeIf(event -> event.getStartTime().toDate().before(new java.util.Date()));
                // now we must sort the events by starting date
                events.sort(Comparator.comparing(Event::getStartTime));
                EventAdapterForDisplay eventOrganizerAdapter = new EventAdapterForDisplay(getContext(), events);
                listView.setAdapter(eventOrganizerAdapter);
            }
        });
        // ---------------------------
        // Disable swipe refresh initially
        swipeRefreshLayout.setEnabled(false);

// Add an onScrollListener to check if the ListView is at the top
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // Only enable swipe refresh when the list is fully at the top
                if (scrollState == SCROLL_STATE_IDLE) {
                    // Ensure the first visible position is 0 and the top of the first child is 0
                    if (listView.getFirstVisiblePosition() == 0 && listView.getChildAt(0).getTop() == 0) {
                        swipeRefreshLayout.setEnabled(true); // Enable swipe to refresh
                    } else {
                        swipeRefreshLayout.setEnabled(false); // Disable swipe to refresh
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                // Not necessary to handle this, the state change is enough
            }
        });

// Set up the swipe-to-refresh listener
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Simulate refreshing events (you can add actual logic to refresh data from the database)
            DatabaseManager.getDatabaseManager().getEvents(eventIds -> {
                if (eventIds != null) {
                    events.clear();
                    events.addAll(eventIds.getResult());
                    events.removeIf(event -> event.getStartTime().toDate().before(new java.util.Date()));
                    events.sort(Comparator.comparing(Event::getStartTime));

                    EventAdapterForDisplay eventOrganizerAdapter = new EventAdapterForDisplay(getContext(), events);
                    listView.setAdapter(eventOrganizerAdapter);
                    Toast.makeText(getContext(), "Refreshed", Toast.LENGTH_SHORT).show();
                }
                // Stop the refresh animation once the data is loaded
                swipeRefreshLayout.setRefreshing(false);
            });
        });

        // Add listener for when query changes
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (UserSession.getInstance().getUserRepresentation() instanceof Attendee) {
                    DatabaseManager.getDatabaseManager().getEventsThatMatchQuery(query, eventIds -> {
                        if (eventIds == null) {
                        } else {
                            events.clear();
                            events.addAll(eventIds);
                            EventAdapterForDisplay eventOrganizerAdapter = new EventAdapterForDisplay(getContext(), events);
                            listView.setAdapter(eventOrganizerAdapter);
                        }
                    });
                }
                // sort the event using the query with the closest test to the query first
                events.sort((o1, o2) -> {
                    if (o1.getTitle().toLowerCase().contains(query.toLowerCase())) {
                        return -1;
                    } else if (o2.getTitle().toLowerCase().contains(query.toLowerCase())) {
                        return 1;
                    } else {
                        return 0;
                    }
                });
                EventAdapterForDisplay eventOrganizerAdapter = new EventAdapterForDisplay(getContext(), events);
                listView.setAdapter(eventOrganizerAdapter);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if (query == null || query.isEmpty()) {
//                    List<Event> events = new ArrayList<>();
//                    DatabaseManager.getDatabaseManager().getEvents(eventIds -> {
//                        Log.d("DashboardEventList", "Event before ids: " + eventIds.getResult());
//                        if (eventIds == null) {
//                        } else {
//                            Log.d("DashboardEventList", "Event ids: " + eventIds.getResult());
//                            events.clear();
//                            events.addAll(eventIds.getResult());
//                            EventAdapterForDisplay eventOrganizerAdapter = new EventAdapterForDisplay(getContext(), events);
//                            listView.setAdapter(eventOrganizerAdapter);
//                        }
//                    });
                    if (UserSession.getInstance().getUserRepresentation() instanceof Attendee) {
                        DatabaseManager.getDatabaseManager().getEventsThatMatchQuery(query, eventIds -> {
                            if (eventIds == null) {
                            } else {
                                events.clear();
                                events.addAll(eventIds);
                                EventAdapterForDisplay eventOrganizerAdapter = new EventAdapterForDisplay(getContext(), events);
                                listView.setAdapter(eventOrganizerAdapter);
                            }
                        });
                    }
                    // now we must sort the events by starting date
                    events.removeIf(event -> event.getStartTime().toDate().before(new java.util.Date()));
                    if (searchView.getQuery().toString().isEmpty()) {
                        events.sort(Comparator.comparing(Event::getStartTime));
                    } else {
                        events.sort((o1, o2) -> {
                            if (o1.getTitle().toLowerCase().contains(query.toLowerCase())) {
                                return -1;
                            } else if (o2.getTitle().toLowerCase().contains(query.toLowerCase())) {
                                return 1;
                            } else {
                                return 0;
                            }
                        });
                    }
                    EventAdapterForDisplay eventOrganizerAdapter = new EventAdapterForDisplay(getContext(), events);
                    listView.setAdapter(eventOrganizerAdapter);
                    return true;
                }
                return false;
            }
        });


        // set on click listener if the user is an attendee
        listView.setOnItemClickListener((parentView, view, position, id) -> {
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

        listView.setOnItemLongClickListener((parentView, view, position, id) -> {

            // Handle long click event
            // if the user is not an instanceof attendee return
            if (UserSession.getInstance().getUserRepresentation() == null || !(UserSession.getInstance().getUserRepresentation() instanceof Attendee)) {
                return false;
            }

            // get the attendee registration status to the app
            if (!UserSession.getInstance().getUserRepresentation().getUserRegistrationState().equals(User.ACCEPTED)) {
                return false;
            }
            // if the event has a time conflict with the other events, we dont register the user
            if (view.findViewById(R.id.statusIcon).getVisibility() == (View.VISIBLE)) {
                Toast.makeText(getContext(), "Event has a time conflict", Toast.LENGTH_SHORT).show();
                return true;
            }
            Event selectedEvent = (Event) parentView.getItemAtPosition(position);
            this.getView().performHapticFeedback(HapticFeedbackConstants.GESTURE_START);
            Toast.makeText(getContext(), "Keep pressing to register", Toast.LENGTH_SHORT).show();
            // we display the option to delete the event and delete it
            // Set up prolonged press detection
            Log.d("DashboardEventList", "Runnable set");
            prolongedPressRunnable = () -> {
                Log.d("DashboardEventList", "Long press detected");
                // delete the event from the list and from the database and from the user
                DocumentReference eventRef = DatabaseManager.getDatabaseManager().getEventReference(selectedEvent.getEventID());
                DocumentReference userRef = DatabaseManager.getDatabaseManager().getUserReference(UserSession.getInstance().getUserRepresentation().getUserId());
                Log.d("DashboardEventList", "Event ref ID: " + selectedEvent);
                Log.d("DashboardEventList", "Event deleted from firestore " + eventRef.getId());
                // check if there is a registration that already exists for the event and user
                // register the event for the user and remove it from the event list that is displayed
                // register the attendee for the event
                // create a new registration
                Registration registration = new Registration(userRef, selectedEvent.isAutoAccept() ? User.ACCEPTED : User.WAITLISTED, eventRef);
                DatabaseManager.getDatabaseManager().createNewRegistration(registration, task -> {
                    if (task.isSuccessful()) {
                        Log.d("DashboardEventList", "Registration created");
                    }
                    DocumentReference registrationRef = task.getResult();
                    DatabaseManager.getDatabaseManager().addRegistrationToEvent(eventRef, registrationRef, task2 -> {
                        if (task2.isSuccessful()) {
                            Log.d("DashboardEventList", "Registration created");
                            DatabaseManager.getDatabaseManager().addEventAttendee(eventRef, registrationRef, task3 -> {
                                Log.d("DashboardEventList", "Event attendee added");
                                if (task3.isSuccessful()) {
                                    Log.d("DashboardEventList", "Event attendee added2");
                                    // update the UserSession with the new event
                                    ((Attendee) UserSession.getInstance().getUserRepresentation()).addRegistration(registrationRef);
                                    // add the event to the user's list of events
                                    DatabaseManager.getDatabaseManager().addRegistrationToAttendee(userRef.getId(), registrationRef, task4 -> {
                                        Toast.makeText(getContext(), "Event registered", Toast.LENGTH_LONG).show();
                                        this.getView().performHapticFeedback(HapticFeedbackConstants.GESTURE_END);
                                        // add the listener to the event to send a notification 24 hours before
                                        DatabaseListener.addEventStartListener(MainActivity.getInstance(), selectedEvent, registration);
                                        Log.d("DashboardEventList", "Event registered successfully and removed from list");
                                        String query = searchView.getQuery().toString();
//                                        if (UserSession.getInstance().getUserRepresentation() instanceof Attendee) {
                                        DatabaseManager.getDatabaseManager().getEventsThatMatchQuery(query, eventIds -> {
                                            if (eventIds == null) {
                                            } else {
                                                events.clear();
                                                events.addAll(eventIds);
                                                events.removeIf(event -> event.getStartTime().toDate().before(new java.util.Date()));
                                                if (searchView.getQuery().toString().isEmpty()) {
                                                    events.sort(Comparator.comparing(Event::getStartTime));
                                                } else {
                                                    events.sort((o1, o2) -> {
                                                        if (o1.getTitle().toLowerCase().contains(query.toLowerCase())) {
                                                            return -1;
                                                        } else if (o2.getTitle().toLowerCase().contains(query.toLowerCase())) {
                                                            return 1;
                                                        } else {
                                                            return 0;
                                                        }
                                                    });
                                                }
                                                ((Attendee) UserSession.getInstance().getUserRepresentation()).clearEventCache();
//                                    events.remove(selectedEvent);
                                                EventAdapterForDisplay eventOrganizerAdapter = new EventAdapterForDisplay(getContext(), events);
                                                listView.setAdapter(eventOrganizerAdapter);
                                            }
                                        });
//                                        }


                                    });
                                } else {
                                    Log.d("DashboardEventList", "Event attendee not added232");
                                }
                            });
                        }
                    });
                });
            };


            // Start the delayed action
            Log.d("DashboardEventList", "Runnable started");
            handler.postDelayed(prolongedPressRunnable, LONG_PRESS_THRESHOLD);


            listView.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    if (prolongedPressRunnable != null) {
                        handler.removeCallbacks(prolongedPressRunnable); // Cancel prolonged press
                    }
                }
                return false; // Don't consume the touch event
            });
            return true;


        });


//
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parentView) {
//                // Handle the case where nothing is select
        return binding.getRoot();
    }

}
