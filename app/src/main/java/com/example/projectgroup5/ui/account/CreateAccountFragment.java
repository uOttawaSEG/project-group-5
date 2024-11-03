package com.example.projectgroup5.ui.account;

import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.projectgroup5.MainActivity;
import com.example.projectgroup5.R;
import com.example.projectgroup5.database.DatabaseManager;
import com.example.projectgroup5.databinding.FragmentCreateAccountBinding;
import com.example.projectgroup5.users.User;
import com.example.projectgroup5.users.UserSession;
import com.google.android.gms.tasks.Task;

import java.util.concurrent.atomic.AtomicInteger;

public class CreateAccountFragment extends Fragment {
    private FragmentCreateAccountBinding binding;
    private NavController navController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCreateAccountBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
        // Switch between user and organizer
        root.findViewById(R.id.OrganizerVSUserSwitch).setOnClickListener(v -> {
            if (binding.OrganizerVSUserSwitch.isChecked()) {
                binding.editTextTextOrganisation.setVisibility(View.VISIBLE);
            } else {
                binding.editTextTextOrganisation.setVisibility(View.GONE);
            }
        });

        root.findViewById(R.id.confirmCredentialAndCreateButton).setOnClickListener(v -> {
            boolean errorFlag = false;
            String password = binding.editTextTextPasswordUserCreate.getText().toString().trim();
            if (binding.editTextTextEmailAddressUserCreate.getText().toString().isEmpty()) {
                binding.editTextTextEmailAddressUserCreate.setError("Please enter an email");
                errorFlag = true;
            }


            if (password.isEmpty() || password.length() < 6) {
                binding.editTextTextPasswordUserCreate.setError("Invalid password");
                errorFlag = true;
            }

            String confirmPassword = binding.editTextTextConfirmPasswordUserCreate.getText().toString().trim();
            if (confirmPassword.isEmpty()) {
                binding.editTextTextConfirmPasswordUserCreate.setError("Please confirm your password");
                errorFlag = true;
            }

            if (!password.equals(confirmPassword)) {
                binding.editTextTextConfirmPasswordUserCreate.setError("Incorrect password");
                errorFlag = true;
            }

            String address = binding.editTextTextPostalAddressUserCreate.getText().toString().trim();
            if (address.isEmpty()) {
                binding.editTextTextPostalAddressUserCreate.setError("Please enter an address");
                errorFlag = true;
            }

            String phoneNumber = binding.editTextPhoneUserCreate.getText().toString().trim();
            if (!PhoneNumberUtils.isGlobalPhoneNumber(phoneNumber) || phoneNumber.length() != 10) {
                binding.editTextPhoneUserCreate.setError("Invalid phone number");
                errorFlag = true;
            }

            String firstName = binding.editTextTextUserCreate.getText().toString().trim().toLowerCase();
            if (firstName.isEmpty()) {
                binding.editTextTextUserCreate.setError("Please enter a first name");
                errorFlag = true;
            }
            if (!firstName.matches("[a-zA-Z]+")) {
                binding.editTextTextUserCreate.setError("Invalid first name");
                errorFlag = true;
            }

            String lastName = binding.editTextText2UserCreate.getText().toString().trim().toLowerCase();
            if (lastName.isEmpty()) {
                binding.editTextText2UserCreate.setError("Please enter a last name");
                errorFlag = true;
            }
            if (!lastName.matches("[a-zA-Z]+")) {
                binding.editTextText2UserCreate.setError("Invalid last name");
                errorFlag = true;
            }

            String organisation;
            if (binding.OrganizerVSUserSwitch.isChecked()) {
                organisation = binding.editTextTextOrganisation.getText().toString().trim().toLowerCase();
                if (organisation.isEmpty()) {
                    binding.editTextTextOrganisation.setError("Please enter an organisation");
                    errorFlag = true;
                }
                if (!organisation.matches("[a-zA-Z]+")) {
                    binding.editTextTextOrganisation.setError("Invalid organisation");
                    errorFlag = true;
                }
            } else {
                organisation = "";
            }

            if (errorFlag) {
                return;
            }

            Log.d("CreateAccountFragment", "Creating user with email: " + binding.editTextTextEmailAddressUserCreate.getText().toString());
            DatabaseManager.getDatabaseManager().createUserWithEmailAndPassword(binding.editTextTextEmailAddressUserCreate.getText().toString(), binding.editTextTextPasswordUserCreate.getText().toString(), task -> {
                // now we have tried to create the user, lets check if it was successful
                if (!task.isSuccessful()) {
                    binding.editTextTextEmailAddressUserCreate.setError("Invalid email or password");
                    binding.editTextTextPasswordUserCreate.setError("Invalid email or password");
                    return;
                }
                // now we have created the user, lets store the user data
                // we must first make sure that the UserSession userid is set
                UserSession.getInstance().setUserId(task.getResult().getUser().getUid());
                // Initialize a counter for the number of tasks
                int totalTasks = binding.OrganizerVSUserSwitch.isChecked()? 7 : 6; // Number of Firestore tasks
                AtomicInteger tasksCompleted = new AtomicInteger(0); // Use AtomicInteger for thread safety

                DatabaseManager.getDatabaseManager().storeUserValueToFirestore(
                        DatabaseManager.USER_TYPE,
                        binding.OrganizerVSUserSwitch.isChecked() ? User.USER_TYPE_ORGANIZER : User.USER_TYPE_ATTENDEE,
                        (task0) -> handleTaskCompletion(task0, "storeUserTypeError", tasksCompleted, totalTasks)
                );

                DatabaseManager.getDatabaseManager().storeUserValueToFirestore(
                        DatabaseManager.USER_ADDRESS,
                        address,
                        (task0) -> handleTaskCompletion(task0, "storeUserAddressError", tasksCompleted, totalTasks)
                );

                DatabaseManager.getDatabaseManager().storeUserValueToFirestore(
                        DatabaseManager.USER_PHONE,
                        phoneNumber,
                        (task0) -> handleTaskCompletion(task0, "storeUserPhoneError", tasksCompleted, totalTasks)
                );

                DatabaseManager.getDatabaseManager().storeUserValueToFirestore(
                        DatabaseManager.USER_FIRST_NAME,
                        firstName,
                        (task0) -> handleTaskCompletion(task0, "storeUserFirstNameError", tasksCompleted, totalTasks)
                );

                DatabaseManager.getDatabaseManager().storeUserValueToFirestore(
                        DatabaseManager.USER_LAST_NAME,
                        lastName,
                        (task0) -> handleTaskCompletion(task0, "storeUserLastNameError", tasksCompleted, totalTasks)
                );

                DatabaseManager.getDatabaseManager().storeUserValueToFirestore(
                        DatabaseManager.USER_REGISTRATION_STATE,
                        User.WAITLISTED,
                        (task0) -> handleTaskCompletion(task0, "storeUserUserRegistrationState", tasksCompleted, totalTasks)
                );

                if (binding.OrganizerVSUserSwitch.isChecked()) {
                    DatabaseManager.getDatabaseManager().storeUserValueToFirestore(
                            DatabaseManager.USER_ORGANIZATION_NAME,
                            organisation,
                            (task0) -> handleTaskCompletion(task0, "storeUserOrganisationError", tasksCompleted, totalTasks)
                    );
                }
            });});

            root.findViewById(R.id.cancelButtonCreate).setOnClickListener(v -> navController.popBackStack());
            return root;
    }

    private void handleTaskCompletion(Task<Void> task, String errorMessage, AtomicInteger tasksCompleted, int totalTasks) {
        if (task.isSuccessful()) {
            Log.d("CreateAccountFragment", "Success: " + task.getResult());
        } else {
            Log.d("CreateAccountFragment", errorMessage + ": " + task.getException());
        }

        // Increment the completed tasks count
        int completed = tasksCompleted.incrementAndGet();

        // Check if all tasks are completed
        if (completed == totalTasks) {
            Log.d("CreateAccountFragment", "All tasks completed successfully!");
            // We now have all the account data stored we can login
            UserSession.getInstance().login(binding.editTextTextEmailAddressUserCreate.getText().toString(), binding.editTextTextPasswordUserCreate.getText().toString(), (MainActivity) getContext(), (task1) -> {
                if (task1.isSuccessful()) {
                    Log.e("LoginFragment", "login was successful");
                    // Once we know the login was successful we can navigate to the account management fragment
                    navController.navigate(R.id.action_create_account_to_account_management);
                } else {
                    // show an error message
                    Log.e("LoginFragment", "login was not successful");
                    binding.editTextTextEmailAddressUserCreate.setError("Invalid email or password");
                }});
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
