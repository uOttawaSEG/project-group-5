package com.example.projectgroup5.ui.home;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.projectgroup5.BuildConfig;
import com.example.projectgroup5.R;
import com.example.projectgroup5.database.DatabaseManager;
import com.example.projectgroup5.databinding.FragmentCreateEventBinding;
import com.example.projectgroup5.events.EventOption;
import com.example.projectgroup5.users.UserSession;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.CircularBounds;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CreateEventFragment extends Fragment {


    private FragmentCreateEventBinding binding;

    // Define a variable to hold the Places API key.
    private EditText editTextLocation;
    private String placeAddress = null;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 1;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCreateEventBinding.inflate(inflater, container, false);
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);

        // Define a variable to hold the Places API key.
        String apiKey = BuildConfig.PLACES_API_KEY;

        // Log an error if apiKey is not set.
        if (TextUtils.isEmpty(apiKey) || apiKey.equals("DEFAULT_API_KEY")) {
            Log.e("Places test", "No api key");
//            finish();
//            return;
        }

        // Initialize the SDK
        if (!Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled(this.getContext(), apiKey);
        }

        editTextLocation = binding.eventAddressInput;

        // the edit text is not editable but allow the user to open the autocomplete fragment when clicking on it
        editTextLocation.setOnClickListener(v -> {
            openAutocompleteActivity(AutocompleteSessionToken.newInstance());
        });

//        editTextLocation.setFocusable(false);

        // Set up Autocomplete widget for EditText
        setUpAutoComplete();



        Calendar calendar0 = Calendar.getInstance();
        calendar0.add(Calendar.DAY_OF_YEAR, 1);
        long minDate = calendar0.getTimeInMillis();

        final Timestamp[] startTime = new Timestamp[1];
        final Timestamp[] endTime = new Timestamp[1];



        setTimeStamp(startTime, calendar0, minDate, binding.getRoot().findViewById(R.id.pickStartTime));
        binding.getRoot().findViewById(R.id.pickEndTime).setOnClickListener(v -> {
            if (startTime[0] == null) {
                // pop up error message as start time is not set
                Toast.makeText(getContext(), "Set start time first!", Toast.LENGTH_SHORT).show();
                return;
            }
            timePicker(endTime, calendar0.get(Calendar.YEAR), calendar0.get(Calendar.MONTH), calendar0.get(Calendar.DAY_OF_MONTH), binding.getRoot().findViewById(R.id.pickEndTime));
            // set the text of the button to the time selected
                });

        binding.getRoot().findViewById(R.id.createEventCreateButton).setOnClickListener (v ->{
            EventOption option = EventOption.newEvent(binding.eventTitleInput.getText().toString(), binding.eventDescriptionInput.getText().toString(), placeAddress,  startTime[0], endTime[0], binding.autoAcceptSwitch.isChecked(), null, DatabaseManager.getDatabaseManager().getCurrentUserReference());
            if (option.holdsAnEvent()) {
                navController.popBackStack();
                Toast.makeText(getContext(), "Event created!", Toast.LENGTH_SHORT).show();
                // TODO add event to database here
            } else {
                Toast.makeText(getContext(), option.getError().toString(), Toast.LENGTH_SHORT).show();
            }
        });

        binding.getRoot().findViewById(R.id.cancelEventCreateButton).setOnClickListener (v ->{
            navController.popBackStack();
        });

        return binding.getRoot();
    }

    private void setTimeStamp(Timestamp[] timeChosen, Calendar calendar0, long minDate, Button button) {
        button.setOnClickListener (v ->{
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
                timePicker(timeChosen, year, month, dayOfMonth, button);
            }, calendar0.get(Calendar.YEAR), calendar0.get(Calendar.MONTH), calendar0.get(Calendar.DAY_OF_MONTH));

            // Set the minimum date
            datePickerDialog.getDatePicker().setMinDate(minDate);

            // Show the DatePickerDialog
            datePickerDialog.show();
        });
    }

    private void timePicker(Timestamp[] timeChosen, int year, int month, int dayOfMonth, Button button) {
        Calendar calendar1 = Calendar.getInstance();
        int hour = calendar1.get(Calendar.HOUR_OF_DAY);
        int minute = calendar1.get(Calendar.MINUTE);

        // Create a TimePickerDialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), (view, hourOfDay, minute1) -> {
            // Round the time to the nearest 30 minutes rounded up or down
            minute1 = (minute1 + 15) / 30 * 30;
            // Create a Calendar object and set the selected time
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth, hourOfDay, minute1, 0);
            // Create a Firebase Timestamp from the Calendar's time
            Timestamp timestamp = new Timestamp(calendar.getTime());
            timeChosen[0] = timestamp;
            Log.d("CreateEventFragment", "Selected time: " + timestamp);
            // Set the text of the button to the selected time
            button.setText(timestamp.toDate().toString());
        }, hour, minute, true);

        // Show the TimePickerDialog
        timePickerDialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setUpAutoComplete() {
        // Create an AutocompleteSessionToken to associate API requests
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        // Set up a listener to handle user input and show suggestions
        editTextLocation.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                // Trigger autocomplete when the EditText gains focus
                openAutocompleteActivity(token);
            }
        });
    }

    private void openAutocompleteActivity(AutocompleteSessionToken token) {
        // Use the Places API to show autocomplete suggestions
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS))
                .build(getContext());
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Get the selected place from the intent
                Place place = Autocomplete.getPlaceFromIntent(data);
                String placeName = place.getDisplayName();
                placeAddress = place.getFormattedAddress();

                // Show the place details in the EditText or log the result
                editTextLocation.setText(placeName);
                Log.d("Place", "Place: " + placeName + ", Address: " + placeAddress);
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.e("Places", "Error: " + status.getStatusMessage());
            }
        }
    }


}
