package com.example.projectgroup5.events;

import android.content.Context;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import com.example.projectgroup5.R;
import com.example.projectgroup5.database.DatabaseManager;
import com.example.projectgroup5.users.Attendee;
import com.example.projectgroup5.users.User;
import com.example.projectgroup5.users.UserSession;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class EventAdapterForDisplay extends ArrayAdapter<Event> {

    Context context;

    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm");

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
            // we want to display the date in the format: "HH:mm dd/MM/yyyy"
            startTimeTextView.setText(dateFormat.format(event.getStartTime().toDate()));
        }
        // end time
        if (event.getEndTime() != null) {
            TextView endTimeTextView = convertView.findViewById(R.id.endTimeEntry);
            endTimeTextView.setText(dateFormat.format(event.getEndTime().toDate()));
        }

        // if the user is an attendee we get either the time conflict or the registration status and make the status icon visible with the correct color
        if (UserSession.getInstance().getUserRepresentation() != null && UserSession.getInstance().getUserRepresentation() instanceof Attendee attendee) {
            Log.d("EventAdapterForDisplay", "Attendee: " + attendee);
            // we must now get all the registrations that the attendee has made
            // if the attendee has the event in his registration list, we set the status icon to the registration status of the event
            if (attendee.getAttendeeRegistrations() != null) {
                Log.d("EventAdapterForDisplay", "Attendee registrations: " + attendee.getAttendeeRegistrations());
                List<DocumentReference> myRegistrations = new ArrayList<>(attendee.getAttendeeRegistrations());
                myRegistrations.retainAll(event.getRegistrations());
                if (!myRegistrations.isEmpty()) {
                    // we set the status icon to the registration status of the event
                    convertView.findViewById(R.id.statusIcon).setVisibility(View.VISIBLE);
                        convertView.findViewById(R.id.spaceWithinTheEventEntry).setVisibility(View.VISIBLE);
                    // we set the color to match the registration status
                    View finalConvertView1 = convertView;
                    // load the event registration id from cache
                    DatabaseManager.getDatabaseManager().getAttendanceToEvent(DatabaseManager.getDatabaseManager().getEventReference(event.getEventID()), attendance -> {
                        if (attendance.getResult() == null) {
                            finalConvertView1.findViewById(R.id.statusIcon).setVisibility(View.GONE);
                        } else {
                            finalConvertView1.findViewById(R.id.statusIcon).setVisibility(View.VISIBLE);
                            if (attendance.getResult().equals(User.ACCEPTED)) {
                                ImageView statusIcon = finalConvertView1.findViewById(R.id.statusIcon);
                                int color = ContextCompat.getColor(finalConvertView1.getContext(), android.R.color.holo_green_light);
                                statusIcon.setColorFilter(color, PorterDuff.Mode.SRC_IN);
                                // set the on long click listener
                                statusIcon.setOnLongClickListener(v -> {
                                        showCustomTooltip(finalConvertView1.findViewById(R.id.statusIcon), "Accepted");
                                    return true;
                                    });
                            } else if (attendance.getResult().equals(User.WAITLISTED)) {
                                ImageView statusIcon = finalConvertView1.findViewById(R.id.statusIcon);
                                int color = ContextCompat.getColor(finalConvertView1.getContext(), android.R.color.holo_blue_light);
                                statusIcon.setColorFilter(color, PorterDuff.Mode.SRC_IN);
                                // set the on long click listener
                                statusIcon.setOnLongClickListener(v -> {
                                        showCustomTooltip(finalConvertView1.findViewById(R.id.statusIcon), "Waitlisted");
                                    return true;
                                });
                            } else if (attendance.getResult().equals(User.REJECTED)) {
                                ImageView statusIcon = finalConvertView1.findViewById(R.id.statusIcon);
                                int color = ContextCompat.getColor(finalConvertView1.getContext(), android.R.color.holo_red_light);
                                statusIcon.setColorFilter(color, PorterDuff.Mode.SRC_IN);
                                // set the on long click listener
                                statusIcon.setOnLongClickListener(v -> {
                                        showCustomTooltip(finalConvertView1.findViewById(R.id.statusIcon), "Rejected");
                                        return true;
                                });
                            }

                        }
                    });
                }
            }
            // check if already visible
            if (convertView.findViewById(R.id.statusIcon).getVisibility() != View.VISIBLE) {
                if ((attendee.getEventCache() == null || attendee.getEventCache().isEmpty())) {
                    View finalConvertView = convertView;
                    DatabaseManager.getDatabaseManager().getAttendeeRegistrations(UserSession.getInstance().getUserRepresentation().getUserId(), eventIds -> {
                        if (eventIds.getResult() != null) {
                            // get all the events related to these registrations
                            AtomicInteger counter = new AtomicInteger(eventIds.getResult().size());
                            for (DocumentReference registrationRef : attendee.getAttendeeRegistrations()) {
                                DatabaseManager.getDatabaseManager().getEventFromRegistration(registrationRef, event1 -> {
                                    attendee.getEventCache().add(event1.getResult().getEvent());
                                    Log.w("EventAdapterForDisplay", "Event added to cache: " + event1.getResult().getEvent().getTitle());
                                    if (counter.decrementAndGet() == 0) {
                                        checkConflict(finalConvertView, attendee, event);
                                    }
                                });
                            }
                        }
                    });
                } else {
                    // now we check if there is a time conflict
                    checkConflict(convertView, attendee, event);
                }
            }
        }

        return convertView;
    }

    private void checkConflict(View convertView, Attendee attendee, Event event) {
        for (Event otherEvent : attendee.getEventCache()) {
            if (event.timeConflict(otherEvent)) {
                convertView.findViewById(R.id.statusIcon).setVisibility(View.VISIBLE);
                convertView.findViewById(R.id.spaceWithinTheEventEntry).setVisibility(View.VISIBLE);
                // we add the hover effect to the icon
                View finalConvertView = convertView;
                ImageView statusIcon = finalConvertView.findViewById(R.id.statusIcon);
                int color = ContextCompat.getColor(finalConvertView.getContext(), android.R.color.holo_orange_light);
                statusIcon.setColorFilter(color, PorterDuff.Mode.SRC_IN);
                // set the on long click listener
                statusIcon.setOnLongClickListener(v -> {
                    showCustomTooltip(finalConvertView.findViewById(R.id.statusIcon), "Time conflict with " + otherEvent.getTitle());
                    Log.e("EventAdapterForDisplay", "Time conflict between " + event.getTitle() + " with " + otherEvent.getTitle());
                    Log.e("EventAdapterForDisplay", "attendee event cache: " + attendee.getEventCache());
                    return true;
                });
                break;
            }
        }
    }

    private void showCustomTooltip(View anchorView, String tooltipTextMessage) {
        // Get the position of the statusIcon on screen
        int[] location = new int[2];
        anchorView.getLocationOnScreen(location);  // Get coordinates of statusIcon on the screen

        // Inflate the custom layout for Tooltip
        View popupView = LayoutInflater.from(anchorView.getContext()).inflate(R.layout.tooltip_layout, null);

        // Create PopupWindow
        PopupWindow popupWindow = new PopupWindow(popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        // Set up the Tooltip text
        TextView tooltipText = popupView.findViewById(R.id.tooltip_text);
        tooltipText.setText(tooltipTextMessage);

        // Calculate the position to show the PopupWindow
        int xPos = location[0] + anchorView.getWidth();  // Position it to the right of the icon
        int yPos = location[1];  // Position it at the same vertical level as the icon

        // Show PopupWindow near the statusIcon
        popupWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY, xPos-700, yPos-30);

        // Optionally, dismiss the popup after some time
        popupView.postDelayed(popupWindow::dismiss, 1500);
    }
}
