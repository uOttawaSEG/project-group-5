package com.example.projectgroup5.ui.account;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.projectgroup5.R;
import com.example.projectgroup5.databinding.FragmentAccountManagementBinding;
import com.example.projectgroup5.users.UserSession;

public class AccountManagementFragment extends Fragment {
    private FragmentAccountManagementBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAccountManagementBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        root.findViewById(R.id.logoutButton).setOnClickListener(v -> {
            // login the user using the email and password
            // if the login is successful, navigate to the dashboard fragment
            // if the login is not successful, show an error message
            // if the user is not logged in, show an error message
            UserSession.getInstance().logout();
            // go back to the login fragment
            Fragment loginFragment = new LoginFragment();
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment_activity_main, loginFragment)
                    .addToBackStack(loginFragment.getClass().getName())
                    .commit();
//
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
