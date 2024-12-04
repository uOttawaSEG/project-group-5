package com.example.projectgroup5.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.projectgroup5.databinding.FragmentAcceptedListBinding;
import com.example.projectgroup5.users.User;
import com.example.projectgroup5.users.UserAdapterForAdminView;
import com.example.projectgroup5.users.UserOptions;

import java.util.ArrayList;
import java.util.List;

public class AcceptedList extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        com.example.projectgroup5.databinding.FragmentAcceptedListBinding binding = FragmentAcceptedListBinding.inflate(inflater, container, false);
        // Initialize the ListView
        ListView listView = binding.acceptedListLayout;

        // Create a list to hold the users
        List<User> acceptedUsers = new ArrayList<>();

        // Fetch rejected users
        UserOptions.getAcceptedUsers(userIds -> {
            // list all the user ids in the list
            for (User userId : userIds) {
                Log.d("AcceptedList", "User ID: " + userId.getUserId());
            }
            acceptedUsers.addAll(userIds);
            // Create the adapter and set it to the ListView
            UserAdapterForAdminView userAdapterForAdminView = new UserAdapterForAdminView(getContext(), acceptedUsers);
            listView.setAdapter(userAdapterForAdminView);
        });

        return binding.getRoot();
    }

}
