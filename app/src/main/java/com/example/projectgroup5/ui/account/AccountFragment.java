package com.example.projectgroup5.ui.account;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.projectgroup5.R;
import com.example.projectgroup5.databinding.FragmentAccountBinding;
import com.example.projectgroup5.users.UserSession;

public class AccountFragment extends Fragment {

    private FragmentAccountBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
        // Currently this is fine, userId is not null but userRepresentation is null
        if (UserSession.getInstance().getUserId() == null || UserSession.getInstance().getUserRepresentation() == null) {
            navController.navigate(R.id.action_login_or_create_account);
        } else {
            navController.navigate(R.id.account_management);
        }

        binding = FragmentAccountBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // destroy the loginOptions
        Log.d("AccountFragment", "onDestroyView: ");
        binding = null;
    }
}