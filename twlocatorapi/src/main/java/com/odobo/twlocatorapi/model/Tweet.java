package com.odobo.twlocatorapi.model;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import twitter4j.GeoLocation;
import twitter4j.Status;
import twitter4j.User;

public class Tweet {
    private long id;
    private String text;
    private double latitude;
    private double longitude;
    private String profileUrl;

    public Tweet() {


    }

    public Tweet(@NonNull Status status) {
        if (status == null) {
            return;
        }

        this.id = status.getId();
        this.text = status.getText();
        GeoLocation location = status.getGeoLocation();
        if (location != null) {
            this.latitude = location.getLatitude();
            this.longitude = location.getLongitude();
        }
        User user = status.getUser();
        if (user != null) {
            this.profileUrl = user.getProfileImageURL();
        }
    }


    public static List<Tweet>mapTweetsFromStatus(List<Status> tweets) {
        if (tweets == null) {
            return null;
        }

        List<Tweet> myTweets = new ArrayList<Tweet>();

        for (final Status tweet: tweets) {
            Tweet tw = new Tweet(tweet);
            myTweets.add(tw);
        }
        return myTweets;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }
}
