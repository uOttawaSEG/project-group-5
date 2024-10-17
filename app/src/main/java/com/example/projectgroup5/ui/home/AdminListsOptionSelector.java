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
import com.example.projectgroup5.databinding.FragmentAdminListsOptionSelectorBinding;

public class AdminListsOptionSelector extends Fragment {

    private FragmentAdminListsOptionSelectorBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAdminListsOptionSelectorBinding.inflate(inflater, container, false);
        ViewGroup.LayoutParams params = binding.getRoot().getLayoutParams();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        binding.getRoot().setLayoutParams(params);
        NavController navController = Navigation.findNavController(requireActivity(),R.id.nav_host_fragment_activity_main);
        // set the buttons on click listeners for the login button and the create account button
        binding.getRoot().findViewById(R.id.acceptedListButton).setOnClickListener(v -> {
            //Fragment loginFragment = new LoginFragment();
            /*getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment_activity_main, loginFragment)
                    .addToBackStack(loginFragment.getClass().getName())
                    .commit();*/
            navController.navigate(R.id.action_list_options_selector_to_accepted_list);

        });

        // same for the create account button
        binding.getRoot().findViewById(R.id.rejectedListButton).setOnClickListener(v -> {
            //Fragment createAccountFragment = new CreateAccountFragment();
            /*getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment_activity_main, createAccountFragment)
                    .addToBackStack(createAccountFragment.getClass().getName())
                    .commit();*/
            navController.navigate(R.id.action_list_options_selector_to_rejected_list);
        });

        binding.getRoot().findViewById(R.id.pendingListButton).setOnClickListener(v -> {
            //Fragment createAccountFragment = new CreateAccountFragment();
            /*getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment_activity_main, createAccountFragment)
                    .addToBackStack(createAccountFragment.getClass().getName())
                    .commit();*/
            navController.navigate(R.id.action_list_options_selector_to_pending_list);
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
