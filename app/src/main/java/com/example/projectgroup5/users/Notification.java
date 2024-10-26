package com.example.projectgroup5.users;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.projectgroup5.MainActivity;
import com.example.projectgroup5.R;

public class Notification {

    public static final String CHANNEL_ID = "my_channel_id";
    public static final String CHANNEL_NAME = "My Channel";
    public static final String CHANNEL_DESCRIPTION = "Channel Description";

//    public static void createNotificationChannel(Context context) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel channel = new NotificationChannel(
//                    CHANNEL_ID,
//                    CHANNEL_NAME,
//                    NotificationManager.IMPORTANCE_DEFAULT
//            );
//            channel.setDescription(CHANNEL_DESCRIPTION);
//
//            NotificationManager manager = context.getSystemService(NotificationManager.class);
//            manager.createNotificationChannel(channel);
//        }
//    }

    public static void sendNotification(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, UserSession.CHANNEL_ID) // Use the correct channel ID
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle("Account Created")
                .setContentText("Your account has been successfully created.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // Handle the case where permission is not granted
            Log.e("CreateAccountFragment", "Notification permission not granted.");
            return;
        }

        notificationManager.notify(1001, builder.build());
    }

//    public static void showNotification(Context context, String title, String message) {
//        Notification.createNotificationChannel(context); // Create the notification channel
//
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Notification.CHANNEL_ID)
//                .setSmallIcon(R.drawable.ic_notifications) // Replace with your own icon
//                .setContentTitle(title)
//                .setContentText(message)
//                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//                .setAutoCancel(true); // Auto dismiss the notification when clicked
//
//        // Create an intent to open an activity when the notification is clicked
//        Intent intent = new Intent(context, MainActivity.class); // Replace with your target activity
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        builder.setContentIntent(pendingIntent);
//
//        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//        notificationManager.notify(1, builder.build()); // Use a unique ID for each notification
//    }
}
