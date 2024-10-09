package com.example.projectgroup5.ui.account;

import static com.example.projectgroup5.users.UserSession.USER_EMAIL;
import static com.example.projectgroup5.users.UserSession.USER_TYPE;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.projectgroup5.R;
import com.example.projectgroup5.databinding.FragmentAccountManagementBinding;
import com.example.projectgroup5.users.UserSession;

public class AccountManagementFragment extends Fragment {
    private FragmentAccountManagementBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAccountManagementBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // get the user name (email) and the user type (organizer or user or admin)
        // change the text of userWelcomeMessage based on the previous

//        if (UserSession.getInstance().getUserRepresentation() == null) {
//            Log.d("AccountManagementFragment", "UserSession is null");
//            // go back to the login fragment
//            Fragment loginFragment = new LoginFragment();  //TODO YONGEN pls change this to the login fragment
//            getActivity().getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.nav_host_fragment_activity_main, loginFragment)
//                    .addToBackStack(loginFragment.getClass().getName())
//                    .commit();
//        }
//        Log.d("firebase", "Updating user type");
//        UserSession.getInstance().updateUserType();
//        Log.d("firebase", "Retrieved user type: " + UserSession.getInstance().getUserRepresentation().getUserType());
        UserSession.getInstance().getUserData(USER_TYPE, new UserSession.FirebaseCallback<Object>() {
            @Override
            public void onCallback(Object userType) {
                Log.d("UserSession", "In the onCallback: " + userType);
                if (userType != null) {
                    // Create a User representation based on the user type

                    Log.d("UserSession", "User type UPDATED: " + userType);
                    UserSession.getInstance().getUserRepresentation().setUserType((int)(long)((Long) userType));
                    int intUserType = (int)(long)((Long) userType);
                    Log.d("firebase", "Retrieved user type: " + userType);
//                    String currentText = binding.userWelcomeMessage.getText().toString();
                    if (intUserType == UserSession.USER_TYPE_ORGANIZER) {
                        binding.userWelcomeMessage.setVisibility(View.VISIBLE);
                        Log.d("AccountManagementFragment", "usertype: organizer");
                        binding.userWelcomeMessage.setText("Welcome Organizer");
                    } else if (intUserType == UserSession.USER_TYPE_USER) {
                        binding.userWelcomeMessage.setVisibility(View.VISIBLE);
                        Log.d("AccountManagementFragment", "usertype: user");
                        binding.userWelcomeMessage.setText("Welcome User");
                    } else if (intUserType == UserSession.USER_TYPE_ADMIN) {
                        binding.userWelcomeMessage.setVisibility(View.VISIBLE);
                        binding.userWelcomeMessage.setText("Welcome Admin");
                    } else {
                        // unknown user type
                        binding.userWelcomeMessage.setVisibility(View.GONE);
                    }

                } else {
                    Log.e("UserSession", "User type not found");
                }
            }
        });

        UserSession.getInstance().getUserData(USER_EMAIL, new UserSession.FirebaseCallback<Object>() {
            @Override
            public void onCallback(Object userEmail) {
                Log.d("UserSession", "In the onCallback: " + userEmail);
                if (userEmail != null) {
                    // Create a User representation based on the user type

                    Log.d("UserSession", "User type UPDATED: " + userEmail);
                    UserSession.getInstance().getUserRepresentation().setUserEmail(String.valueOf(userEmail));
                        // Append the user email to the current text

                        Log.d("AccountManagementFragment", "usertype: organizer");
                        String newText = binding.userWelcomeMessage.getText().toString() + " " + String.valueOf(userEmail);
                        binding.userWelcomeMessage.setText(newText);
                } else {
                    Log.e("UserSession", "User type not found");
                }
            }
        });



        root.findViewById(R.id.logoutButton).setOnClickListener(v -> {
            // login the user using the email and password
            // if the login is successful, navigate to the dashboard fragment
            // if the login is not successful, show an error message
            // if the user is not logged in, show an error message
            UserSession.getInstance().logout();
            // go back to the login fragment
            Fragment loginFragment = new LoginFragment();
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment_activity_main, loginFragment)
                    .addToBackStack(loginFragment.getClass().getName())
                    .commit();
//
        });

        return root;
    }

    //


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
