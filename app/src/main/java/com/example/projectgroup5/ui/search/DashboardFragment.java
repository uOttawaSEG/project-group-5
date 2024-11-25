package com.example.projectgroup5.ui.search;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.projectgroup5.MainActivity;
import com.example.projectgroup5.R;
import com.example.projectgroup5.database.DatabaseManager;
import com.example.projectgroup5.databinding.FragmentDashboardBinding;
import com.example.projectgroup5.users.User;
import com.example.projectgroup5.users.UserSession;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        //TODO if there is errors with starting up the project, its probably because the user is not instantiated
        // If this frame requires the user to be logged in try creating a new view where it is not in the navigation graph
        // Also change the starting point of the navigation graph to another fragment

        if (MainActivity.complete) {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
            navController.navigate(R.id.action_search_event_dashboard_to_search_event_list);
        }

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        MainActivity.setOnCompleteListener( task -> {
            if (task.isSuccessful()) {

                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
                navController.navigate(R.id.action_search_event_dashboard_to_search_event_list);
            }
        });

        return root;
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