package io.keepcoding.twlocator.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by javi on 25/4/16.
 */
public class TweetSchema {

    public static final class TweetTable {
        public static final String NAME = "TWEET";

        public static final class Cols {
            public static final String TWEET_ID = "_id";
            public static final String TWEET_SEARCH = "search";
            public static final String TWEET_USERNAME = "username";
            public static final String TWEET_TEXT = "text";
            public static final String TWEET_LATITUDE = "latitude";
            public static final String TWEET_LONGITUDE = "longitude";
            public static final String TWEET_PHOTO_PROFILE_URL = "photoProfileUrl";
        }
    }

    private static WeakReference<Context> context = null;
    public static final String[] allColumns = {
            TweetTable.Cols.TWEET_ID,
            TweetTable.Cols.TWEET_USERNAME,
            TweetTable.Cols.TWEET_TEXT,
            TweetTable.Cols.TWEET_LATITUDE,
            TweetTable.Cols.TWEET_LONGITUDE,
            TweetTable.Cols.TWEET_PHOTO_PROFILE_URL,
            TweetTable.Cols.TWEET_SEARCH
    };

    public TweetSchema(@NonNull Context context) {
        this.context = new WeakReference<>(context);
    }

    public long insert(@NonNull Tweet data) {
        if (data == null) {
            return DBHelper.INVALID_ID;
        }

        final DBHelper dbHelper = DBHelper.getInstance(context.get());
        SQLiteDatabase db = DBHelper.getDb(dbHelper);

        db.beginTransaction();
        long id = DBHelper.INVALID_ID;

        try {
            id = db.insert(TweetTable.NAME, null, getContentValues(data));
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            dbHelper.close();
        }

        return id;
    }


    public static ContentValues getContentValues(Tweet tweet) {

        ContentValues content = new ContentValues();
        content.put(TweetTable.Cols.TWEET_USERNAME, tweet.getUserName());
        content.put(TweetTable.Cols.TWEET_PHOTO_PROFILE_URL, tweet.getURLUserPhotoProfile());
        content.put(TweetTable.Cols.TWEET_TEXT, tweet.getText());
        content.put(TweetTable.Cols.TWEET_LATITUDE, tweet.getLatitude());
        content.put(TweetTable.Cols.TWEET_LONGITUDE, tweet.getLongitude());
        content.put(TweetTable.Cols.TWEET_SEARCH, tweet.getSearch().getId());

        return content;
    }

    public Tweet query(long id) {
        Tweet tweet = null;

        final DBHelper dbHelper = DBHelper.getInstance(context.get());
        SQLiteDatabase db = DBHelper.getDb(dbHelper);

        final String whereClause = TweetTable.Cols.TWEET_ID + "=" + id;
        Cursor cursor = db.query(TweetTable.NAME, allColumns, whereClause, null, null, null, TweetTable.Cols.TWEET_ID);

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();

                tweet = tweetFromCursor(cursor);
            }
        }

        cursor.close();
        db.close();

        return tweet;
    }

    public static List<Tweet> query(Search search) {
        List<Tweet> tweets = new ArrayList<>();
        Tweet tweet = null;

        final DBHelper dbHelper = DBHelper.getInstance(context.get());
        SQLiteDatabase db = DBHelper.getDb(dbHelper);

        final String whereClause = TweetTable.Cols.TWEET_SEARCH + "=" + search.getId();
        Cursor cursor = db.query(TweetTable.NAME, allColumns, whereClause, null, null, null, TweetTable.Cols.TWEET_ID);

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (cursor.moveToNext()) {
                    tweet = tweetFromCursor(cursor);
                    tweets.add(tweet);
                }
            }
        }

        cursor.close();
        db.close();

        return tweets;
    }

    @NonNull public static Tweet tweetFromCursor(Cursor cursor) {
        Tweet tweet;
        SearchSchema searchDAO = new SearchSchema(context.get());
        Search search = searchDAO.query(cursor.getLong(cursor.getColumnIndex(TweetTable.Cols.TWEET_SEARCH)));
        tweet = new Tweet(cursor.getString(cursor.getColumnIndex(TweetTable.Cols.TWEET_USERNAME)),
                cursor.getString(cursor.getColumnIndex(TweetTable.Cols.TWEET_PHOTO_PROFILE_URL)),
                cursor.getString(cursor.getColumnIndex(TweetTable.Cols.TWEET_TEXT)),
                search,
                cursor.getDouble(cursor.getColumnIndex(TweetTable.Cols.TWEET_LATITUDE)),
                cursor.getDouble(cursor.getColumnIndex(TweetTable.Cols.TWEET_LONGITUDE)));
        tweet.setId(cursor.getLong(cursor.getColumnIndex(TweetTable.Cols.TWEET_ID)));
        return tweet;
    }

}
