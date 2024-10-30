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
import com.example.projectgroup5.databinding.FragmentLoginBinding;
import com.example.projectgroup5.users.UserSession;

public class LoginFragment extends Fragment {
    private FragmentLoginBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
        root.findViewById(R.id.confirmCredentialAndLoginButton).setOnClickListener(v -> UserSession.getInstance().login(binding.emailInput.getText().toString(), binding.passwordInput.getText().toString(), (task) -> {
            if (task.isSuccessful()) {
                Log.e("LoginFragment", "login was successful");
                UserSession.getInstance().instantiateUserRepresentation(getContext());
                UserSession.getInstance().setUserId(task.getResult().getUser().getUid());
            } else {
                // show an error message
                Log.e("LoginFragment", "login was not successful");
                binding.emailInput.setError("Invalid email or password");
            }
        }));
        root.findViewById(R.id.cancelButton).setOnClickListener(v -> navController.navigate(R.id.action_go_back_to_login));
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
