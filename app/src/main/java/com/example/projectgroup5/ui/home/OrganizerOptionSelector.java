package com.example.projectgroup5.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.projectgroup5.R;
import com.example.projectgroup5.databinding.FragmentOrganizerOptionSelectorBinding;

public class OrganizerOptionSelector extends Fragment {

    private FragmentOrganizerOptionSelectorBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOrganizerOptionSelectorBinding.inflate(inflater, container, false);
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);

        // set the buttons on click listeners for the accepted list button
        binding.getRoot().findViewById(R.id.EventCreateButton).setOnClickListener(v -> navController.navigate(R.id.action_organizer_option_selector_to_create_event));
//
//        // same for the rejected list button
        binding.getRoot().findViewById(R.id.organizer_view_event_list).setOnClickListener(v -> navController.navigate(R.id.action_organizer_option_selector_to_organizer_event_list));
//
//        // and finally the pending list button
        binding.getRoot().findViewById(R.id.organizer_event_requests).setOnClickListener(v -> {
            OrganizerRegistrationList.setGlobal();
            navController.navigate(R.id.action_organizer_option_selector_to_organizer_registration_list);
        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Hide the back button for this fragment
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
