package com.example.projectgroup5.ui.account;

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
import com.example.projectgroup5.database.DatabaseManager;
import com.example.projectgroup5.users.User;
import com.example.projectgroup5.users.UserSession;

public class AccountManagementFragment extends Fragment {
    private FragmentAccountManagementBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAccountManagementBinding.inflate(inflater, container, false);
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
        View root = binding.getRoot();

        // user representation comes out as null
        if (UserSession.getInstance().getUserId() == null || UserSession.getInstance().getUserRepresentation() == null) {
            navController.navigate(R.id.action_account_management_to_login_or_create_account);
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
            // haptic feedback

            navController.navigate(R.id.action_account_management_to_login_or_create_account);
        });

        return root;
    }

    /**
     * Displays the user's registration status based on data retrieved from the database.
     * <p>
     * This method fetches the user's registration state from the database and updates the UI
     * accordingly. It handles different states such as waitlisted, accepted, and rejected,
     * setting the visibility and text of the userCurrentState view. If the user state is unknown,
     * it hides the view.
     */
    private void displayUserRegistrationStatus() {
        Log.d("AccountManagementFragment", "In the displayUserRegistrationStatus method");
        cachedDisplayRegistrationStatus();
        DatabaseManager.getDatabaseManager().getUserDataFromFirestore(DatabaseManager.USER_REGISTRATION_STATE, userState -> {
            Log.d("AccountManagementFragment", "In the onCallback of displayUserRegistrationStatus: " + userState);
            if (userState != null) {
                String newText;
                switch (userState.toString()) {
                    case User.WAITLISTED:
                        binding.userCurrentState.setVisibility(View.VISIBLE);
                        Log.d("AccountManagementFragment", "userState: waitlisted");
                        newText = "You are on the waitlist";
                        break;
                    case User.ACCEPTED:
                        binding.userCurrentState.setVisibility(View.VISIBLE);
                        Log.d("AccountManagementFragment", "userState: accepted");
                        newText = "You are registered";
                        break;
                    case User.REJECTED:
                        binding.userCurrentState.setVisibility(View.VISIBLE);
                        Log.d("AccountManagementFragment", "userState: rejected");
                        newText = "Your application was rejected, call 911 for help";
                        break;
                    default:
                        // unknown registration state
                        binding.userCurrentState.setVisibility(View.GONE);
                        newText = "";
                        break;
                }
                // Append the userCurrentState to the current text
                Log.d("AccountManagementFragment", "userState: " + userState);
                    binding.userCurrentState.setText(newText);
            } else {
                Log.e("AccountManagementFragment", "User registration state not found");
            }
        });
    }

    private void cachedDisplayRegistrationStatus() {
        if (UserSession.getInstance().getUserRepresentation() != null) {
            String newText;
            if (UserSession.getInstance().getUserRepresentation().getUserRegistrationState() == null) return;
            switch (UserSession.getInstance().getUserRepresentation().getUserRegistrationState()) {
                case User.WAITLISTED:
                    binding.userCurrentState.setVisibility(View.VISIBLE);
                    Log.d("AccountManagementFragment", "userState: waitlisted");
                    newText = "You are on the waitlist";
                    break;
                case User.ACCEPTED:
                    binding.userCurrentState.setVisibility(View.VISIBLE);
                    Log.d("AccountManagementFragment", "userState: accepted");
                    newText = "You are registered";
                    break;
                case User.REJECTED:
                    binding.userCurrentState.setVisibility(View.VISIBLE);
                    Log.d("AccountManagementFragment", "userState: rejected");
                    newText = "Your application was rejected, call 911 for help";
                    break;
                default:
                    // unknown registration state
                    binding.userCurrentState.setVisibility(View.GONE);
                    newText = "";
                    break;
            }
            binding.userCurrentState.setText(newText);
        }
    }

    /**
     * Retrieves and displays the user's email address from the database.
     * <p>
     * This method fetches the user's email and updates the corresponding UI element.
     * If the email is found, it also updates the user representation with the email.
     * The visibility of the userCurrentEmail view is set to VISIBLE. If the email is not found,
     * an error log is generated.
     */
    private void displayUserEmail() {
        cachedDisplayUserEmail();
        Log.d("AccountManagementFragment", "In the displayUserEmail method");
        DatabaseManager.getDatabaseManager().getUserDataFromFirestore(DatabaseManager.USER_EMAIL, userEmail -> {
            if (userEmail != null && !userEmail.toString().isEmpty()) {
                // Create a User representation based on the user type
                if (UserSession.getInstance().getUserRepresentation() != null) {
                    UserSession.getInstance().getUserRepresentation().setUserEmail(String.valueOf(userEmail));
                }
                binding.userCurrentEmail.setText(String.valueOf(userEmail));
                // set the visibility of the textview
                binding.userCurrentEmail.setVisibility(View.VISIBLE);
            } else {
                Log.e("AccountManagementFragment", "User email not found");
                binding.userCurrentEmail.setVisibility(View.GONE);
            }
        });
    }

    private void cachedDisplayUserEmail() {
        User userRepresentation = UserSession.getInstance().getUserRepresentation();
        if (userRepresentation != null && userRepresentation.getUserEmail() != null && !userRepresentation.getUserEmail().isEmpty()) {
            binding.userCurrentEmail.setText(UserSession.getInstance().getUserRepresentation().getUserEmail());
            // set the visibility of the textview
            binding.userCurrentEmail.setVisibility(View.VISIBLE);
        } else {
            binding.userCurrentEmail.setVisibility(View.GONE);
        }
    }

    /**
     * Retrieves and displays the user's account type from the database.
     * <p>
     * This method fetches the user's account type and updates the corresponding UI element
     * based on the retrieved value. It sets the visibility of the userAccountType view to
     * VISIBLE or GONE depending on the user type (organizer, user, or admin).
     * If the user type is not found, an error log is generated.
     */
    private void displayUserType() {
        Log.d("AccountManagementFragment", "In the displayUserType method");
        cachedDisplayUserType();
        DatabaseManager.getDatabaseManager().getUserDataFromFirestore(DatabaseManager.USER_TYPE, userType -> {
            Log.d("AccountManagementFragment", "In the onCallback of displayUserType: " + userType);
            if (userType != null) {
                // Create a User representation based on the user type
                Log.d("UserSession", "User type UPDATED: " + userType);
                UserSession.getInstance().getUserRepresentation().setUserType(userType.toString());
                Log.d("firebase", "Retrieved user type: " + userType);
                String newText;
                switch (userType.toString()) {
                    case User.USER_TYPE_ORGANIZER:
                        Log.d("AccountManagementFragment", "usertype: organizer");
                        binding.userAccountType.setVisibility(View.VISIBLE);
                        newText = "Welcome Organizer";
                        break;
                    case User.USER_TYPE_ATTENDEE:
                        Log.d("AccountManagementFragment", "usertype: user");
                        binding.userAccountType.setVisibility(View.VISIBLE);
                        newText = "Welcome User";
                        break;
                    case User.USER_TYPE_ADMIN:
                        Log.d("AccountManagementFragment", "usertype: admin");
                        binding.userAccountType.setVisibility(View.VISIBLE);
                        newText = "Welcome Admin";
                        break;
                    default:
                        // unknown user type
                        Log.e("AccountManagementFragment", "usertype: unknown");
                        binding.userAccountType.setVisibility(View.GONE);
                        newText = "";
                        break;
                }
                Log.d("AccountManagementFragment", "newText for userType: " + newText);
                    binding.userAccountType.setText(newText);

            } else {
                Log.e("AccountManagementFragment", "User type not found");
            }
        });
    }

    private void cachedDisplayUserType() {
        if (UserSession.getInstance().getUserRepresentation() == null) return;
        String newText;
        if (UserSession.getInstance().getUserRepresentation().getUserType() == null) return;
        switch (UserSession.getInstance().getUserRepresentation().getUserType()) {
            case User.USER_TYPE_ORGANIZER:
                Log.d("AccountManagementFragment", "usertype: organizer");
                binding.userAccountType.setVisibility(View.VISIBLE);
                newText = "Welcome Organizer";
                break;
            case User.USER_TYPE_ATTENDEE:
                Log.d("AccountManagementFragment", "usertype: user");
                binding.userAccountType.setVisibility(View.VISIBLE);
                newText = "Welcome User";
                break;
            case User.USER_TYPE_ADMIN:
                Log.d("AccountManagementFragment", "usertype: admin");
                binding.userAccountType.setVisibility(View.VISIBLE);
                newText = "Welcome Admin";
                break;
            default:
                // unknown user type
                Log.e("AccountManagementFragment", "usertype: unknown");
                binding.userAccountType.setVisibility(View.GONE);
                newText = "";
                break;
        }
        Log.d("AccountManagementFragment", "newText for userType: " + newText);
        binding.userAccountType.setText(newText);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
