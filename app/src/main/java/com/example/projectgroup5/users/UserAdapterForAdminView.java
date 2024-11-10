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

import java.util.List;

public class UserAdapterForAdminView extends ArrayAdapter<User> {

    private Context context;

    // Constructor for the adapter
    public UserAdapterForAdminView(@NonNull Context context, List<User> users) {
        super(context, 0, users);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View customView, @NonNull ViewGroup parent) {
        // Reuse existing view if possible
        if (customView == null) {
            customView = LayoutInflater.from(context).inflate(R.layout.admin_account_entry, parent, false);
        }

        Log.d("UserAdapterForAdminView", "getView called for position: " + position);

        // Get the user for the current position
        User user = getItem(position);
//        if (position <= 0 && position < getCount())
        if (user == null) {
            Log.e("UserAdapterForAdminView", "User is null at position : " + position + " for item : " + getItem(position) );
            return customView;
        }


        customView.setId(user.getUserId().hashCode());
        // set get the data from firebase if possible
        View finalCustomView = customView;
        DatabaseManager.getDatabaseManager().getAllUserDataFromFirestore(user.getUserId(), value -> {
            if (value != null) {
                if (value.containsKey(DatabaseManager.USER_ADDRESS)) {
                    user.setUserAddress(value.get(DatabaseManager.USER_ADDRESS).toString());
                    TextView userAddressTextView = finalCustomView.findViewById(R.id.homeAddressEntry);
                    userAddressTextView.setText(user.getUserAddress());
                }
                if (value.containsKey(USER_ORGANIZATION_NAME)) {
                    if (user instanceof Organizer organizer) {
                        organizer.setUserOrganizationName(value.get(USER_ORGANIZATION_NAME).toString());
                        TextView userOrganizationNameTextView = finalCustomView.findViewById(R.id.organizationNameEntry);
                        userOrganizationNameTextView.setVisibility(View.VISIBLE);
                        userOrganizationNameTextView.setText(organizer.getUserOrganizationName());
                    }
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
                if (value.containsKey(DatabaseManager.USER_REGISTRATION_STATE)) {
                    String userRegistrationState = value.get(DatabaseManager.USER_REGISTRATION_STATE).toString();
                    switch (userRegistrationState) {
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
                }


            }
        });

        Button rejectButton = customView.findViewById(R.id.rejectUserButton);
        rejectButton.setOnClickListener(v -> {
            this.remove(user);
            // Handle reject button click
            DatabaseManager.getDatabaseManager().storeUserValueToFirestore(user.getUserId(), DatabaseManager.USER_REGISTRATION_STATE, REJECTED, null);
        });
        Button acceptButton = customView.findViewById(R.id.acceptUserButton);
        acceptButton.setOnClickListener(v -> {
            this.remove(user);
            // Handle accept button click
            DatabaseManager.getDatabaseManager().storeUserValueToFirestore(user.getUserId(), DatabaseManager.USER_REGISTRATION_STATE, ACCEPTED, null);
        });

        return customView;
    }
}
