package com.example.dev20;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

// Khi người dùng click vào push notification thì sẽ vào activity này
public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapActivity";

    private FusedLocationProviderClient fusedLocationClient;
    private static final int REQUEST_LOCATION = 999;

    private GoogleMap mMap;
    String email, lat, lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map); // dùng view activity_map

        // lấy vị trí hiện tại của người dùng và thêm vào bản đồ
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(MapActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        } else {
            // permission has been granted, continue as usual
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(MapActivity.this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude()))
                                    .title("My Location")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                        }
                    });
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    // gán thông tin được chuyển từ noti sang các biến đã khởi tạo ở đây
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle extras = getIntent().getExtras();
        email = extras.getString("email");
        lat = extras.getString("lat");
        lng = extras.getString("lng");
    }

    // tạo marker điểm tắc đường với toạ độ được gửi lên từ push noti
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (!lat.equals("") && !lng.equals("")){
            LatLng jam = new LatLng(Long.parseLong(lat), Long.parseLong(lng));
            mMap.addMarker(new MarkerOptions().position(jam).title("Jam!").snippet("Reported by user: " + email));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(jam));
        } else {
            Toast.makeText(this, "Can't get jam data.", Toast.LENGTH_SHORT).show();
        }
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
}
