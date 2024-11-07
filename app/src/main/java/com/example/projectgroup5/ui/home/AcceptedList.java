package com.example.projectgroup5.ui.home;

import com.example.projectgroup5.databinding.FragmentAcceptedListBinding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.projectgroup5.users.User;
import com.example.projectgroup5.users.UserAdapterForAdminView;
import com.example.projectgroup5.users.UserOptions;

import java.util.ArrayList;
import java.util.List;

public class AcceptedList extends Fragment {
    private FragmentAcceptedListBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAcceptedListBinding.inflate(inflater, container, false);
        // Initialize the ListView
        ListView listView = binding.acceptedListLayout;

        // Create a list to hold the users
        List<User> acceptedUsers = new ArrayList<>();

        // Fetch rejected users
        UserOptions.getAcceptedUsers(userIds -> {
            acceptedUsers.addAll(userIds);
            // Create the adapter and set it to the ListView
            UserAdapterForAdminView userAdapterForAdminView = new UserAdapterForAdminView(getContext(), acceptedUsers);
            listView.setAdapter(userAdapterForAdminView);
        });

        return binding.getRoot();
    }

}
