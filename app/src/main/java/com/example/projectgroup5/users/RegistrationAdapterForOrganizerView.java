package com.example.projectgroup5.users;

import static com.example.projectgroup5.database.DatabaseManager.USER_ORGANIZATION_NAME;
import static com.example.projectgroup5.users.User.ACCEPTED;
import static com.example.projectgroup5.users.User.REJECTED;
import static com.example.projectgroup5.users.User.WAITLISTED;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.projectgroup5.R;
import com.example.projectgroup5.database.DatabaseManager;
import com.example.projectgroup5.events.Registration;

import java.util.List;

public class RegistrationAdapterForOrganizerView extends ArrayAdapter<Registration> {

    private final Context context;
    private final List<Registration> registrations;

    // Constructor for the adapter
    public RegistrationAdapterForOrganizerView(@NonNull Context context, List<Registration> registrations) {
        super(context, 0, registrations);
        this.context = context;
        this.registrations = registrations;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View customView, @NonNull ViewGroup parent) {
        // Reuse existing view if possible
        if (customView == null) {
            customView = LayoutInflater.from(context).inflate(R.layout.admin_account_entry, parent, false);
        }

        // Get the user for the current position
        Registration registration = getItem(position);

        customView.setId(registration.getRegistrationId().hashCode());
        // set get the data from firebase if possible
        View finalCustomView = customView;
        DatabaseManager.getDatabaseManager().getEventReference(registration.getEvent().getId()).get().addOnSuccessListener(value -> {
            TextView titleTextView = finalCustomView.findViewById(R.id.titleEntry);
            titleTextView.setText(value.getString(DatabaseManager.EVENT_TITLE));
            titleTextView.setVisibility(View.VISIBLE);
            //TODO look into this to check if buttons are alright
        });

                switch (registration.getRegistrationStatus()) {
                    case REJECTED: {
                        Button rejectButton = finalCustomView.findViewById(R.id.rejectUserButton);
                        rejectButton.setVisibility(View.GONE);
                        Button acceptButton = finalCustomView.findViewById(R.id.acceptUserButton);
                        acceptButton.setVisibility(View.VISIBLE);
                        break;
                    }
                    case ACCEPTED: {
                        Button rejectButton = finalCustomView.findViewById(R.id.rejectUserButton);
                        rejectButton.setVisibility(View.GONE);
                        Button acceptButton = finalCustomView.findViewById(R.id.acceptUserButton);
                        acceptButton.setVisibility(View.GONE);
                        break;
                    }
                    case WAITLISTED: {
                        Button rejectButton = finalCustomView.findViewById(R.id.rejectUserButton);
                        rejectButton.setVisibility(View.VISIBLE);
                        Button acceptButton = finalCustomView.findViewById(R.id.acceptUserButton);
                        acceptButton.setVisibility(View.VISIBLE);
                        break;
                    }
            }
        // get the user from the registration
        User.newUserFromDatabase(registration.getAttendee().getId(), userTask -> {
            User user;
            if (userTask.isSuccessful())
                user = userTask.getResult();
            else {
                Log.e("UserAdapterForAdminView", "Failed to create user from database, user ID: " + registration.getAttendee().getId());
                return;
            }
            // TODO add a title field to the registration display

            DatabaseManager.getDatabaseManager().getAllUserDataFromFirestore(user.getUserId(), value -> {
                if (value != null) {
                    if (value.containsKey(DatabaseManager.USER_ADDRESS)) {
                        user.setUserAddress(value.get(DatabaseManager.USER_ADDRESS).toString());
                        TextView userAddressTextView = finalCustomView.findViewById(R.id.homeAddressEntry);
                        userAddressTextView.setText(user.getUserAddress());
                    }
                    if (value.containsKey(DatabaseManager.USER_ADDRESS)) {
                        user.setUserAddress(value.get(DatabaseManager.USER_ADDRESS).toString());
                        TextView userAddressTextView = finalCustomView.findViewById(R.id.homeAddressEntry);
                        userAddressTextView.setText(user.getUserAddress());
                    }
                    if (value.containsKey(DatabaseManager.USER_FIRST_NAME)) {
                        user.setUserFirstName(value.get(DatabaseManager.USER_FIRST_NAME).toString());
                        TextView userFirstNameTextView = finalCustomView.findViewById(R.id.firstNameEntry);
                        userFirstNameTextView.setText(user.getFirstName());
                    }
                    if (value.containsKey(DatabaseManager.USER_LAST_NAME)) {
                        user.setUserLastName(value.get(DatabaseManager.USER_LAST_NAME).toString());
                        TextView userLastNameTextView = finalCustomView.findViewById(R.id.lastNameEntry);
                        userLastNameTextView.setText(user.getLastName());
                    }
                    if (value.containsKey(DatabaseManager.USER_EMAIL)) {
                        user.setUserEmail(value.get(DatabaseManager.USER_EMAIL).toString());
                        TextView userEmailTextView = finalCustomView.findViewById(R.id.emailAddressEntry);
                        userEmailTextView.setText(user.getUserEmail());
                    }
                    if (value.containsKey(DatabaseManager.USER_PHONE)) {
                        user.setUserPhoneNumber(Long.valueOf(value.get(DatabaseManager.USER_PHONE).toString().replace("\"", "")));
                        TextView userPhoneNumberTextView = finalCustomView.findViewById(R.id.phoneNumberEntry);
                        userPhoneNumberTextView.setText(user.getPhoneNumber() + "");
                    }

                }

            });
        });

        Button rejectButton = customView.findViewById(R.id.rejectUserButton);
        rejectButton.setOnClickListener(v -> {
            this.remove(registration);
            // Handle reject button click
            DatabaseManager.getDatabaseManager().changeAttendeeStatus(DatabaseManager.getDatabaseManager().getRegistrationReference(registration.getRegistrationId()), User.REJECTED);
        });
        Button acceptButton = customView.findViewById(R.id.acceptUserButton);
        acceptButton.setOnClickListener(v -> {
            this.remove(registration);
            // Handle accept button click
            DatabaseManager.getDatabaseManager().changeAttendeeStatus(DatabaseManager.getDatabaseManager().getRegistrationReference(registration.getRegistrationId()), ACCEPTED);
        });

        return customView;
    }
}
