package com.example.projectgroup5.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.projectgroup5.databinding.NotLoggedInHomeBinding;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class HomeNotLoggedInFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        com.example.projectgroup5.databinding.NotLoggedInHomeBinding binding = NotLoggedInHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

}
