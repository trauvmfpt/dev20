package com.example.dev20.Services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.example.dev20.Config.Config;
import com.example.dev20.MainActivity;
import com.example.dev20.MapActivity;
import com.example.dev20.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.e("NEW_TOKEN",s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if(remoteMessage.getData()!=null){
            getMessage(remoteMessage);
            sendNotification();
        }
    }

    @Override
    public void onMessageSent(String s) {
        super.onMessageSent(s);
        Log.e("UPSTREAM", "##########onMessageSent: " + s );
        Toast.makeText(this, "Sent jammed location", Toast.LENGTH_SHORT).show();
    }

    public void sendNotification(){

        Intent intent = new Intent(getApplicationContext(), MapActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("email", Config.email);
        intent.putExtra("lat", Config.lat);
        intent.putExtra("lng", Config.lng);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notif = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "101";

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            @SuppressLint("WrongConstant") NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Notification", NotificationManager.IMPORTANCE_MAX);

            //Configure Notification Channel
            notificationChannel.setDescription("Jam Notifications");
            notificationChannel.enableLights(true);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);

            notif.createNotificationChannel(notificationChannel);
        }

        Notification notify=new Notification.Builder
                (getApplicationContext()).setContentTitle("JamTime")
                .setContentText("Tap to see where it's jammin'.")
                .setContentTitle("It's jammin' here!")
                .setSmallIcon(R.drawable.abc)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setWhen(System.currentTimeMillis()).build();

        notify.flags |= Notification.FLAG_AUTO_CANCEL;
        notif.notify(0, notify);
    }

    private void getMessage(final RemoteMessage remoteMessage) {
        Map<String, String> data = remoteMessage.getData();
        Config.email = data.get("email");
        Config.lat = data.get("lat");
        Config.lng = data.get("lng");
    }
}
