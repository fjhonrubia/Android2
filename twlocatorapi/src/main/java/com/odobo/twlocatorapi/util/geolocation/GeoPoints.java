package com.odobo.twlocatorapi.util.geolocation;

import android.location.Address;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GeoPoints {
    public static @Nullable String[] getAddressNames(@NonNull List<GeoPoint> addresses) {
        if (addresses == null || addresses.size() == 0) {
            return null;
        }

        List<String> returnValue = new ArrayList<String>(addresses.size());
        for (GeoPoint geoPoint: addresses) {
            returnValue.add(geoPoint.getName());
        }

        return returnValue.toArray(new String[addresses.size()]);
    }

    public static @Nullable List<GeoPoint> getGeoPoints(@NonNull List<Address> addresses) {
        if (addresses == null || addresses.size() == 0) {
            return null;
        }

        List<GeoPoint> returnValue = new ArrayList<GeoPoint>(addresses.size());
        for (Address address: addresses) {
            GeoPoint geoPoint = new GeoPoint(address);
            returnValue.add(geoPoint);
        }

        return returnValue;
    }

}
