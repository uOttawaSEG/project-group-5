package com.example.projectgroup5.ui.account;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.projectgroup5.R;
import com.example.projectgroup5.databinding.FragmentLoginOrCreateAccountBinding;

public class LoginOrCreate extends Fragment {

    private FragmentLoginOrCreateAccountBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLoginOrCreateAccountBinding.inflate(inflater, container, false);
        ViewGroup.LayoutParams params = binding.getRoot().getLayoutParams();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        binding.getRoot().setLayoutParams(params);
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);

        // set the buttons on click listeners for the login button and the create account button
        binding.getRoot().findViewById(R.id.loginButton).setOnClickListener(v -> navController.navigate(R.id.action_login_or_create_account_to_login));

        // same for the create account button
        binding.getRoot().findViewById(R.id.createAccountButton).setOnClickListener(v -> navController.navigate(R.id.action_login_or_create_account_to_create_account));

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
