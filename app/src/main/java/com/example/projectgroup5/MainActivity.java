package com.example.projectgroup5;

import android.os.Bundle;
import android.util.Log;

import com.example.projectgroup5.users.UserSession;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.projectgroup5.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bottomNavigationView = findViewById(R.id.nav_view);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home,
                R.id.search_event_dashboard,
                R.id.account_management).build();

        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_home) {
                navController.navigate(R.id.navigation_home);
//                setTitle("Home");
            } else if (item.getItemId() == R.id.search_event_dashboard) {
                navController.navigate(R.id.search_event_dashboard);
//                setTitle("Search Events");
            } else if (item.getItemId() == R.id.account_management) {
                navController.navigate(R.id.account_management);
            }
//            } else if (item.getItemId() == R.id.account) {
//                navController.navigate(R.id.account);
//                Log.e("MainActivity", "Account fragment is active");
//                bottomNavigationView.setSelectedItemId(R.id.account_management);
//            } else if (item.getItemId() == R.id.login_or_create_account) {
//                navController.navigate(R.id.login_or_create_account);
//                Log.e("MainActivity", "Account fragment is active login or create");
//                bottomNavigationView.setSelectedItemId(R.id.account_management);
//            } else if (item.getItemId() == R.id.login) {
//                navController.navigate(R.id.login);
//                Log.e("MainActivity", "Account fragment is active login");
//                bottomNavigationView.setSelectedItemId(R.id.account_management);
//            } else if (item.getItemId() == R.id.create_account) {
//                navController.navigate(R.id.create_account);
//                Log.e("MainActivity", "Account fragment is active create account");
//                bottomNavigationView.setSelectedItemId(R.id.account_management);
             else {
                return false;
            }
            return true;
        });

        // Set the default selected item
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        UserSession.initialize(this, navController);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}
