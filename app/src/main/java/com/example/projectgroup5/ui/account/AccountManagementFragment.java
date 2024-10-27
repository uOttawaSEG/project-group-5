package com.example.projectgroup5.ui.account;

import static com.example.projectgroup5.users.UserSession.USER_EMAIL;
import static com.example.projectgroup5.users.UserSession.USER_REGISTRATION_STATE;
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
import com.example.projectgroup5.users.DatabaseManager;
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

        // get the user type
        displayUserType();

        // get the user email
        displayUserEmail();

        // get the user registration status
        displayUserRegistrationStatus();

        root.findViewById(R.id.logoutButton).setOnClickListener(v -> {
            UserSession.getInstance().logout();
            // go back to the login fragment
            navController.navigate(R.id.login);
        });

        return root;
    }

    private void displayUserRegistrationStatus() {
        DatabaseManager.getDatabaseManager().getUserData(USER_REGISTRATION_STATE, userState -> {
            Log.d("AccountManagementFragment", "In the onCallback: " + userState);
            if (userState != null) {
                int intUserState = (int)(long)((Long) userState);
                String newText;
                switch (intUserState) {
                    case UserSession.WAITLISTED:
                        binding.userCurrentState.setVisibility(View.VISIBLE);
                        Log.d("AccountManagementFragment", "userState: waitlisted");
                        newText = "You are on the waitlist";
                        break;
                    case UserSession.ACCEPTED:
                        binding.userCurrentState.setVisibility(View.VISIBLE);
                        Log.d("AccountManagementFragment", "userState: accepted");
                        newText = "You are registered";
                        break;
                    case UserSession.REJECTED:
                        binding.userCurrentState.setVisibility(View.VISIBLE);
                        Log.d("AccountManagementFragment", "userState: rejected");
                        newText = "Your application was rejected, call 911 for help";
                        break;
                    default:
                        // unknown registration state
                        binding.userCurrentState.setVisibility(View.GONE);
                        newText = null;
                        break;
                }
                // Append the userCurrentState to the current text
                Log.d("AccountManagementFragment", "userState: " + String.valueOf(userState));
                binding.userCurrentState.setText(newText);
            } else {
                Log.e("AccountManagementFragment", "User registration state not found");
            }
        });
    }

    private void displayUserEmail() {
        DatabaseManager.getDatabaseManager().getUserData(USER_EMAIL, userEmail -> {
            if (userEmail != null) {
                // Create a User representation based on the user type
                if (UserSession.getInstance().getUserRepresentation() != null) {
                    UserSession.getInstance().getUserRepresentation().setUserEmail(String.valueOf(userEmail));
                }
                binding.userCurrentEmail.setText(String.valueOf(userEmail));
                // set the visibility of the textview
                binding.userCurrentEmail.setVisibility(View.VISIBLE);
            } else {
                Log.e("AccountManagementFragment", "User email not found");
            }
        });
    }

    private void displayUserType() {
        DatabaseManager.getDatabaseManager().getUserData(USER_TYPE, userType -> {
            Log.d("UserSession", "In the onCallback: " + userType);
            if (userType != null) {
                // Create a User representation based on the user type
                Log.d("UserSession", "User type UPDATED: " + userType);
                UserSession.getInstance().getUserRepresentation().setUserType((int)(long)((Long) userType));
                int intUserType = (int)(long)((Long) userType);
                Log.d("firebase", "Retrieved user type: " + userType);
                String newText;
                switch (intUserType) {
                    case UserSession.USER_TYPE_ORGANIZER:
                        binding.userAccountType.setVisibility(View.VISIBLE);
                        Log.d("AccountManagementFragment", "usertype: organizer");
                        newText = "Welcome Organizer";
                        break;
                    case UserSession.USER_TYPE_USER:
                        binding.userAccountType.setVisibility(View.VISIBLE);
                        Log.d("AccountManagementFragment", "usertype: user");
                        newText = "Welcome User";
                        break;
                    case UserSession.USER_TYPE_ADMIN:
                        binding.userAccountType.setVisibility(View.VISIBLE);
                        Log.d("AccountManagementFragment", "usertype: admin");
                        newText = "Welcome Admin";
                        break;
                    default:
                        // unknown user type
                        Log.e("AccountManagementFragment", "usertype: unknown");
                        binding.userAccountType.setVisibility(View.GONE);
                        newText = null;
                        break;
                }
                Log.d("AccountManagementFragment", "newText for userType: " + newText);
                binding.userAccountType.setText(newText);

            } else {
                Log.e("AccountManagementFragment", "User type not found");
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
