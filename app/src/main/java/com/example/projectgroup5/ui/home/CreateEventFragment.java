package com.example.projectgroup5.ui.home;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.projectgroup5.R;
import com.example.projectgroup5.databinding.FragmentCreateEventBinding;
import com.example.projectgroup5.events.EventOption;
import com.google.firebase.Timestamp;

public class CreateEventFragment extends Fragment {


    private FragmentCreateEventBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCreateEventBinding.inflate(inflater, container, false);
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);

        Calendar calendar0 = Calendar.getInstance();
        calendar0.add(Calendar.DAY_OF_YEAR, 1);
        long minDate = calendar0.getTimeInMillis();

        final Timestamp[] startTime = new Timestamp[1];
        final Timestamp[] endTime = new Timestamp[1];

        // TODO auto accept and address
        setTimeStamp(startTime, calendar0, minDate, binding.getRoot().findViewById(R.id.pickStartTime));
        binding.getRoot().findViewById(R.id.pickEndTime).setOnClickListener(v -> {
            if (startTime[0] == null) {
                // pop up error message as start time is not set
                Toast.makeText(getContext(), "Set start time first!", Toast.LENGTH_SHORT).show();
                return;
            }
            timePicker(endTime, calendar0.get(Calendar.YEAR), calendar0.get(Calendar.MONTH), calendar0.get(Calendar.DAY_OF_MONTH));
                });

        binding.getRoot().findViewById(R.id.pickLocation).setOnClickListener(v -> {
            // log both dates
            Log.d("CreateEventFragment", "Start time: " + startTime[0]);
            Log.d("CreateEventFragment", "End time: " + endTime[0]);
        });

        binding.getRoot().findViewById(R.id.createEventCreateButton).setOnClickListener (v ->{
            EventOption option = EventOption.newEvent(binding.eventTitleInput.getText().toString(), binding.eventDescriptionInput.getText().toString(), "address",  startTime[0], endTime[0], null);
            if (option.holdsAnEvent()) {
                navController.popBackStack();
            } else {
                Toast.makeText(getContext(), option.getError().toString(), Toast.LENGTH_SHORT).show();
            }
        });

//
//        // same for the rejected list button
//        binding.getRoot().findViewById(R.id.Placeholder2).setOnClickListener(v -> navController.navigate(R.id.SOMEWHERE_TO_GO_TO));
//
//        // and finally the pending list button
//        binding.getRoot().findViewById(R.id.Placeholder3).setOnClickListener(v -> navController.navigate(R.id.SOMEWHERE_TO_GO_TO));

        return binding.getRoot();
    }

    private void setTimeStamp(Timestamp[] startTime, Calendar calendar0, long minDate, View button) {
        button.setOnClickListener (v ->{
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
                timePicker(startTime, year, month, dayOfMonth);
            }, calendar0.get(Calendar.YEAR), calendar0.get(Calendar.MONTH), calendar0.get(Calendar.DAY_OF_MONTH));

            // Set the minimum date
            datePickerDialog.getDatePicker().setMinDate(minDate);

            // Show the DatePickerDialog
            datePickerDialog.show();
        });
    }

    private void timePicker(Timestamp[] startTime, int year, int month, int dayOfMonth) {
        Calendar calendar1 = Calendar.getInstance();
        int hour = calendar1.get(Calendar.HOUR_OF_DAY);
        int minute = calendar1.get(Calendar.MINUTE);

        // Create a TimePickerDialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                // Round the time to the nearest 30 minutes
                minute = (minute / 30) * 30;
                // Create a Calendar object and set the selected time
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, dayOfMonth, hourOfDay, minute, 0);
                // Create a Firebase Timestamp from the Calendar's time
                Timestamp timestamp = new Timestamp(calendar.getTime());
                startTime[0] = timestamp;
                Log.d("CreateEventFragment", "Selected time: " + timestamp);
            }
        }, hour, minute, true);

        // Show the TimePickerDialog
        timePickerDialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
