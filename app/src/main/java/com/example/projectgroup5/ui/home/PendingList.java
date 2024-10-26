package com.example.projectgroup5.ui.home;

import com.example.projectgroup5.databinding.FragmentPendingListBinding;
import com.example.projectgroup5.users.UserOptions;
import com.example.projectgroup5.users.UserSession;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.projectgroup5.users.User;

import androidx.fragment.app.Fragment;


public class PendingList extends Fragment{
    private FragmentPendingListBinding binding;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPendingListBinding.inflate(inflater, container, false);
//        UserSession.getInstance().getUserRepresentation().addUserToLayout(binding.pendingListLinearLayout, getContext());
        UserOptions.getPendingUsers(userIds -> {
            for (User user : userIds) {
                user.addUserToLayout(binding.pendingListLinearLayout, getContext());
            }
        });

        View root = binding.getRoot();
        return root;
    }
}
