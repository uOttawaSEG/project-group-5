package com.example.projectgroup5;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.projectgroup5.database.Notification;
import com.example.projectgroup5.databinding.ActivityMainBinding;
import com.example.projectgroup5.users.UserSession;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    public static boolean complete = false;
    private static OnCompleteListener<Boolean> onCompleteListener;
    private static MainActivity instance;
    private NavController navController;

    public static void setOnCompleteListener(OnCompleteListener<Boolean> onCompleteListener) {
        MainActivity.onCompleteListener = onCompleteListener;
    }

    public static MainActivity getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.example.projectgroup5.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        BottomNavigationView bottomNavigationView = findViewById(R.id.nav_view);
        instance = this;

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home,
                R.id.search_event_dashboard,
                R.id.account_management).build();


        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_home)
                navController.navigate(R.id.navigation_home);
            else if (item.getItemId() == R.id.search_event_dashboard)
                navController.navigate(R.id.search_event_dashboard);
            else if (item.getItemId() == R.id.account_management)
                navController.navigate(R.id.account_management);
            else
                return false;
            return true;
        });

        // Set the default selected item
        bottomNavigationView.setSelectedItemId(R.id.search_event_dashboard);
        createNotificationChannel();
        UserSession.initialize(this, task -> {
            if (task.isSuccessful()) {
                Log.d("MainActivity", "UserSession initialized successfully");
            } else {
                Log.e("MainActivity", "UserSession initialization failed", task.getException());
            }
            if (onCompleteListener != null) {
                onCompleteListener.onComplete(Tasks.forResult(true));
                complete = true;
            }
        });
    }


    /**
     * Creates a notification channel for account-related notifications.
     * <p>
     * This method is designed to be called on devices running Android O
     * (API level 26) and above. It initializes a new notification channel
     * with the specified channel ID and name, setting its importance level
     * to default. The channel must be created to allow notifications to
     * be displayed to the user.
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    Notification.CHANNEL_ID,
                    "Account Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    public NavController getNavController() {
        return navController;
    }
}
