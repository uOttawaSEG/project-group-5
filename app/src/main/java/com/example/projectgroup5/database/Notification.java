package com.example.projectgroup5.database;

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
import com.example.projectgroup5.events.Event;

public class Notification {
    public static final String CHANNEL_ID = "account_creation_channel";

    /**
     * Sends a notification to the user with a message and title.
     * <p>
     * This method constructs a notification that informs the user about something happening.
     * The notification is shown only if the necessary permissions are granted.
     *
     * @param context The context from which the notification is sent, typically an Activity or Application context.
     * @param title   The title of the notification.
     * @param message The content of the notification.
     */


    public static void sendMessageNotification(Context context, String title, String message) {
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                new Intent(), // Empty intent
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT // Flags to ensure the intent doesn't change
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID) // Use the correct channel ID
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // Handle the case where permission is not granted
            Log.e("Notification", "Notification permission not granted.");
            return;
        }

        notificationManager.notify(1002, builder.build());
    }


}
