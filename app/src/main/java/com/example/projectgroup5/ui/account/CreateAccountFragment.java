package com.example.projectgroup5.ui.account;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.projectgroup5.R;
import com.example.projectgroup5.databinding.FragmentCreateAccountBinding;
import com.example.projectgroup5.ui.search.DashboardFragment;
import com.example.projectgroup5.users.UserSession;

public class CreateAccountFragment extends Fragment {
    private FragmentCreateAccountBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCreateAccountBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        NavController navController = Navigation.findNavController(requireActivity(),R.id.nav_host_fragment_activity_main);
        // Switch between user and organizer
        root.findViewById(R.id.OrganizerVSUserSwitch).setOnClickListener(v -> {
                    if (binding.OrganizerVSUserSwitch.isChecked()) {
                        binding.editTextTextOrganisation.setVisibility(View.VISIBLE);
                    } else {
                        binding.editTextTextOrganisation.setVisibility(View.GONE);
                    }
                }
        );

        root.findViewById(R.id.confirmCredentialAndCreateButton).setOnClickListener(v -> {
            // login the user using the email and password
            // if the login is successful, navigate to the dashboard fragment
            // if the login is not successful, show an error message
            // if the user is not logged in, show an error message
            // first check if the data in the fields are valid
            if (binding.editTextTextEmailAddressUserCreate.getText().toString().isEmpty()) {
                binding.editTextTextEmailAddressUserCreate.setError("Please enter an email");
                return;
            }
            if (binding.editTextTextPasswordUserCreate.getText().toString().isEmpty()) {
                binding.editTextTextPasswordUserCreate.setError("Please enter a password");
                return;
            }
            UserSession.getInstance().createUser(binding.editTextTextEmailAddressUserCreate.getText().toString(), binding.editTextTextPasswordUserCreate.getText().toString(), (task) -> {
                if (task.isSuccessful()) {
                    UserSession.getInstance().setUserId(task.getResult().getUser().getUid());
                    //Fragment dashboardFragment = new DashboardFragment();

                    /*getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.nav_host_fragment_activity_main, dashboardFragment)
                            .addToBackStack(dashboardFragment.getClass().getName())
                            .commit();*/
                    navController.navigate(R.id.action_create_account_to_dashboard);
                } else {
                    Log.d("CreateAccountFragment", "onCreateView: " + task.getException());
                    // provide more information about the error
                    if (task.getException() != null) {
                        Log.d("CreateAccountFragment", "onCreateView: " + task.getException().getMessage());
                    }

                    // show an error message
                    binding.editTextTextEmailAddressUserCreate.setError("Invalid email or password");
                    String message = task.getException().getMessage();
                    binding.editTextTextPasswordUserCreate.setError(message.substring(message.lastIndexOf("[") + 2).replaceAll("]", "").stripTrailing());
                }
            });


//
        });
        root.findViewById(R.id.cancelButtonCreate).setOnClickListener(v -> {
            // go back
            //Fragment accountFragment = new AccountFragment();

            /*getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment_activity_main, accountFragment)
                    .addToBackStack(accountFragment.getClass().getName())
                    .commit();*/
            navController.navigate(R.id.action_create_account_to_account);
        });
        return root;
    }

    //


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
