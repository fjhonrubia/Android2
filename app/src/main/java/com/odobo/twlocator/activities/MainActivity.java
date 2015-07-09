package com.odobo.twlocator.activities;

import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.odobo.twlocator.R;
import com.odobo.twlocatorapi.model.Tweet;
import com.odobo.twlocatorapi.util.contentprovider.TweetsProvider;
import com.odobo.twlocatorapi.util.db.dao.TweetDAO;
import com.odobo.twlocatorapi.util.geolocation.GeoPoint;
import com.odobo.twlocatorapi.util.geolocation.GeoPoints;
import com.odobo.twlocatorapi.util.geolocation.GeoTweets;
import com.odobo.twlocatorapi.util.geolocation.MapGeoTweets;
import com.odobo.twlocatorapi.util.map.MapHelper;
import com.odobo.twlocatorapi.util.net.NetworkHelper;
import com.odobo.twlocatorapi.util.twitter.ConnectTwitterTask;
import com.odobo.twlocatorapi.util.twitter.Globals;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import twitter4j.AsyncTwitter;
import twitter4j.Status;

public class MainActivity extends ActionBarActivity implements ConnectTwitterTask.OnConnectTwitterListener
        , LoaderManager.LoaderCallbacks<Cursor> {

    GoogleMap googleMap;
    ConnectTwitterTask twitterTask;
    private static final int URL_LOADER = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (NetworkHelper.isNetworkConnectionOK(new WeakReference<>(getApplication()))) {
            twitterTask = new ConnectTwitterTask(this);
            twitterTask.setListener(this);

            twitterTask.execute();
        } else {
            Toast.makeText(this, getString(R.string.error_network), Toast.LENGTH_LONG).show();
            getLoaderManager().initLoader(URL_LOADER, null, this);

        }

        this.initializeMap();
        this.handleIntent(getIntent());
    }


    @Override
    public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle)
    {
    /*
     * Takes action based on the ID of the Loader that's being created
     */
        switch (loaderID) {
            case URL_LOADER:
                // Returns a new CursorLoader
                return new CursorLoader(
                        this,   // Parent activity context
                        TweetsProvider.TWEETS_URI,        // Table to query
                        TweetDAO.allColumns,     // Projection to return
                        null,            // No selection clause
                        null,            // No selection arguments
                        null             // Default sort order
                );
            default:
                // An invalid id was passed in
                return null;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.d("","finished");

        final List<Tweet> tweets = new ArrayList<Tweet>();

        while (cursor.moveToNext()) {
            Tweet tweet = TweetDAO.tweetFromCursor(cursor);

            tweets.add(tweet);
        }
        MapHelper.centerMapInPosition(googleMap, tweets.get(0).getLatitude(), tweets.get(0).getLongitude());

        updateMapWithTweets(tweets);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchManager searchManager = (SearchManager) MainActivity.this.getSystemService(Context.SEARCH_SERVICE);

        SearchView searchView = null;
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(MainActivity.this.getComponentName()));
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search) {
            onSearchRequested();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onSearchRequested() {
        Bundle appData = new Bundle();
        startSearch(null, false, appData, false);
        return true;
    }

    @Override
    public void twitterConnectionFinished() {
        Toast.makeText(this, getString(R.string.twiiter_auth_ok), Toast.LENGTH_SHORT).show();
    }

    private void initializeMap() {
        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                    R.id.map)).getMap();

            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(getApplicationContext(),
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                        .show();
            } else {
                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setRotateGesturesEnabled(false);
            }
        }
    }

    // Search stuff

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            getLocationFromAddress(query);
        }
    }


    private void getLocationFromAddress(String address) {

        Geocoder geoCoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geoCoder.getFromLocationName(address , 1);
            if (addresses != null) {
                final List<GeoPoint> geoPoints = GeoPoints.getGeoPoints(addresses);
                if (geoPoints == null) {
                    Toast.makeText(this, getString(R.string.error_getting_location), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (addresses.size() > 1) {
                    String[] names = GeoPoints.getAddressNames(geoPoints);

                    AlertDialog.Builder b = new AlertDialog.Builder(this);
                    b.setTitle(getString(R.string.select_address));
                    b.setItems(names, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getTweetsFromLocationAt(geoPoints.get(which));

                            dialog.dismiss();
                        }
                    });

                    b.show();
                } else {
                    getTweetsFromLocationAt(geoPoints.get(0));
                }
            } else {
                Toast.makeText(this, getString(R.string.error_getting_location), Toast.LENGTH_SHORT).show();
            }
        } catch(Exception e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.error_getting_location), Toast.LENGTH_SHORT).show();
        }
    }

    public void getTweetsFromLocationAt(GeoPoint geoPoint) {
        if (geoPoint == null) {
            return;
        }
        MapHelper.centerMapInPosition(googleMap, geoPoint.getLatitude(), geoPoint.getLongitude());

        AsyncTwitter asyncTwitter = Globals.getSharedTwitterHelper(this).getAsyncTwitter();

        GeoTweets geoTweets = new GeoTweets(asyncTwitter);
        geoTweets.setListener(new GeoTweets.GeoTweetsListener() {
            @Override
            public void onGetTweetsError() {
                Log.e("ERROR", "ERROR");
            }

            @Override
            public void onGetTweetsSuccess(final List<Status> statuses) {
                for (Status s: statuses) {
                    Log.d("", s.getText());
                }
                final List<Tweet> tweets = Tweet.mapTweetsFromStatus(statuses);

                updateMapWithTweets(tweets);

                TweetDAO tweetDAO = new TweetDAO(getBaseContext());
                tweetDAO.deleteAll();

                for (Tweet tweet: tweets) {
                    tweetDAO.insert(tweet);
                }
            }
        });

        geoTweets.getTweetsFromLocationAt(geoPoint);


    }

    private void updateMapWithTweets(final List<Tweet> tweets) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MapGeoTweets.addGeoTweetsToMap(tweets, googleMap, getBaseContext());
            }
        });
    }

}

