package com.odobo.twlocatorapi.util.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.odobo.twlocatorapi.util.db.dao.TweetDAO;

public class DBHelper extends SQLiteOpenHelper{

    public static final String DATABASE_NAME = "weather_world.sqlite";
    public static final int DATABASE_VERSION = 1;

    public static final String[] CREATE_DATABASE = {
            TweetDAO.SQL_CREATE_TWEET_TABLE,
    };


    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        createDB(sqLiteDatabase);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);

        // called everytime a DB connection is opened. We activate foreing keys to have ON_CASCADE deletion
        db.execSQL("PRAGMA foreign_keys = ON");

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {

    }

    // utility method to create DB
    private void createDB(SQLiteDatabase db) {
        for (String sql: CREATE_DATABASE) {
            db.execSQL(sql);
        }
    }


}
