package io.keepcoding.twlocator.model;

/**
 * Created by javi on 25/4/16.
 */
public class Search {

    private long mId;
    private double mLatitude;
    private double mLongitude;
    private String mText;

    public Search(double latitude, double longitude, String text) {
        mLatitude = latitude;
        mLongitude = longitude;
        mText = text;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }


}
