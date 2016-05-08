package io.keepcoding.twlocator.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static io.keepcoding.twlocator.model.TweetSchema.*;
import static io.keepcoding.twlocator.model.TweetInfoSchema.*;
import static io.keepcoding.twlocator.model.SearchSchema.*;

public class DBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "twlocator.sqlite";
    public static final int DATABASE_VERSION = 3;
    public static final long INVALID_ID = -1;

    private static DBHelper sharedInstance;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static DBHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sharedInstance == null) {
            sharedInstance = new DBHelper(context.getApplicationContext());
        }
        return sharedInstance;
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);

        // called everytime a DB connection is opened. We activate foreing keys to have ON_CASCADE deletion
        db.execSQL("PRAGMA foreign_keys = ON");

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        //Se crean las 3 tablas del modelo de datos
        db.execSQL("create table "
                + SearchSchema.SearchTable.NAME + "( "
                + SearchTable.Cols.SEARCH_ID + " INTEGER primary key autoincrement, "
                + SearchTable.Cols.SEARCH_TEXT + " TEXT not null, "
                + SearchTable.Cols.SEARCH_LATITUDE + " REAL not null, "
                + SearchTable.Cols.SEARCH_LONGITUDE + " REAL not null "
                + ");");

        db.execSQL("create table "
                + TweetTable.NAME + "( "
                + TweetTable.Cols.TWEET_ID + " INTEGER primary key autoincrement, "
                + TweetTable.Cols.TWEET_USERNAME + " TEXT not null,"
                + TweetTable.Cols.TWEET_TEXT + " TEXT not null,"
                + TweetTable.Cols.TWEET_LATITUDE + " REAL not null,"
                + TweetTable.Cols.TWEET_LONGITUDE + " REAL not null,"
                + TweetTable.Cols.TWEET_PHOTO_PROFILE_URL + " TEXT not null, "
                + TweetTable.Cols.TWEET_SEARCH + " INTEGER,"
                + "FOREIGN KEY(" + TweetTable.Cols.TWEET_SEARCH + ") REFERENCES " + SearchSchema.SearchTable.NAME + "(" + SearchTable.Cols.SEARCH_ID + ") ON DELETE CASCADE"
                + ");");

        db.execSQL("create table "
                + TweetInfoSchema.TweetInfoTable.NAME + "( "
                + TweetInfoTable.Cols.TWEET_INFO_URL_ID + " INTEGER primary key autoincrement, "
                + TweetInfoTable.Cols.TWEET_INFO_URL_URL + " TEXT not null,"
                + TweetInfoTable.Cols.TWEET_INFO_URL_TWEET + " INTEGER,"
                + "FOREIGN KEY(" + TweetInfoTable.Cols.TWEET_INFO_URL_TWEET + ") REFERENCES " + TweetTable.NAME + "(" + TweetTable.Cols.TWEET_ID + ") ON DELETE CASCADE"
                + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 1:
                // upgrades for version 1->2
                Log.i("DBHelper", "Migrating from V1 to V2");

            case 2:
                // upgrades for version 2->3

            case 3:
                // upgrades for version 3->4
        }
    }


    public static SQLiteDatabase getDb(DBHelper dbHelper) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
        } catch (SQLiteException e) {
            db = dbHelper.getReadableDatabase();
        }
        return db;
    }

}
