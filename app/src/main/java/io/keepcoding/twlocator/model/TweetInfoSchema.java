package io.keepcoding.twlocator.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by javi on 25/4/16.
 */
public class TweetInfoSchema {

    public static final class TweetInfoTable {
        public static final String NAME = "TWEET_INFO";

        public static final class Cols {
            public static final String TWEET_INFO_URL_ID = "_id";
            public static final String TWEET_INFO_URL_URL = "url";
            public static final String TWEET_INFO_URL_TWEET = "tweet";
        }
    }

    private static WeakReference<Context> context = null;
    public static final String[] allColumns = {
            TweetInfoTable.Cols.TWEET_INFO_URL_ID,
            TweetInfoTable.Cols.TWEET_INFO_URL_URL,
            TweetInfoTable.Cols.TWEET_INFO_URL_TWEET
    };

    public TweetInfoSchema(@NonNull Context context) {
        this.context = new WeakReference<>(context);
    }

    public long insert(@NonNull TweetInfo data) {
        if (data == null) {
            return DBHelper.INVALID_ID;
        }

        final DBHelper dbHelper = DBHelper.getInstance(context.get());
        SQLiteDatabase db = DBHelper.getDb(dbHelper);

        db.beginTransaction();
        long id = DBHelper.INVALID_ID;

        try {
            id = db.insert(TweetInfoTable.NAME, null, getContentValues(data));
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            dbHelper.close();
        }

        return id;
    }


    public static ContentValues getContentValues(TweetInfo tweetInfoURL) {

        ContentValues content = new ContentValues();
        content.put(TweetInfoTable.Cols.TWEET_INFO_URL_URL, tweetInfoURL.getText());
        content.put(TweetInfoTable.Cols.TWEET_INFO_URL_TWEET, tweetInfoURL.getTweet().get().getId());

        return content;
    }


    public ArrayList<TweetInfo> query(Tweet tweet) {
        ArrayList<TweetInfo> tweetInfoURLArrayList = new ArrayList<>();

        final DBHelper dbHelper = DBHelper.getInstance(context.get());
        SQLiteDatabase db = DBHelper.getDb(dbHelper);

        final String whereClause = TweetInfoTable.Cols.TWEET_INFO_URL_TWEET + "=" + tweet.getId();
        Cursor cursor = db.query(TweetInfoTable.NAME, allColumns, whereClause, null, null, null, TweetInfoTable.Cols.TWEET_INFO_URL_ID);

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();

                TweetInfo tweetInfoURL = tweetFromCursor(cursor);
                tweetInfoURLArrayList.add(tweetInfoURL);
            }
        }

        cursor.close();
        db.close();

        return tweetInfoURLArrayList;
    }

    @NonNull public static TweetInfo tweetFromCursor(Cursor cursor) {
        TweetInfo tweetInfo;
        TweetSchema tweetDAO = new TweetSchema(context.get());
        Tweet tweet = tweetDAO.query(cursor.getLong(cursor.getColumnIndex(TweetInfoTable.Cols.TWEET_INFO_URL_ID)));
        tweetInfo = new TweetInfo(
                cursor.getString(cursor.getColumnIndex(TweetInfoTable.Cols.TWEET_INFO_URL_URL)),
                new WeakReference<>(tweet));
        tweetInfo.setId(cursor.getLong(cursor.getColumnIndex(TweetInfoTable.Cols.TWEET_INFO_URL_ID)));
        return tweetInfo;
    }

}
