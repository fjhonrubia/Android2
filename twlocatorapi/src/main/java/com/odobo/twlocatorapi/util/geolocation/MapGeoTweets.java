package com.odobo.twlocatorapi.util.geolocation;


import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

import twitter4j.Status;

public class MapGeoTweets {
    public static void addGeoTweetsToMap(List<Status> tweets, final GoogleMap googleMap, final Context context) {
        if (tweets == null || googleMap == null) {
            return;
        }

        for (final Status tweet: tweets) {
            if (tweet.getGeoLocation() != null) {
                final LatLng position = new LatLng(tweet.getGeoLocation().getLatitude(), tweet.getGeoLocation().getLongitude());
                final String profileImageUrl = tweet.getUser().getProfileImageURL();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        BitmapDescriptor bitmapDescriptor = null;

                        try {
                            bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(Picasso.with(context).load(profileImageUrl).get());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        final MarkerOptions marker = new MarkerOptions().position(position).title(tweet.getText()).icon(bitmapDescriptor);

                        (new Handler(Looper.getMainLooper())).post(new Runnable() {
                            @Override
                            public void run() {
                                googleMap.addMarker(marker);

                            }
                        });
                    }
                }).start();



            }
        }
    }
}
