package com.example.projectgroup5.ui.home;

import com.example.projectgroup5.databinding.FragmentPendingListBinding;
import com.example.projectgroup5.databinding.FragmentRejectedListBinding;
import com.example.projectgroup5.users.User;
import com.example.projectgroup5.users.UserOptions;
import com.example.projectgroup5.users.UserSession;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

public class RejectedList extends Fragment{
    private FragmentRejectedListBinding binding;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRejectedListBinding.inflate(inflater, container, false);
//        UserSession.getInstance().getUserRepresentation().addUserToLayout(binding.rejectedListLinearLayout, getContext());
        UserOptions.getRejectedUsers(userIds -> {
            for (User user : userIds) {
                user.addUserToLayout(binding.rejectedListLinearLayout, getContext());
            }
        });

        View root = binding.getRoot();
        return root;
    }

}
