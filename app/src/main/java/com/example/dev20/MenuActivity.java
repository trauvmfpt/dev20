package com.example.dev20;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
                                mDatabase.child("notis").child(notiId).setValue(noti);
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
}
