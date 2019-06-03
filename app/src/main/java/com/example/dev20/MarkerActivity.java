package com.example.dev20;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
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

// Khi người dùng đăng nhập xong thì sẽ vào activity này
public class MarkerActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerDragListener {

    private static final String TAG = "MarkerActivity";
    private static final String SENDER_ID = "114752414746";
    private static final int REQUEST_LOCATION = 999;
    private FusedLocationProviderClient fusedLocationClient;
    private GoogleMap mMap;
    Button btnMark;
    Marker marker;

    // khởi tạo biến database
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker); // dùng view tên là activity_marker

        // gán giá trị vào biến mDatabase để dùng tính năng của Firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // cả cục này nói chung là để lấy vị trí hiện tại của người dùng
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(MarkerActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        } else {
            fusedLocationClient.getLastLocation()
                    // lấy thành công vị trí thì làm gì
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // tạo một cái dấu (marker) trên bản đồ để cho thấy vị trí hiện tại
                            marker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude()))
                                    .title("My Location")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                                    .draggable(true));
                            // di chuyển camera đến vị trí hiện tại
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 14));
                        }
                    });
        }

        // đoạn này để hiển thị bản đồ
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // tìm nút tên là btnMark để gửi vị trí hiện tại ở trong view
        btnMark = (Button) findViewById(R.id.btnMark);
        btnMark.setOnClickListener(new View.OnClickListener() { // click vào nút thì sao
            @Override
            public void onClick(View v) {
                // lấy thông tin đăng nhập của người dùng đã được lưu trong Firebase database
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String email;
                if (user != null) {
                    email = user.getEmail();
                } else {
                    email = "1";
                }
                // lấy thông tin toạ độ của marker
                LatLng position = marker.getPosition();
                String lat = Double.toString(position.latitude);
                String lng = Double.toString(position.longitude);
                // tạo 1 instance của class Notification tên là "noti" để gửi lên database
                Notification noti = new Notification(email, lat, lng);
                // tạo id ngẫu nhiên cho noti
                String notiId = createID();

                // gửi lên Firebase Messaging 1 tin nhắn với nội dung như dưới
                FirebaseMessaging fm = FirebaseMessaging.getInstance();
                fm.send(new RemoteMessage.Builder(SENDER_ID + "@fcm.googleapis.com")
                        .setMessageId(notiId)
                        .addData("email", email)
                        .addData("lat",lat)
                        .addData("lng",lng)
                        .build());

                // lưu thông tin noti vào Firebase database
                mDatabase.child("notis").push().setValue(noti);
                // Toast lên cho người dùng biết đã gửi dc tin nhắn
                Toast.makeText(MarkerActivity.this, "Sent jammed location", Toast.LENGTH_SHORT).show();
            }
        });

        // khi Firebase database thay đổi thì sao
        mDatabase.child("notis").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("DbChange", "Data changed!");
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                // với mỗi thay đổi trong database thì khởi tạo 1 instance class Notification tương ứng
                // và gửi lên push notification cho người dùng
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

    // các hàm có chữ "map" hay "marker" để tuỳ chỉnh bản đồ, cho phép di chuyển marker
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerDragListener(this);
        setUpMap();
    }

    private void setUpMap() {
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        } else {
            // permission has been granted, continue as usual
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        LatLng position=marker.getPosition();
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        LatLng position=marker.getPosition();
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        LatLng position=marker.getPosition();
    }

    // hàm tạo id ngẫu nhiên dựa vào ngày tạo
    public String createID(){
        Date now = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        return dateFormat.format(now);
    }

    // hàm gửi push noti nhưng bh đang lỗi
    public void sendNotification(Notification noti){
        Log.d("@sendNotification", "we're in");
        // đoạn dưới để gửi data từ activity này sang activity khác
        Intent intent = new Intent(getApplicationContext(), MapActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("email", noti.email);
        intent.putExtra("lat", noti.lat);
        intent.putExtra("lng", noti.lng);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // khởi tạo chung cho nhiều noti
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

        // thông tin trong noti, gồm tiêu đề, nội dung, ảnh ọt, thời gian, vv..vv..
        android.app.Notification notify=new android.app.Notification.Builder
                (getApplicationContext()).setContentTitle("JamTime")
                .setContentText("Tap to see where it's jammin'.")
                .setContentTitle("It's jammin' here!")
                .setSmallIcon(R.drawable.abc)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setWhen(System.currentTimeMillis()).build();

        notify.flags |= android.app.Notification.FLAG_AUTO_CANCEL;
        notif.notify(0, notify); // push noti
    }
}
