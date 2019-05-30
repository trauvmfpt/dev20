package com.example.dev20;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.dev20.Config.Config;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MenuActivity extends AppCompatActivity {
    private static final String TAG = "MenuActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final String SENDER_ID = "114752414746";
    private FusedLocationProviderClient fusedLocationClient;
    private DatabaseReference mDatabase;

    Button btnSendLocation, btnChooseLocation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        if (!isServicesOK()){
            Toast.makeText(this, "Google Maps is unavailable at the moment.", Toast.LENGTH_SHORT).show();
        }
        else {
            btnSendLocation = (Button) findViewById(R.id.btnSendLocation);
            btnSendLocation.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("MissingPermission")
                @Override
                public void onClick(View v) {
                    fusedLocationClient = LocationServices.getFusedLocationProviderClient(MenuActivity.this);
                    fusedLocationClient.getLastLocation().addOnSuccessListener(MenuActivity.this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            if (user != null) {
                                String email = user.getEmail();
                                String lat = Double.toString(location.getLatitude());
                                String lng = Double.toString(location.getLongitude());
                                Notification noti = new Notification(email, lat, lng);
                                String notiId = createID();

                                FirebaseMessaging fm = FirebaseMessaging.getInstance();
                                fm.send(new RemoteMessage.Builder(SENDER_ID + "@fcm.googleapis.com")
                                        .setMessageId(notiId)
                                        .addData("email", email)
                                        .addData("lat", lat)
                                        .addData("lng", lng)
                                        .build());
                                mDatabase.child("notis").push().setValue(noti);
                            } else {
                                Toast.makeText(MenuActivity.this, "You're not signed in?", Toast.LENGTH_SHORT).show();
                            }
                        }

                    });
                }
            });

            btnChooseLocation = (Button) findViewById(R.id.btnChooseLocation);
            btnChooseLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MenuActivity.this, MarkerActivity.class);
                    startActivity(intent);
                }
            });

            mDatabase.child("notis").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                    for ( DataSnapshot child: children){
                        Notification noti = child.getValue(Notification.class);
                        sendNotification(noti);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }
    private void openMap(){
        Intent intent = new Intent(MenuActivity.this, MapActivity.class);
        startActivity(intent);
    }

    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking GG services version");
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MenuActivity.this);

        if (available == ConnectionResult.SUCCESS){
            Log.d(TAG, "isServicesOK: GG Play Services is working");
            return true;
        }
        else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            Log.d(TAG, "isServicesOK: error but fixable");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MenuActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }
        else {
            Toast.makeText(this, "We can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }
    public String createID(){
        Date now = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        return dateFormat.format(now);
    }

    public void sendNotification(Notification noti){
        Intent intent = new Intent(getApplicationContext(), MapActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("email", noti.email);
        intent.putExtra("lat", noti.lat);
        intent.putExtra("lng", noti.lng);
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

        android.app.Notification notify=new android.app.Notification.Builder
                (getApplicationContext()).setContentTitle("JamTime")
                .setContentText("Tap to see where it's jammin'.")
                .setContentTitle("It's jammin' here!")
                .setSmallIcon(R.drawable.abc)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setWhen(System.currentTimeMillis()).build();

        notify.flags |= android.app.Notification.FLAG_AUTO_CANCEL;
        notif.notify(0, notify);
    }
}
