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

import java.text.SimpleDateFormat;
import java.util.List;

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
            // we must now get all the registrations that the attendee has made
            // if the attendee has the event in his registration list, we set the status icon to the registration status of the event
            if (attendee.getAttendeeRegistrations() != null) {
                if (attendee.getAttendeeRegistrations().contains(DatabaseManager.getDatabaseManager().getEventReference(event.getEventID()))) {
                    // we set the status icon to the registration status of the event
                    convertView.findViewById(R.id.statusIcon).setVisibility(View.VISIBLE);
                        convertView.findViewById(R.id.spaceWithinTheEventEntry).setVisibility(View.VISIBLE);
                    // we set the color to match the registration status
                    View finalConvertView1 = convertView;
                    DatabaseManager.getDatabaseManager().getAttendanceToEvent(attendee.getUserId(), DatabaseManager.getDatabaseManager().getEventReference(event.getEventID()), attendance -> {
                        if (attendance == null) {
                            finalConvertView1.findViewById(R.id.statusIcon).setVisibility(View.GONE);
                            Log.e("EventAdapterForDisplay", "Attendance is null");
                        } else {
                            finalConvertView1.findViewById(R.id.statusIcon).setVisibility(View.VISIBLE);
                            if (attendance.getResult().equals(User.ACCEPTED)) {
                                ImageView statusIcon = finalConvertView1.findViewById(R.id.statusIcon);
                                int color = ContextCompat.getColor(finalConvertView1.getContext(), android.R.color.holo_green_light);
                                statusIcon.setColorFilter(color, PorterDuff.Mode.SRC_IN);
                                // set the on long click listener
                                statusIcon.setOnLongClickListener(v -> {
                                        showCustomTooltip(finalConvertView1.findViewById(R.id.statusIcon), "Accepted");
                                    return false;
                                    });
                            } else if (attendance.getResult().equals(User.WAITLISTED)) {
                                ImageView statusIcon = finalConvertView1.findViewById(R.id.statusIcon);
                                int color = ContextCompat.getColor(finalConvertView1.getContext(), android.R.color.holo_blue_light);
                                statusIcon.setColorFilter(color, PorterDuff.Mode.SRC_IN);
                                // set the on long click listener
                                statusIcon.setOnLongClickListener(v -> {
                                        showCustomTooltip(finalConvertView1.findViewById(R.id.statusIcon), "Waitlisted");
                                    return false;
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
            if (convertView.findViewById(R.id.statusIcon).getVisibility() != View.VISIBLE && attendee.getEventCache() == null) {
                DatabaseManager.getDatabaseManager().getEvents(eventIds -> {
                    if (eventIds == null) {
                    } else {
                        attendee.setEventCache(eventIds.getResult());
                    }
                });
            } else {
                // now we check if there is a time conflict
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
                            showCustomTooltip(finalConvertView.findViewById(R.id.statusIcon), "Time conflict");
                            return true;
                        });
                        break;
                    }
                }
            }
        }

        return convertView;
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
        popupWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY, xPos - 300, yPos - 100);

        // Optionally, dismiss the popup after some time
        popupView.postDelayed(popupWindow::dismiss, 1500); // Dismiss after 1.5 seconds
    }
}
