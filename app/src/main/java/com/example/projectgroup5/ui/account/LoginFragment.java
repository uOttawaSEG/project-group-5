package com.example.projectgroup5.ui.account;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.projectgroup5.MainActivity;
import com.example.projectgroup5.R;
import com.example.projectgroup5.databinding.FragmentLoginBinding;
import com.example.projectgroup5.users.UserSession;

public class LoginFragment extends Fragment {
    private FragmentLoginBinding binding;

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
        root.findViewById(R.id.confirmCredentialAndLoginButton).setOnClickListener(v -> {
            this.getView().performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            UserSession.getInstance().login(binding.emailInput.getText().toString(), binding.passwordInput.getText().toString(), (MainActivity) getContext(), (task) -> {
            if (task.isSuccessful()) {
                Log.e("LoginFragment", "login was successful");
                this.getView().performHapticFeedback(HapticFeedbackConstants.CONFIRM);
                navController.navigate(R.id.action_login_to_account_management);
            } else {
                // show an error message
                Log.e("LoginFragment", "login was not successful");
                this.getView().performHapticFeedback(HapticFeedbackConstants.REJECT);
                binding.emailInput.setError("Invalid email or password");
            }
        });});
        root.findViewById(R.id.cancelButton).setOnClickListener(v -> {navController.popBackStack();
            this.getView().performHapticFeedback(HapticFeedbackConstants.CONFIRM);
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
