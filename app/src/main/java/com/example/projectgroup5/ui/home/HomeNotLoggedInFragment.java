package com.example.projectgroup5.ui.home;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.projectgroup5.databinding.NotLoggedInHomeBinding;
import androidx.fragment.app.Fragment;

public class HomeNotLoggedInFragment extends Fragment{
    private NotLoggedInHomeBinding binding;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = NotLoggedInHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        return root;
    }

}
