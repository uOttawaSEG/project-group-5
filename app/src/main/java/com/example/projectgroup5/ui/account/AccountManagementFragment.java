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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.projectgroup5.R;
import com.example.projectgroup5.databinding.FragmentAccountManagementBinding;
import com.example.projectgroup5.users.UserSession;

public class AccountManagementFragment extends Fragment {
    private FragmentAccountManagementBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAccountManagementBinding.inflate(inflater, container, false);
        NavController navController = Navigation.findNavController(requireActivity(),R.id.nav_host_fragment_activity_main);
        View root = binding.getRoot();

        // user representation comes out as null
        if (UserSession.getInstance().getUserId() == null || UserSession.getInstance().getUserRepresentation() == null) {
            navController.navigate(R.id.account);
            Log.d("AccountManagementFragment", "User is not logged in");
            return root;
        }
            // go back to the login fragment


        // get the user name (email) and the user type (organizer or user or admin)
        // change the text of userWelcomeMessage based on the previous
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
                    String newText = binding.userWelcomeMessage.getText().toString();
                    Log.d("AccountManagementFragment", "newText before usertype: " + newText);
                    if (intUserType == UserSession.USER_TYPE_ORGANIZER) {
                        binding.userWelcomeMessage.setVisibility(View.VISIBLE);
                        Log.d("AccountManagementFragment", "usertype: organizer");
                        newText = "Welcome Organizer" + newText;
                    } else if (intUserType == UserSession.USER_TYPE_USER) {
                        binding.userWelcomeMessage.setVisibility(View.VISIBLE);
                        Log.d("AccountManagementFragment", "usertype: user");
                        newText = "Welcome User" + newText;
                    } else if (intUserType == UserSession.USER_TYPE_ADMIN) {
                        binding.userWelcomeMessage.setVisibility(View.VISIBLE);
                        newText = "Welcome Admin" + newText;
                    } else {
                        // unknown user type
                        binding.userWelcomeMessage.setVisibility(View.GONE);
                    }

                    Log.d("AccountManagementFragment", "newText after usertype: " + newText);
                    binding.userWelcomeMessage.setText(newText);

                } else {
                    Log.e("UserSession", "User type not found");
                }
            }
        });

        // get the user name (email) and the user type (organizer or user or admin)
        // change the text of userWelcomeMessage based on the previous
        UserSession.getInstance().getUserData(USER_EMAIL, new UserSession.FirebaseCallback<Object>() {
            @Override
            public void onCallback(Object userEmail) {
                Log.d("AccountManagementFragment", "In the onCallback: " + userEmail);
                if (userEmail != null) {
                    // Create a User representation based on the user type

                    Log.d("AccountManagementFragment", "User type UPDATED: " + userEmail);
                    if (UserSession.getInstance().getUserRepresentation() != null) {
                        UserSession.getInstance().getUserRepresentation().setUserEmail(String.valueOf(userEmail));
                    } else {
                        Log.e("AccountManagementFragment", "User representation not found");
                    }
                        // Append the user email to the current text

                        Log.d("AccountManagementFragment", "useremail: " + String.valueOf(userEmail));
                    Log.d("AccountManagementFragment", "userWelcomeMessage: " + binding.userWelcomeMessage.getText().toString());
                        String newText = binding.userWelcomeMessage.getText().toString() + " " + String.valueOf(userEmail);
                        Log.d("AccountManagementFragment", "newText email: " + newText);
                        binding.userWelcomeMessage.setText(newText);
                        // set the visibility of the textview
                        binding.userWelcomeMessage.setVisibility(View.VISIBLE);

                } else {
                    Log.e("AccountManagementFragment", "User email not found");
                }
            }
        });




        root.findViewById(R.id.logoutButton).setOnClickListener(v -> {
            // login the user using the email and password
            // if the login is successful, navigate to the dashboard fragment
            // if the login is not successful, show an error message
            // if the user is not logged in, show an error message
            UserSession.getInstance().logout();
            Log.d("AccountManagementFragment", "User is not logged in");
            // go back to the login fragment
            /*Fragment loginFragment = new LoginFragment();
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment_activity_main, loginFragment)
                    .addToBackStack(loginFragment.getClass().getName())
                    .commit();*/
            navController.navigate(R.id.login);

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
