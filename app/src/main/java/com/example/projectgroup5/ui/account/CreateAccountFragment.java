package com.example.projectgroup5.ui.account;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.projectgroup5.MainActivity;
import com.example.projectgroup5.R;
import com.example.projectgroup5.databinding.FragmentCreateAccountBinding;
import com.example.projectgroup5.users.DatabaseManager;
import com.example.projectgroup5.users.UserSession;

public class CreateAccountFragment extends Fragment {
    private FragmentCreateAccountBinding binding;
    public void sendNotification() {
        Intent intent = new Intent(requireContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), UserSession.CHANNEL_ID) // Use the correct channel ID
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle("Account Created")
                .setContentText("Your account has been successfully created.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(requireContext());

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // Handle the case where permission is not granted
            Log.e("CreateAccountFragment", "Notification permission not granted.");
            return;
        }

        notificationManager.notify(1001, builder.build());
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCreateAccountBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        NavController navController = Navigation.findNavController(requireActivity(),R.id.nav_host_fragment_activity_main);
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
            if (binding.editTextTextEmailAddressUserCreate.getText().toString().isEmpty()) {
                binding.editTextTextEmailAddressUserCreate.setError("Please enter an email");
                errorFlag = true;
            }

            String password = binding.editTextTextPasswordUserCreate.getText().toString().trim();
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

            if (binding.OrganizerVSUserSwitch.isChecked()) {
                String organisation = binding.editTextTextOrganisation.getText().toString().trim().toLowerCase();
                if (organisation.isEmpty()) {
                    binding.editTextTextOrganisation.setError("Please enter an organisation");
                    errorFlag = true;
                }
                if (!organisation.matches("[a-zA-Z]+")) {
                    binding.editTextTextOrganisation.setError("Invalid organisation");
                    errorFlag = true;
                }
            }

            if (errorFlag) {
                return;
            }

            UserSession.getInstance().createUser(binding.editTextTextEmailAddressUserCreate.getText().toString(), binding.editTextTextPasswordUserCreate.getText().toString(), (task) -> {
                if (task.isSuccessful()) {
                    UserSession.getInstance().setUserId(task.getResult().getUser().getUid());

                    DatabaseManager.getDatabaseManager().storeValue(UserSession.USER_TYPE, binding.OrganizerVSUserSwitch.isChecked() ? UserSession.USER_TYPE_ORGANIZER : UserSession.USER_TYPE_USER, (task1) -> {
                        if (task1.isSuccessful()) {
                            Log.d("CreateAccountFragment", "Success: " + task1.getResult());
                        } else {
                            Log.d("CreateAccountFragment", "storeUserTypeError: " + task1.getException());
                        }
                    });

                    DatabaseManager.getDatabaseManager().storeValue(UserSession.USER_ADDRESS, address, (task1) -> {
                        if (task1.isSuccessful()) {
                            Log.d("CreateAccountFragment", "Success: " + task1.getResult());
                        } else {
                            Log.d("CreateAccountFragment", "storeUserAddressError: " + task1.getException());
                        }
                    });

                    DatabaseManager.getDatabaseManager().storeValue(UserSession.USER_PHONE, phoneNumber, (task1) -> {
                        if (task1.isSuccessful()) {
                            Log.d("CreateAccountFragment", "Success: " + task1.getResult());
                        } else {
                            Log.d("CreateAccountFragment", "storeUserPhoneError: " + task1.getException());
                        }
                    });

                    DatabaseManager.getDatabaseManager().storeValue(UserSession.USER_FIRST_NAME, firstName, (task1) -> {
                        if (task1.isSuccessful()) {
                            Log.d("CreateAccountFragment", "Success: " + task1.getResult());
                        } else {
                            Log.d("CreateAccountFragment", "storeUserFirstNameError: " + task1.getException());
                        }
                    });

                    DatabaseManager.getDatabaseManager().storeValue(UserSession.USER_LAST_NAME, lastName, (task1) -> {
                        if (task1.isSuccessful()) {
                            Log.d("CreateAccountFragment", "Success: " + task1.getResult());
                        } else {
                            Log.d("CreateAccountFragment", "storeUserLastNameError: " + task1.getException());
                        }
                    });

                    DatabaseManager.getDatabaseManager().storeValue(UserSession.USER_REGISTRATION_STATE, UserSession.WAITLISTED, (task1) -> {
                        if (task1.isSuccessful()) {
                            Log.d("CreateAccountFragment", "Success: " + task1.getResult());
                            // Call the notification method after successful account creation
                            sendNotification();
                        } else {
                            Log.d("CreateAccountFragment", "storeUserUserRegistrationState: " + task1.getException());
                        }
                    });
                } else {
                    Log.d("CreateAccountFragment", "onCreateView: " + task.getException());
                    // Provide more information about the error
                    if (task.getException() != null) {
                        Log.d("CreateAccountFragment", "onCreateView: " + task.getException().getMessage());
                    }

                    // Show an error message
                    binding.editTextTextEmailAddressUserCreate.setError("Invalid email or password");
                    binding.editTextTextPasswordUserCreate.setError("Invalid email or password");
                }
            });
        });

        root.findViewById(R.id.cancelButtonCreate).setOnClickListener(v -> {
            navController.navigate(R.id.action_create_account_to_account);
        });
        return root;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
