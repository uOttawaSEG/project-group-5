package com.example.projectgroup5.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.projectgroup5.databinding.FragmentNotLoggedInHomeBinding;

public class HomeNotLoggedInFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentNotLoggedInHomeBinding binding = FragmentNotLoggedInHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

}
