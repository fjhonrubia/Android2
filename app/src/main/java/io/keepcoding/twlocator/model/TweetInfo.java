package io.keepcoding.twlocator.model;

import java.lang.ref.WeakReference;

/**
 * Created by javi on 25/4/16.
 */
public class TweetInfo {

    private long mId;
    private String mText;
    private WeakReference<Tweet> mTweet;

    public TweetInfo(long id, String text, WeakReference<Tweet> tweet) {
        mId = id;
        mText = text;
        mTweet = tweet;
    }

    public TweetInfo(String text, WeakReference<Tweet> tweet) {
        this(DBHelper.INVALID_ID, text, tweet);
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }

    public WeakReference<Tweet> getTweet() {
        return mTweet;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

}
