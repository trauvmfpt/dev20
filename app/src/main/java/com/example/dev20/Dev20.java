package com.example.dev20;

import android.app.Application;
import java.net.URISyntaxException;

public class Dev20 extends Application {

    private static final String URL = "http://yoururl.com";
    @Override
    public void onCreate() {

        super.onCreate();
//        try {
//            mSocket = IO.socket(URL);
//        } catch (URISyntaxException e) {
//            throw new RuntimeException(e);
//        }
    }
}
