package com.example.projectgroup5.ui.home;


import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.HapticFeedbackConstants;
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
import com.example.projectgroup5.events.EventOptional;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
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
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm");

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCreateEventBinding.inflate(inflater, container, false);
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);

        // Define a variable to hold the Places API key.
        String apiKey = BuildConfig.PLACES_API_KEY;

        // Log an error if apiKey is not set.
        if (TextUtils.isEmpty(apiKey)) {
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
            // remove focus from edit text fields
            binding.eventDescriptionInput.clearFocus();
            binding.eventTitleInput.clearFocus();
            // clear the error of the edit text
            editTextLocation.setError(null);
            openAutocompleteActivity();
        });


        startCalendar.add(Calendar.DAY_OF_YEAR, 1);
        long minDate = startCalendar.getTimeInMillis();


        binding.getRoot().findViewById(R.id.pickStartTime).setOnClickListener(v -> {
            // remove focus from edit text fields
            binding.eventDescriptionInput.clearFocus();
            binding.eventTitleInput.clearFocus();
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
            // remove focus from edit text fields
            binding.eventDescriptionInput.clearFocus();
            binding.eventTitleInput.clearFocus();
            // clear the error of the edit text
            binding.pickEndTime.setError(null);
            // set the text of the button to the time selected
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> timePicker(endTime, binding.pickEndTime, stopCalendar), stopCalendar.get(Calendar.YEAR), stopCalendar.get(Calendar.MONTH), stopCalendar.get(Calendar.DAY_OF_MONTH));

            // Set the minimum date
            datePickerDialog.getDatePicker().setMinDate(startCalendar.getTimeInMillis());
            // Show the DatePickerDialog
            datePickerDialog.show();
        });


        binding.getRoot().findViewById(R.id.createEventCreateButton).setOnClickListener(v -> {
            this.getView().performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
            EventOptional option = EventOptional.newEvent(binding.eventTitleInput.getText().toString(), binding.eventDescriptionInput.getText().toString(), placeAddress, startTime, endTime, binding.autoAcceptSwitch.isChecked(), DatabaseManager.getDatabaseManager().getCurrentUserReference());
            if (option.holdsAnEvent()) {
                this.getView().performHapticFeedback(HapticFeedbackConstants.CONFIRM);


                DatabaseManager.getDatabaseManager().createNewEvent(option.getEvent(), (task1) -> {
//                    Log.d("CreateEventFragment", "Event created");
                    // we now have an event with all the fields filled in
                    // add the event to the organizer's list of events
                    if (!task1.isSuccessful()) {
                        Log.e("CreateEventFragment", "Event creation failed");
                        Toast.makeText(getContext(), "Event creation failed", Toast.LENGTH_SHORT).show();
                        navController.popBackStack();
                    } else {
                        Log.d("CreateEventFragment", "Event created now adding to organizer");
                        DatabaseManager.getDatabaseManager().addEventToOrganizer(task1.getResult(), task2 -> {
                            if (!task2.isSuccessful()) {
                                Log.e("CreateEventFragment", "Error adding event to organizer");
                            } else {

                                Toast.makeText(getContext(), "Event created!", Toast.LENGTH_SHORT).show();

                            }
                            navController.popBackStack();
                        });
                    }
                });
            } else {
                this.getView().performHapticFeedback(HapticFeedbackConstants.REJECT);
                // based on the error add a warning to the corresponding field
                switch (option.getError()) {
                    case TITLE_EMPTY:
                        binding.eventTitleInput.setError("Please enter a title");
                        // set the focus on the title field
                        binding.eventTitleInput.requestFocus();
                        break;
                    case TITLE_BADLY_FORMATTED:
                        binding.eventTitleInput.setError("Please enter a valid title");
                        // set the focus on the title field
                        binding.eventTitleInput.requestFocus();
                        break;
                    case DESCRIPTION_EMPTY:
                        binding.eventDescriptionInput.setError("Please enter a description");
                        // set the focus on the description field
                        binding.eventDescriptionInput.requestFocus();
                        break;
                    case ADDRESS_EMPTY:
                        editTextLocation.setError("Please enter an address");
                        break;
                    case START_TIME_EMPTY:
                        binding.pickStartTime.setError("Please select a start time");
                        break;
                    case END_TIME_EMPTY:
                        binding.pickEndTime.setError("Please select an end time");
                        break;
                    case START_TIME_PAST:
                        binding.pickStartTime.setError("Start time cannot be in the past");
                        break;
                    case END_TIME_PAST:
                        binding.pickEndTime.setError("End time cannot be in the past");
                        break;
                    case END_TIME_BEFORE_START_TIME:
                        binding.pickEndTime.setError("End time cannot be before start time");
                        break;
                    case ORGANIZER_EMPTY:
                        Toast.makeText(getContext(), "You are not logged in as an organizer", Toast.LENGTH_SHORT).show();
                        navController.popBackStack();
                        break;
                    default:
                        Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

        binding.getRoot().findViewById(R.id.cancelEventCreateButton).setOnClickListener(v -> navController.popBackStack());

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
            if (calendarToSet.getTimeInMillis() <= startCalendar.getTimeInMillis() + 50000 && calendarToSet != startCalendar) {
                calendarToSet.add(Calendar.DAY_OF_YEAR, 1);
            }
            // clear the Pick END time if the new start time is after the end time
            if (endTime != null && calendarToSet.getTimeInMillis() >= endTime.toDate().getTime() && calendarToSet == startCalendar) {
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
            button.setText(dateFormat.format(timestamp.toDate()));
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
