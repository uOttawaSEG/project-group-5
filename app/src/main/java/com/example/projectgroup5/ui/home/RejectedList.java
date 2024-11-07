package com.example.projectgroup5.ui.home;

import com.example.projectgroup5.databinding.FragmentRejectedListBinding;
import com.example.projectgroup5.users.User;
import com.example.projectgroup5.users.UserAdapterForAdminView;
import com.example.projectgroup5.users.UserOptions;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

public class RejectedList extends Fragment {
    private FragmentRejectedListBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRejectedListBinding.inflate(inflater, container, false);
        // Initialize the ListView
        ListView listView = binding.rejectedListLayout;

        // Create a list to hold the users
        List<User> rejectedUsers = new ArrayList<>();

        // Fetch rejected users
        UserOptions.getRejectedUsers(userIds -> {
            rejectedUsers.addAll(userIds);
            // Create the adapter and set it to the ListView
            UserAdapterForAdminView userAdapterForAdminView = new UserAdapterForAdminView(getContext(), rejectedUsers);
            listView.setAdapter(userAdapterForAdminView);
        });


        return binding.getRoot();
    }

}
