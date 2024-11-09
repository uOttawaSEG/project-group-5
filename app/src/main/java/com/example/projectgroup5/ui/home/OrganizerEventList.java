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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.projectgroup5.R;
import com.example.projectgroup5.databinding.FragmentOrganizerEventListBinding;
import com.example.projectgroup5.events.Event;
import com.example.projectgroup5.events.EventAdapterForOrganizer;
import com.example.projectgroup5.events.EventOption;

import java.util.ArrayList;
import java.util.List;


public class OrganizerEventList extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentOrganizerEventListBinding binding = FragmentOrganizerEventListBinding.inflate(inflater, container, false);
        // Initialize the ListView
        ListView listView = binding.organizerListLayout;

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
                            events.addAll(eventIds);
                            EventAdapterForOrganizer userAdapterForAdminView = new EventAdapterForOrganizer(getContext(), events);
                            listView.setAdapter(userAdapterForAdminView);
                        });
                        break;
                    case 1:
                        EventOption.getPastEvents(eventIds -> {
                            events.addAll(eventIds);
                            EventAdapterForOrganizer userAdapterForAdminView = new EventAdapterForOrganizer(getContext(), events);
                            listView.setAdapter(userAdapterForAdminView);
                        });
                        break;
                    case 2:
                        EventOption.getCurrentEvents(eventIds -> {
                            events.addAll(eventIds);
                            EventAdapterForOrganizer userAdapterForAdminView = new EventAdapterForOrganizer(getContext(), events);
                            listView.setAdapter(userAdapterForAdminView);
                        });
                        break;
                    default:
                        // Default case
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Handle the case where nothing is selected
            }
        });

        return binding.getRoot();
    }
}
