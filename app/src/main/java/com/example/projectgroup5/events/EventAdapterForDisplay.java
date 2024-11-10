package com.example.projectgroup5.events;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.projectgroup5.R;

import java.util.List;

public class EventAdapterForDisplay extends ArrayAdapter<Event> {

    Context context;

    public EventAdapterForDisplay(@NonNull Context context, @NonNull List<Event> objects) {
        super(context, 0, objects);
        this.context = context;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Event event = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.organizer_event_entry, parent, false);
        }
        // now we set all the fields
        // title
        if (event.getTitle() != null) {
            TextView titleTextView = convertView.findViewById(R.id.eventTitleEntry);
            titleTextView.setText(event.getTitle());
        }
        if (event.getDescription() != null) {
            TextView descriptionTextView = convertView.findViewById(R.id.eventDescriptionEntry);
            Log.d("EventAdapterForDisplay", "Description dash: " + event.getDescription());
            descriptionTextView.setText(event.getDescription());
        }
        // address
        if (event.getAddress() != null) {
            TextView addressTextView = convertView.findViewById(R.id.eventAddressEntry);
            addressTextView.setText(event.getAddress());
        }
        // start time
        if (event.getStartTime() != null) {
            TextView startTimeTextView = convertView.findViewById(R.id.startTimeEntry);
            startTimeTextView.setText(event.getStartTime().toDate().toString());
        }
        // end time
        if (event.getEndTime() != null) {
            TextView endTimeTextView = convertView.findViewById(R.id.endTimeEntry);
            endTimeTextView.setText(event.getEndTime().toDate().toString());
        }



        return convertView;
    }
}
