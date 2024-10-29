package com.example.projectgroup5.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.projectgroup5.R;
import com.example.projectgroup5.databinding.FragmentAttendeeOptionSelectorBinding;

public class AttendeeOptionSelector extends Fragment {

    private FragmentAttendeeOptionSelectorBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAttendeeOptionSelectorBinding.inflate(inflater, container, false);
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);

        // set the buttons on click listeners for the accepted list button
//        binding.getRoot().findViewById(R.id.Placeholder1).setOnClickListener(v -> navController.navigate(R.id.SOMEWHERE_TO_GO_TO));
//
//        // same for the rejected list button
//        binding.getRoot().findViewById(R.id.Placeholder2).setOnClickListener(v -> navController.navigate(R.id.SOMEWHERE_TO_GO_TO));
//
//        // and finally the pending list button
//        binding.getRoot().findViewById(R.id.Placeholder3).setOnClickListener(v -> navController.navigate(R.id.SOMEWHERE_TO_GO_TO));

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
