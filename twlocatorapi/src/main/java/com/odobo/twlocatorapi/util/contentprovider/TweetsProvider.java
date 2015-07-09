package com.odobo.twlocatorapi.util.contentprovider;


import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import com.odobo.twlocatorapi.util.db.dao.TweetDAO;

public class TweetsProvider extends ContentProvider {

    public static final String TWEETS_PROVIDER = "com.odobo.twlocator.provider";

    public static final Uri TWEETS_URI = Uri.parse("content://" + TWEETS_PROVIDER + "/tweets");

    // Create the constants used to differentiate between the different URI requests.
    private static final int ALL_TWEETS = 1;
    private static final int SINGLE_TWEET = 2;

    private static final UriMatcher uriMatcher;
    // Populate the UriMatcher object, where a URI ending in ‘elements’ will correspond to a request for all items, and ‘elements/[rowID]’ represents a single row.
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(TWEETS_PROVIDER, "tweets", ALL_TWEETS);
        uriMatcher.addURI(TWEETS_PROVIDER, "tweets/#", SINGLE_TWEET);

    }

    @Override
    public boolean onCreate() {
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        Cursor cursor = null;
        String rowID = null;
        long id;
        switch (uriMatcher.match(uri)) {

            case SINGLE_TWEET:
                rowID = uri.getPathSegments().get(1);
                id = Long.getLong(rowID).intValue();

                // cursor = (new TweetDAO(getContext())).queryCursor(id);
                break;
            case ALL_TWEETS:

                cursor = (new TweetDAO(getContext())).queryCursor();
                break;

            default:
                break;
        }

        // Return the result Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        String rowID = null;
        long id;
        int deleteCount = 0;
        // If this is a row URI, limit the deletion to the specified row.
        switch (uriMatcher.match(uri)) {
            case ALL_TWEETS:
                rowID = uri.getPathSegments().get(1);
                id = Long.getLong(rowID).intValue();
                (new TweetDAO(getContext())).delete(id);
                break;

            default:
                break;
        }

        // Perform the deletion.
        // Notify any observers of the change in the data set.
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the number of deleted items.
        return deleteCount;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }

}
