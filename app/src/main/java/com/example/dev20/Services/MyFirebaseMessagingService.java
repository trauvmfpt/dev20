package com.example.dev20.Services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.dev20.Config.Config;
import com.example.dev20.MapActivity;
import com.example.dev20.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

// cả class này dùng để tuỳ chỉnh tính năng gửi tin nhắn tự động qua Firebase
// Firebase Messaging chỉ hỗ trợ xử lí khi nhận được tin nhắn từ server,
// chưa hỗ trợ xử lí người dùng gửi tin nhắn lên Firebase database thì tự động tạo tin nhắn
// trên Firebase Messaging và gửi trở lại về cho người dùng.

// Em đang thử dùng luôn Firebase database để xem khi nào dữ liệu thay đổi thì tự động gửi tin nhắn
// cho người dùng luôn, nhưng đang lỗi và cũng chưa biết lỗi ở đâu :p
public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.e("NEW_TOKEN",s);
    }

    // server gửi tin nhắn về thì làm gì
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // nếu trong tin nhắn có thông tin data thì lấy data và vào hàm sendNotification
        // hàm sendNotification như đã comment ở MarkerActivity
        if(remoteMessage.getData()!=null){
            getMessage(remoteMessage);
            sendNotification();
        }
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
