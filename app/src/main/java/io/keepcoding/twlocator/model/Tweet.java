package io.keepcoding.twlocator.model;


import java.lang.ref.WeakReference;

/**
 * Created by javi on 25/4/16.
 */
public class Tweet {

    private long mId;
    private String mUserName;
    private String mURLUserPhotoProfile;
    private String mText;
    private double mLatitude;
    private double mLongitude;
    private WeakReference<Search> mSearch;

    public Tweet(String userName, String URLUserPhotoProfile, String text, Search search) {
        mUserName = userName;
        mURLUserPhotoProfile = URLUserPhotoProfile;
        mText = text;
        mSearch = new WeakReference<>(search);
    }

    public Tweet(String userName, String URLUserPhotoProfile, String text, Search search, double latitude, double longitude) {
        this(userName, URLUserPhotoProfile, text, search);
        mLatitude = latitude;
        mLongitude = longitude;
    }


    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public String getUserName() {
        return mUserName;
    }


    public String getURLUserPhotoProfile() {
        return mURLUserPhotoProfile;
    }


    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public Search getSearch() {
        return mSearch.get();
    }


}
