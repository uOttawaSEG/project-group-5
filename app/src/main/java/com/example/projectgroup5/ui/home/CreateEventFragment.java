package com.example.projectgroup5.ui.home;


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
import android.widget.EditText;
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
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.Timestamp;

import java.util.Arrays;

public class CreateEventFragment extends Fragment {


    private FragmentCreateEventBinding binding;

    // Define a variable to hold the Places API key.
    private EditText editTextLocation;
    private ActivityResultLauncher<Intent> autocompleteLauncher;
    private String placeAddress = null;
    private final Calendar startCalendar = Calendar.getInstance();
    private final Calendar stopCalendar = Calendar.getInstance();
    private Timestamp startTime = new Timestamp(startCalendar.getTime());
    private Timestamp endTime = new Timestamp(stopCalendar.getTime());

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCreateEventBinding.inflate(inflater, container, false);
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);

        // Define a variable to hold the Places API key.
        String apiKey = BuildConfig.PLACES_API_KEY;

        // Log an error if apiKey is not set.
        if (TextUtils.isEmpty(apiKey) || apiKey.equals("DEFAULT_API_KEY")) {
            Log.e("Places test", "No api key");
            Toast.makeText(getContext(), "No api key", Toast.LENGTH_SHORT).show();
            navController.popBackStack();
        }

        // Initialize the SDK
        if (!Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled(this.getContext(), apiKey);
        }

        editTextLocation = binding.eventAddressInput;

        autocompleteLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // Handle the result of the Autocomplete Activity here
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        // You can retrieve the Place data here from the result
                        Place place = Autocomplete.getPlaceFromIntent(result.getData());
                        // Use the place object as needed
                        placeAddress = place.getFormattedAddress();
                        Log.d("CreateEventFragment", "Selected address: " + placeAddress);
                        editTextLocation.setText(place.getDisplayName());
                    }
                });

        // the edit text is not editable but allow the user to open the autocomplete fragment when clicking on it
        editTextLocation.setOnClickListener(v -> {
            // clear the error of the edit text
            editTextLocation.setError(null);
            openAutocompleteActivity();
        });


        startCalendar.add(Calendar.DAY_OF_YEAR, 1);
        long minDate = startCalendar.getTimeInMillis();



        binding.getRoot().findViewById(R.id.pickStartTime).setOnClickListener(v -> {
            // clear the error of the edit text
            binding.pickStartTime.setError(null);
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
                startCalendar.set(Calendar.YEAR, year);
                startCalendar.set(Calendar.MONTH, month);
                startCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                timePicker(startTime, binding.pickStartTime, startCalendar);

            }, startCalendar.get(Calendar.YEAR), startCalendar.get(Calendar.MONTH), startCalendar.get(Calendar.DAY_OF_MONTH));

            // Set the minimum date
            datePickerDialog.getDatePicker().setMinDate(minDate);

            // Show the DatePickerDialog
            datePickerDialog.show();
        });


        binding.getRoot().findViewById(R.id.pickEndTime).setOnClickListener(v -> {
            // clear the error of the edit text
            binding.pickEndTime.setError(null);
            if (startTime == null) {
                // pop up error message as start time is not set
                Toast.makeText(getContext(), "Set start time first!", Toast.LENGTH_SHORT).show();
                return;
            }
            // clear the error of the edit text

//            timePicker(endTime, calendar0.get(Calendar.YEAR), calendar0.get(Calendar.MONTH), calendar0.get(Calendar.DAY_OF_MONTH), binding.getRoot().findViewById(R.id.pickEndTime), calendar0);
            // set the text of the button to the time selected
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
                timePicker(endTime, binding.pickEndTime, stopCalendar);
            }, stopCalendar.get(Calendar.YEAR), stopCalendar.get(Calendar.MONTH), stopCalendar.get(Calendar.DAY_OF_MONTH));

            // Set the minimum date
            datePickerDialog.getDatePicker().setMinDate(startCalendar.getTimeInMillis());
            // Show the DatePickerDialog
            datePickerDialog.show();
        });


        binding.getRoot().findViewById(R.id.createEventCreateButton).setOnClickListener(v -> {
            EventOption option = EventOption.newEvent(binding.eventTitleInput.getText().toString(), binding.eventDescriptionInput.getText().toString(), placeAddress, startTime, endTime, binding.autoAcceptSwitch.isChecked(), null, DatabaseManager.getDatabaseManager().getCurrentUserReference());
            if (option.holdsAnEvent()) {
                navController.popBackStack();
                Toast.makeText(getContext(), "Event created!", Toast.LENGTH_SHORT).show();
                // TODO add event to database here
            } else {
                // based on the error add a warning to the corresponding field
                switch (option.getError()) {
                    case TITLE_EMPTY_ERROR:
                        binding.eventTitleInput.setError("Please enter a title");
                        break;
                    case DESCRIPTION_EMPTY_ERROR:
                        binding.eventDescriptionInput.setError("Please enter a description");
                        break;
                    case ADDRESS_EMPTY_ERROR:
                        editTextLocation.setError("Please enter an address");
                        break;
                    case START_TIME_EMPTY_ERROR:
                        binding.pickStartTime.setError("Please select a start time");
                        break;
                    case END_TIME_EMPTY_ERROR:
                        binding.pickEndTime.setError("Please select an end time");
                        break;
                    case START_TIME_PAST_ERROR:
                        binding.pickStartTime.setError("Start time cannot be in the past");
                        break;
                    case END_TIME_PAST_ERROR:
                        binding.pickEndTime.setError("End time cannot be in the past");
                        break;
                    case END_TIME_BEFORE_START_TIME_ERROR:
                        binding.pickEndTime.setError("End time cannot be before start time");
                        break;
                    case ORGANIZER_EMPTY_ERROR:
                        Toast.makeText(getContext(), "You are not logged in as an organizer", Toast.LENGTH_SHORT).show();
                        navController.popBackStack();
                        break;
                    default:
                        Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

        binding.getRoot().findViewById(R.id.cancelEventCreateButton).setOnClickListener(v -> {
            navController.popBackStack();
        });

        return binding.getRoot();
    }

    private void timePicker(Timestamp timeChosen, Button button, Calendar calendarToSet) {
        Calendar calendar1 = Calendar.getInstance();
        int hour = calendar1.get(Calendar.HOUR_OF_DAY);
        int minute = calendar1.get(Calendar.MINUTE);

        // Create a TimePickerDialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), (view, hourOfDay, minute1) -> {
            // Round the time to the nearest 30 minutes rounded up or down
            minute1 = (minute1 + 15) / 30 * 30;
            // Create a Calendar object and set the selected time
            calendarToSet.set(startCalendar.get(Calendar.YEAR), startCalendar.get(Calendar.MONTH), startCalendar.get(Calendar.DAY_OF_MONTH), hourOfDay, minute1, 0);
            // increase the day by 1 if the time is smaller than calendar
            if (calendarToSet.getTimeInMillis() <= startCalendar.getTimeInMillis() && calendarToSet != startCalendar) {
                calendarToSet.add(Calendar.DAY_OF_YEAR, 1);
            }
            // clear the Pick END time if the new start time is after the end time
            if (endTime != null && calendarToSet.getTimeInMillis() > endTime.toDate().getTime() && calendarToSet == startCalendar) {
                endTime = null;
                binding.pickEndTime.setText(R.string.pick_end_time);
            } else {
                Log.d("CreateEventFragment", "Selected time: " + calendarToSet.getTime().toString());
            }
            Timestamp timestamp = new Timestamp(calendarToSet.getTime());
            // check if the timestamp is before calendar0
            if (timeChosen == startTime)
                startTime = timestamp;
            else
                endTime = timestamp;
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

    private void openAutocompleteActivity() {
        // Use the Places API to show autocomplete suggestions
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN,
                Arrays.asList(Place.Field.ID, Place.Field.DISPLAY_NAME, Place.Field.FORMATTED_ADDRESS))
                .build(getContext());
        // Launch the autocomplete activity using the launcher initialized in OnCreate
        autocompleteLauncher.launch(intent);
    }
}
