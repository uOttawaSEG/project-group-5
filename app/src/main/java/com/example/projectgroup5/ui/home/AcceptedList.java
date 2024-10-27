package com.example.projectgroup5.ui.home;
import com.example.projectgroup5.databinding.FragmentAcceptedListBinding;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.projectgroup5.R;
import com.example.projectgroup5.databinding.FragmentPendingListBinding;
import com.example.projectgroup5.users.User;
import com.example.projectgroup5.users.UserOptions;
import com.example.projectgroup5.users.UserSession;

public class AcceptedList extends Fragment{
    private FragmentAcceptedListBinding binding;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAcceptedListBinding.inflate(inflater, container, false);
        UserOptions.getAcceptedUsers(userIds -> {
            for (User user : userIds) {
                user.addUserToLayout(binding.acceptedListLinearLayout, getContext());
            }
        });

        return binding.getRoot();
    }

}
