package com.example.projectgroup5.ui.account;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.example.projectgroup5.R;
import com.example.projectgroup5.databinding.FragmentAccountBinding;
import com.example.projectgroup5.users.UserSession;

public class AccountFragment extends Fragment {

    private FragmentAccountBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

//        AccountViewModel accountViewModel =
//                new ViewModelProvider(this).get(AccountViewModel.class);

        // place the login in the center of the remaining space

        /*if (UserSession.getInstance().getUserId() == null) {
            Fragment loginOrCreateFragment = new LoginOrCreate();
            // add the loginOrCreateFragment to the container
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment_activity_main, loginOrCreateFragment)
                    .addToBackStack(loginOrCreateFragment.getClass().getName())
                    .commit();
        } else {
            Fragment accountManagementFragment = new AccountManagementFragment();
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment_activity_main, accountManagementFragment)
                    .addToBackStack(accountManagementFragment.getClass().getName())
                    .commit();

        }*/
        NavController navController = Navigation.findNavController(requireActivity(),R.id.nav_host_fragment_activity_main);
        if(UserSession.getInstance().getUserId() == null){
            navController.navigate(R.id.action_login_or_create_account);
        }else{
            navController.navigate(R.id.action_create_account);
        }



        binding = FragmentAccountBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.accountSettings;
//        accountViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // destroy the loginOptions

        Log.d("AccountFragment", "onDestroyView: ");
        binding = null;
    }
}