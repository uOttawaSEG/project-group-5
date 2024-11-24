package com.example.projectgroup5.ui.account;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.projectgroup5.BuildConfig;
import com.example.projectgroup5.MainActivity;
import com.example.projectgroup5.R;
import com.example.projectgroup5.database.DatabaseManager;
import com.example.projectgroup5.database.FieldValidator;
import com.example.projectgroup5.databinding.FragmentCreateAccountBinding;
import com.example.projectgroup5.users.User;
import com.example.projectgroup5.users.UserSession;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.Arrays;

public class CreateAccountFragment extends Fragment {
    private EditText editTextLocation;
    private ActivityResultLauncher<Intent> autocompleteLauncher;
    private String placeAddress = null;
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

        root.findViewById(R.id.cancelButtonCreate).setOnClickListener(v -> navController.popBackStack());

        // Define a variable to hold the Places API key.
        String apiKey = BuildConfig.PLACES_API_KEY;

        // Log an error if apiKey is not set.
        if (TextUtils.isEmpty(apiKey)) {
            Log.e("Places test", "No api key");
            Toast.makeText(getContext(), "No api key", Toast.LENGTH_SHORT).show();
            navController.popBackStack();
        }

        // Initialize the SDK
        if (!Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled(this.getContext(), apiKey);
        }

        editTextLocation = binding.editTextTextPostalAddressUserCreate;

        autocompleteLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // Handle the result of the Autocomplete Activity here
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        // You can retrieve the Place data here from the result
                        Place place = Autocomplete.getPlaceFromIntent(result.getData());
                        // Use the place object as needed
                        placeAddress = place.getFormattedAddress();
                        Log.d("CreateEventFragment", "Selected address: " + placeAddress);
                        editTextLocation.setText(place.getDisplayName());
                    }
                });

        // the edit text is not editable but allow the user to open the autocomplete fragment when clicking on it
        editTextLocation.setOnClickListener(v -> {
            // clear the error of the edit text
            editTextLocation.setError(null);
            openAutocompleteActivity();
        });

        root.findViewById(R.id.confirmCredentialAndCreateButton).setOnClickListener(v -> {
            boolean errorFlag = false;
            String password = binding.editTextTextPasswordUserCreate.getText().toString().trim();
            if (binding.editTextTextEmailAddressUserCreate.getText().toString().isEmpty()) {
                binding.editTextTextEmailAddressUserCreate.setError("Please enter an email");
                errorFlag = true;
            }


            if (FieldValidator.checkIfPasswordIsInvalid(password)) {
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
            if (FieldValidator.checkIfPhoneNumberIsInvalid(phoneNumber)) {
                binding.editTextPhoneUserCreate.setError("Invalid phone number");
                errorFlag = true;
            }

            String firstName = binding.editTextTextUserCreate.getText().toString().trim().toLowerCase();
            if (firstName.isEmpty()) {
                binding.editTextTextUserCreate.setError("Please enter a first name");
                errorFlag = true;
            }
            if (FieldValidator.checkIfIsNotAlphabet(firstName)) {
                binding.editTextTextUserCreate.setError("Invalid first name");
                errorFlag = true;
            }

            String lastName = binding.editTextText2UserCreate.getText().toString().trim().toLowerCase();
            if (lastName.isEmpty()) {
                binding.editTextText2UserCreate.setError("Please enter a last name");
                errorFlag = true;
            }
            if (FieldValidator.checkIfIsNotAlphabet(lastName)) {
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
                if (FieldValidator.checkIfIsNotAlphabet(organisation)) {
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
            User user = User.newUser(
                    binding.OrganizerVSUserSwitch.isChecked() ? User.USER_TYPE_ORGANIZER : User.USER_TYPE_ATTENDEE,
                    firstName,
                    lastName,
                    binding.editTextTextEmailAddressUserCreate.getText().toString(),
                    Long.parseLong(phoneNumber),
                    address,
                    organisation
            );

            DatabaseManager.getDatabaseManager().createNewUser(user, password, onCompleteListener -> {
                if (onCompleteListener.isSuccessful()) {
                    // we have created the account, we must now login
                    UserSession.getInstance().login(user.getUserEmail(), password, (MainActivity) getActivity(), onCompleteListener1 -> {
                        if (onCompleteListener1.isSuccessful()) {
                            Log.d("CreateAccountFragment", "login was successful");
                            // Once we know the login was successful we can navigate to the account management fragment
                            navController.navigate(R.id.action_create_account_to_account_management);
                        } else {
                            // show an error message
                            Log.e("CreateAccountFragment", "login was not successful");
                            binding.editTextTextEmailAddressUserCreate.setError("Invalid email or password");
                        }
                    });
                } else if (onCompleteListener.getException() != null) {
                    switch (onCompleteListener.getException().getMessage()) {
                        case "The email address is badly formatted.":
                            binding.editTextTextEmailAddressUserCreate.setError("Invalid email address");
                            break;
                        case "The given password is invalid. [ Password should be at least 6 characters ]":
                            binding.editTextTextPasswordUserCreate.setError("Invalid password");
                            break;
                            case "The email address is already in use by another account.":
                                binding.editTextTextEmailAddressUserCreate.setError("Email address already in use");
                                break;

                        default:
                            Log.e("CreateAccountFragment", "Error creating user: " + onCompleteListener.getException().getMessage());
                            binding.editTextTextEmailAddressUserCreate.setError("Invalid email or password");
                            binding.editTextTextPasswordUserCreate.setError("Invalid email or password");
                            break;
                    }
                }
            });


        });
        return root;
    }

    private void openAutocompleteActivity() {
        // Use the Places API to show autocomplete suggestions
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN,
                Arrays.asList(Place.Field.ID, Place.Field.DISPLAY_NAME, Place.Field.FORMATTED_ADDRESS))
                .build(getContext());
        // Launch the autocomplete activity using the launcher initialized in OnCreate
        autocompleteLauncher.launch(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
