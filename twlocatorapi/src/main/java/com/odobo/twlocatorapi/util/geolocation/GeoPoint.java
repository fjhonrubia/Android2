package com.odobo.twlocatorapi.util.geolocation;


import android.location.Address;
import android.support.annotation.NonNull;

public class GeoPoint {
    private double latitude;
    private double longitude;
    private String name;

    public GeoPoint(@NonNull Address address) {
        if (address == null) {
            return;
        }

        this.name = address.getLocality();
        this.latitude = address.getLatitude();
        this.longitude = address.getLongitude();
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
