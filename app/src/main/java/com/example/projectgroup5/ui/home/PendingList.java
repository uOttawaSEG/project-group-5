package com.example.projectgroup5.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.projectgroup5.databinding.FragmentPendingListBinding;
import com.example.projectgroup5.users.User;
import com.example.projectgroup5.users.UserAdapterForAdminView;
import com.example.projectgroup5.users.UserOptions;

import java.util.ArrayList;
import java.util.List;


public class PendingList extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        com.example.projectgroup5.databinding.FragmentPendingListBinding binding = FragmentPendingListBinding.inflate(inflater, container, false);
        // Initialize the ListView
        ListView listView = binding.pendingListLayout;

        // Create a list to hold the users
        List<User> pendingUsers = new ArrayList<>();

        // Fetch rejected users
        UserOptions.getPendingUsers(userIds -> {
            pendingUsers.addAll(userIds);
            // Create the adapter and set it to the ListView
            UserAdapterForAdminView userAdapterForAdminView = new UserAdapterForAdminView(getContext(), pendingUsers);
            listView.setAdapter(userAdapterForAdminView);
        });

        return binding.getRoot();
    }
}
