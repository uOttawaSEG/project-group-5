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
import com.example.projectgroup5.databinding.FragmentAdminListsOptionSelectorBinding;

public class AdminOptionSelector extends Fragment {

    private FragmentAdminListsOptionSelectorBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAdminListsOptionSelectorBinding.inflate(inflater, container, false);
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);

        // set the buttons on click listeners for the accepted list button
        binding.getRoot().findViewById(R.id.acceptedListButton).setOnClickListener(v -> navController.navigate(R.id.action_list_options_selector_to_accepted_list));

        // same for the rejected list button
        binding.getRoot().findViewById(R.id.rejectedListButton).setOnClickListener(v -> navController.navigate(R.id.action_list_options_selector_to_rejected_list));

        // and finally the pending list button
        binding.getRoot().findViewById(R.id.pendingListButton).setOnClickListener(v -> navController.navigate(R.id.action_list_options_selector_to_pending_list));

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
