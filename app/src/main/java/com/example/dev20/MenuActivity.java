package com.example.dev20;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class MenuActivity extends AppCompatActivity {
    private static final String TAG = "MenuActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        if (isServicesOK()){
            openMap();
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
}
