package com.odobo.twlocator.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.odobo.twlocator.R;
import com.odobo.twlocatorapi.util.geolocation.GeoPoint;
import com.odobo.twlocatorapi.util.geolocation.GeoPoints;
import com.odobo.twlocatorapi.util.geolocation.GeoTweets;
import com.odobo.twlocatorapi.util.geolocation.MapGeoTweets;
import com.odobo.twlocatorapi.util.twitter.ConnectTwitterTask;
import com.odobo.twlocatorapi.util.twitter.Globals;

import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import twitter4j.AsyncTwitter;
import twitter4j.Status;

public class MainActivity extends ActionBarActivity implements ConnectTwitterTask.OnConnectTwitterListener {

    GoogleMap googleMap;
    ConnectTwitterTask twitterTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        getSupportActionBar().setDisplayShowHomeEnabled(true);

        twitterTask = new ConnectTwitterTask(this);
        twitterTask.setListener(this);

        twitterTask.execute();

        this.initializeMap();
        this.handleIntent(getIntent());

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
            }
        } catch(Exception e) {
            e.printStackTrace();
        }


    }

    public void getTweetsFromLocationAt(GeoPoint geoPoint) {

        if (geoPoint == null) {
            return;
        }

        CameraPosition cameraPosition = new CameraPosition.Builder().target(
                new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude())).zoom(12).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        AsyncTwitter asyncTwitter = Globals.getSharedTwitterHelper(this).getAsyncTwitter();

        GeoTweets geoTweets = new GeoTweets(asyncTwitter);
        geoTweets.setListener(new GeoTweets.GeoTweetsListener() {
            @Override
            public void onGetTweetsError() {
                Log.e("ERROR", "ERROR");

            }

            @Override
            public void onGetTweetsSuccess(final List<Status> tweets) {
                for (Status s: tweets) {
                    Log.d("", s.getText());
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MapGeoTweets.addGeoTweetsToMap(tweets, googleMap, getBaseContext());
                    }
                });
            }
        });

        geoTweets.getTweetsFromLocationAt(geoPoint);


    }


}

