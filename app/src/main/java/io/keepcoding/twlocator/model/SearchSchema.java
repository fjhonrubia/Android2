package io.keepcoding.twlocator.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;

/**
 * Created by javi on 25/4/16.
 */
public class SearchSchema {

    public static final class SearchTable {
        public static final String NAME = "SEARCH";

        public static final class Cols {
            public static final String SEARCH_ID = "_id";
            public static final String SEARCH_LATITUDE = "latitude";
            public static final String SEARCH_LONGITUDE = "longitude";
            public static final String SEARCH_TEXT = "text";
        }
    }

    private final WeakReference<Context> context;
    public static final String[] allColumns = {
            SearchTable.Cols.SEARCH_ID,
            SearchTable.Cols.SEARCH_TEXT,
            SearchTable.Cols.SEARCH_LATITUDE,
            SearchTable.Cols.SEARCH_LONGITUDE
    };

    public SearchSchema(@NonNull Context context) {
        this.context = new WeakReference<>(context);
    }

    public long insert(@NonNull Search data) {
        if (data == null) {
            return DBHelper.INVALID_ID;
        }

        final DBHelper dbHelper = DBHelper.getInstance(context.get());
        SQLiteDatabase db = DBHelper.getDb(dbHelper);

        db.beginTransaction();
        long id = DBHelper.INVALID_ID;

        try {
            id = db.insert(SearchTable.NAME, null, getContentValues(data));
            db.setTransactionSuccessful();

        } finally {
            db.endTransaction();
            dbHelper.close();
        }

        return id;
    }


    public static ContentValues getContentValues(Search search) {

        ContentValues content = new ContentValues();
        content.put(SearchTable.Cols.SEARCH_TEXT, search.getText());
        content.put(SearchTable.Cols.SEARCH_LATITUDE, search.getLatitude());
        content.put(SearchTable.Cols.SEARCH_LONGITUDE, search.getLongitude());

        return content;
    }


    public void delete(long currentId, long lastId) {
        final DBHelper dbHelper = DBHelper.getInstance(context.get());
        SQLiteDatabase db = DBHelper.getDb(dbHelper);

        db.delete(SearchTable.NAME,
                SearchTable.Cols.SEARCH_ID + "!=? AND " + SearchTable.Cols.SEARCH_ID + "!=?",
                new String[]{"" + currentId, "" + lastId});

        db.close();
    }


    public Search query(long id) {
        Search search = null;

        final DBHelper dbHelper = DBHelper.getInstance(context.get());
        SQLiteDatabase db = DBHelper.getDb(dbHelper);

        final String whereClause = SearchTable.Cols.SEARCH_ID + "=" + id;
        Cursor cursor = db.query(SearchTable.NAME, allColumns, whereClause, null, null, null, SearchTable.Cols.SEARCH_ID);

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();

                search = searchFromCursor(cursor);
            }
        }

        cursor.close();
        db.close();

        return search;
    }

    public long getIdLastSearch() {
        long idLastSearch = DBHelper.INVALID_ID;

        final DBHelper dbHelper = DBHelper.getInstance(context.get());
        SQLiteDatabase db = DBHelper.getDb(dbHelper);

        String query = "SELECT " + SearchTable.Cols.SEARCH_ID + " " +
                "FROM " + SearchTable.NAME + " " +
                "ORDER BY " + SearchTable.Cols.SEARCH_ID + " DESC " +
                "LIMIT 1";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();

                idLastSearch = cursor.getLong(cursor.getColumnIndex(SearchTable.Cols.SEARCH_ID));
            }
        }

        cursor.close();
        db.close();

        return idLastSearch;
    }

    @NonNull public static Search searchFromCursor(Cursor cursor) {
        Search search;
        search = new Search(cursor.getDouble(cursor.getColumnIndex(SearchTable.Cols.SEARCH_LATITUDE)),
                cursor.getDouble(cursor.getColumnIndex(SearchTable.Cols.SEARCH_LONGITUDE)),
                cursor.getString(cursor.getColumnIndex(SearchTable.Cols.SEARCH_TEXT)));
        search.setId(cursor.getLong(cursor.getColumnIndex(SearchTable.Cols.SEARCH_ID)));
        return search;
    }


}
