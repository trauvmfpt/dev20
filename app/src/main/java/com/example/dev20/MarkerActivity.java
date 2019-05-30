package com.example.dev20;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
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
import java.util.Locale;

public class MarkerActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerDragListener {

    private static final String TAG = "MarkerActivity";
    private static final String SENDER_ID = "114752414746";
    private FusedLocationProviderClient fusedLocationClient;
    private GoogleMap mMap;
    Button btnMark;
    Marker marker;

    private DatabaseReference mDatabase;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        marker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude()))
                                .title("My Location")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                                .draggable(true));
                    }
                });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnMark = (Button) findViewById(R.id.btnMark);
        btnMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    String email = user.getEmail();
                    LatLng position = marker.getPosition();
                    String lat = Double.toString(position.latitude);
                    String lng = Double.toString(position.longitude);
                    Notification noti = new Notification(email, lat, lng);
                    String notiId = createID();

                    FirebaseMessaging fm = FirebaseMessaging.getInstance();
                    fm.send(new RemoteMessage.Builder(SENDER_ID + "@fcm.googleapis.com")
                            .setMessageId(notiId)
                            .addData("email", email)
                            .addData("lat",lat)
                            .addData("lng",lng)
                            .build());

                    mDatabase.child("notis").push().setValue(noti);

                } else {
                    Toast.makeText(MarkerActivity.this, "You're not signed in?", Toast.LENGTH_SHORT).show();
                }
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerDragListener(this);

        setUpMap();
    }

    @SuppressLint("MissingPermission")
    private void setUpMap() {
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        LatLng position=marker.getPosition();

        Log.d(getClass().getSimpleName(), String.format("Drag from %f:%f",
                position.latitude,
                position.longitude));
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        LatLng position=marker.getPosition();

        Log.d(getClass().getSimpleName(),
                String.format("Dragging to %f:%f", position.latitude,
                        position.longitude));

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        LatLng position=marker.getPosition();

        Log.d(getClass().getSimpleName(), String.format("Dragged to %f:%f",
                position.latitude,
                position.longitude));

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
