package com.example.dev20;

public class Notification {
    public String email;
    public String lat;
    public String lng;

    public Notification(String email, String lat, String lng) {
        this.email = email;
        this.lat = lat;
        this.lng = lng;
    }

    public Notification() {
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String id) {
        this.email = email;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }
}
