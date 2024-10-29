package com.example.projectgroup5.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.projectgroup5.databinding.FragmentNotRegisteredHomeBinding;

public class HomeNotRegisteredFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentNotRegisteredHomeBinding binding = FragmentNotRegisteredHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

}
