package com.odobo.twlocatorapi.util.geolocation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import twitter4j.AsyncTwitter;
import twitter4j.GeoLocation;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterListener;

public class GeoTweets {

    AsyncTwitter asyncTwitter;

    public GeoTweets(AsyncTwitter asyncTwitter) {
        this.asyncTwitter = asyncTwitter;
    }

    public interface GeoTweetsListener {
        public void onGetTweetsError();
        public void onGetTweetsSuccess(List<Status> tweets);
    }

    private Set<GeoTweetsListener> listeners = new HashSet<GeoTweetsListener>();

    public void setListener(GeoTweetsListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(GeoTweetsListener listener) {
        this.listeners.remove(listener);
    }

    private void notifySuccessToListeners(List<Status> tweets) {
        for (GeoTweetsListener listener: this.listeners) {
            listener.onGetTweetsSuccess(tweets);
        }
    }

    private void notifyErrorToListeners() {
        for (GeoTweetsListener listener: this.listeners) {
            listener.onGetTweetsError();
        }
    }

    public void getTweetsFromLocationAt(GeoPoint geoPoint) {
        if (this.asyncTwitter == null) {
            notifyErrorToListeners();
            return;
        }

        TwitterListener listener = new TwitterAdapter() {
            @Override
            public void searched(final QueryResult queryResult) {
                super.searched(queryResult);

                notifySuccessToListeners(queryResult.getTweets());
            }

            @Override
            public void onException(TwitterException te, twitter4j.TwitterMethod method) {
                notifyErrorToListeners();
                te.printStackTrace();
            };
        };

        this.asyncTwitter.addListener(listener);
        Query qry = new Query(geoPoint.getName());
        GeoLocation location = new GeoLocation(geoPoint.getLatitude(), geoPoint.getLongitude());
        qry.geoCode(location, 10, Query.KILOMETERS.name());
        qry.setCount(20);
        asyncTwitter.search(qry);
    }

}
