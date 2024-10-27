package com.example.projectgroup5.users;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.projectgroup5.MainActivity;
import com.example.projectgroup5.R;

public class Notification {
    public static final String CHANNEL_ID = "account_creation_channel";

    /**
     * Sends a notification to the user indicating that their account has been created.
     * <p>
     * This method constructs a notification that informs the user about the successful creation
     * of their account. It includes an intent that launches the MainActivity when the notification
     * is tapped. The notification is shown only if the necessary permissions are granted.
     *
     * @param context The context from which the notification is sent, typically an Activity or Application context.
     */
    public static void sendNotification(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID) // Use the correct channel ID
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

}
