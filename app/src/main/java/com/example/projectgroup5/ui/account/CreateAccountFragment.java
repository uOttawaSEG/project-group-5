package com.example.projectgroup5.ui.account;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.example.projectgroup5.R;
import com.example.projectgroup5.databinding.FragmentAccountBinding;
import com.example.projectgroup5.databinding.FragmentDashboardBinding;
import com.example.projectgroup5.databinding.FragmentLoginBinding;
import com.example.projectgroup5.ui.search.DashboardFragment;
import com.example.projectgroup5.users.UserSession;

public class CreateAccountFragment extends Fragment {
    private FragmentCreateAccountBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        root.findViewById(R.id.confirmCredentialAndLoginButton).setOnClickListener(v -> {
            // login the user using the email and password
            // if the login is successful, navigate to the dashboard fragment
            // if the login is not successful, show an error message
            // if the user is not logged in, show an error message
            UserSession.getInstance().login(binding.emailInput.getText().toString(), binding.passwordInput.getText().toString(), (task) -> {
                if (task.isSuccessful()) {
                    UserSession.getInstance().setUserId(task.getResult().getUser().getUid());
                    Fragment dashboardFragment = new DashboardFragment();

                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.nav_host_fragment_activity_main, dashboardFragment)
                            .addToBackStack(dashboardFragment.getClass().getName())
                            .commit();
                } else {
                    // show an error message
                    binding.emailInput.setError("Invalid email or password");
                }
            });


//
        });
        root.findViewById(R.id.cancelButton).setOnClickListener(v -> {
            // go back
            Fragment accountFragment = new AccountFragment();

            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment_activity_main, accountFragment)
                    .addToBackStack(accountFragment.getClass().getName())
                    .commit();
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
