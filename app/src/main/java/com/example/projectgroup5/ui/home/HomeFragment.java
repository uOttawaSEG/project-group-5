package com.example.projectgroup5.ui.home;

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
import com.example.projectgroup5.databinding.FragmentHomeBinding;
import com.example.projectgroup5.database.DatabaseManager;
import com.example.projectgroup5.users.User;
import com.example.projectgroup5.users.UserSession;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);

        if (UserSession.getInstance().getUserId() == null || UserSession.getInstance().getUserRepresentation() == null) {
            navController.navigate(R.id.action_navigation_home_to_home_not_logged_in);
        } else {
            DatabaseManager.getDatabaseManager().getUserDataFromFirestore(DatabaseManager.USER_REGISTRATION_STATE, userRegistrationState -> {
                if (!(userRegistrationState.toString()).equals((User.ACCEPTED))) {
                    navController.navigate(R.id.action_navigation_home_to_home_not_registered_in);
                } else if (UserSession.getInstance().getUserRepresentation().getUserType().equals(User.USER_TYPE_ADMIN)) {
                    navController.navigate(R.id.action_navigation_home_to_admin_lists_option_selector);
                } else if (UserSession.getInstance().getUserRepresentation().getUserType().equals(User.USER_TYPE_ORGANIZER)) {
                    navController.navigate(R.id.action_navigation_home_to_organizer_option_selector);
                } else if (UserSession.getInstance().getUserRepresentation().getUserType().equals(User.USER_TYPE_ATTENDEE)) {
                    navController.navigate(R.id.action_navigation_home_to_attendee_event_list);
                }
            });
        }

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("HomeFragment", "onDestroyView: "); //TODO check this out
        binding = null;
    }
}