package com.example.projectgroup5.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.projectgroup5.R;
import com.example.projectgroup5.databinding.FragmentHomeBinding;
import com.example.projectgroup5.users.UserSession;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
        // Currently this isateException: Binary XML file line #30 in com.example.projectgroup5:l fine, userId is not null but userRepresentation is null
        //TODO
        if(UserSession.getInstance().getUserId() == null || UserSession.getInstance().getUserRepresentation() == null){
//            navController.navigate(R.id.action_login_or_create_account);
            navController.navigate(R.id.action_navigation_home_to_home_not_logged_in);

        }else{
            navController.navigate(R.id.action_navigation_home_to_admin_lists_option_selector);

        }




//        accountViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }
//        HomeViewModel homeViewModel =
//                new ViewModelProvider(this).get(HomeViewModel.class);
//
//        binding = FragmentHomeBinding.inflate(inflater, container, false);
//        View root = binding.getRoot();
//
//        final TextView textView = binding.textHome;
//        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
//        return root;
//    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("HomeFragment", "onDestroyView: "); //TODO check this out
        binding = null;
    }
}