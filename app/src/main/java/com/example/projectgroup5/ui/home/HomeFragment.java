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
        } else if (UserSession.getInstance().getUserRepresentation().getUserType() == User.USER_TYPE_ADMIN) {
            navController.navigate(R.id.action_navigation_home_to_admin_lists_option_selector);
        } else {
            navController.navigate(R.id.action_navigation_home_to_home_not_logged_in);
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