package service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.barberme.R;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import ui.SplashScreenActivity;

public class FirebasePushService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            //if the application is not in forground post notification
            NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            String NOTIFICATION_CHANNEL_ID = getPackageName() + "UploadNotifID";
            if(Build.VERSION.SDK_INT >= 26) {
                NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                        "Important Notification", NotificationManager.IMPORTANCE_HIGH);

                // Configure the notification channel.
                notificationChannel.setDescription("Important Notification");
                notificationManager.createNotificationChannel(notificationChannel);
            }
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,
                    NOTIFICATION_CHANNEL_ID);

            // Create notification action intent..
            Intent notificationIntent = new Intent(getApplicationContext(), SplashScreenActivity.class);
            PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);

            notificationBuilder
                    .setContentTitle(getResources().getString(R.string.app_name))
                    .setContentText(remoteMessage.getData().get("message"))
                    .setSmallIcon(android.R.drawable.star_on)
                    .setContentIntent(pi);

            notificationManager.notify(1,notificationBuilder.build());

        }
    }
}
