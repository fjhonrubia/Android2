package com.odobo.twlocatorapi.util.db.dao;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.odobo.twlocatorapi.model.Tweet;
import com.odobo.twlocatorapi.util.db.DBHelper;

public class TweetDAO {
    public static final String TABLE_TWEET = "TWEET";

    // Table field constants 
    public static final String KEY_TWEET_ID = "_id";
    public static final String KEY_TWEET_TEXT = "text";
    public static final String KEY_TWEET_LATITUDE = "latitude";
    public static final String KEY_TWEET_LONGITUDE = "longitude";
    public static final String KEY_TWEET_IMAGE_URL = "imageUrl";


    public static final String SQL_CREATE_TWEET_TABLE =
            "create table "
                    + TABLE_TWEET
                    + "( " + KEY_TWEET_ID + " integer primary key autoincrement, "
                    + KEY_TWEET_TEXT + " text not null,"
                    + KEY_TWEET_LATITUDE + " float, "
                    + KEY_TWEET_LONGITUDE + " float, "
                    + KEY_TWEET_IMAGE_URL + " text"
                    + ");";

    public static final String[] allColumns = {
            KEY_TWEET_ID,
            KEY_TWEET_TEXT,
            KEY_TWEET_LATITUDE,
            KEY_TWEET_LONGITUDE,
            KEY_TWEET_IMAGE_URL
    };

    private Context context;

    public TweetDAO(Context context) {
        this.context = context;
    }

    /**
     * This method inserts a new Tweet in the database.
     *
     * If Tweet is null returns 0
     * If there's an error, returns -1
     * else the new Tweet's id
     *
     * @param tweet
     * @return
     */
    public long insert(Tweet tweet) {
        if (tweet == null) {
            return 0;
        }
        // insert
        DBHelper db = new DBHelper(context);

        long id = db.getWritableDatabase().insert(TABLE_TWEET, null, getContentValues(tweet));
        //tweet.setId(id);
        db.close();

        return id;
    }


    public Cursor queryCursor() {
        // select
        DBHelper db = new DBHelper(context);

        Cursor c = db.getReadableDatabase().query(TABLE_TWEET, allColumns, null , null, null, null, null);

        return c;
    }
/*
    public Cursor queryCursor(long id) {
        Country country = null;

        DBHelper db = new DBHelper(context);

        String where = KEY_TWEET_ID + "=" + id;
        Cursor c = db.getReadableDatabase().query(TABLE_TWEET, allColumns, where, null, null, null, null);

        return c;
    }


    public Country query(long id) {
        Country country = null;

        Cursor c = queryCursor(id);

        if (c != null) {
            if (c.getCount() > 0) {
                c.moveToFirst();
                country = countryFromCursor(c);
            }
        }
        c.close();
        return country;
    }
*/

    // convenience method
    public static Tweet tweetFromCursor(final Cursor c) {
        assert c != null;

        Tweet tweet = new Tweet();
        tweet.setId(c.getInt(c.getColumnIndex(KEY_TWEET_ID)));
        tweet.setLatitude(c.getDouble(c.getColumnIndex(KEY_TWEET_LATITUDE)));
        tweet.setLongitude(c.getDouble(c.getColumnIndex(KEY_TWEET_LONGITUDE)));
        tweet.setText(c.getString(c.getColumnIndex(KEY_TWEET_TEXT)));
        tweet.setProfileUrl(c.getString(c.getColumnIndex(KEY_TWEET_IMAGE_URL)));

        return tweet;
    }

    public static ContentValues getContentValues(Tweet tweet) {
        ContentValues content = new ContentValues();

        content.put(KEY_TWEET_TEXT, tweet.getText());
        content.put(KEY_TWEET_LATITUDE, tweet.getLatitude());
        content.put(KEY_TWEET_LONGITUDE, tweet.getLongitude());
        content.put(KEY_TWEET_IMAGE_URL, tweet.getProfileUrl());

        return content;
    }

    public void delete(long id) {
        DBHelper db = new DBHelper(context);

        db.getWritableDatabase().delete(TABLE_TWEET,  KEY_TWEET_ID + " = " + id, null);

        db.close();
    }

    public void deleteAll() {
        DBHelper db = new DBHelper(context);

        db.getWritableDatabase().delete(TABLE_TWEET,  null, null);

        db.close();
    }


    public int update(long id, Tweet tweet) {
        if (tweet == null) {
            return 0;
        }

        DBHelper db = new DBHelper(context);

        int numRecordsUpdated = db.getWritableDatabase().update(TABLE_TWEET, getContentValues(tweet), KEY_TWEET_ID + "=" + id, null);

        db.close();

        return numRecordsUpdated;
    }


}
