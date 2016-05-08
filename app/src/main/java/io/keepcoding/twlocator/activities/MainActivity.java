package io.keepcoding.twlocator.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.keepcoding.twlocator.R;
import io.keepcoding.twlocator.dialog.TweetDialog;
import io.keepcoding.twlocator.model.DBHelper;
import io.keepcoding.twlocator.model.Search;
import io.keepcoding.twlocator.model.SearchSchema;
import io.keepcoding.twlocator.model.Tweet;
import io.keepcoding.twlocator.model.TweetInfo;
import io.keepcoding.twlocator.model.TweetInfoSchema;
import io.keepcoding.twlocator.model.TweetSchema;
import io.keepcoding.twlocator.util.NetworkHelper;
import io.keepcoding.twlocator.util.twitter.ConnectTwitterTask;
import io.keepcoding.twlocator.util.twitter.TwitterHelper;
import twitter4j.AccountSettings;
import twitter4j.AsyncTwitter;
import twitter4j.Category;
import twitter4j.DirectMessage;
import twitter4j.Friendship;
import twitter4j.GeoLocation;
import twitter4j.IDs;
import twitter4j.Location;
import twitter4j.OEmbed;
import twitter4j.PagableResponseList;
import twitter4j.Place;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.RateLimitStatus;
import twitter4j.Relationship;
import twitter4j.ResponseList;
import twitter4j.SavedSearch;
import twitter4j.Status;
import twitter4j.Trends;
import twitter4j.Twitter;
import twitter4j.TwitterAPIConfiguration;
import twitter4j.TwitterException;
import twitter4j.TwitterListener;
import twitter4j.TwitterMethod;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.api.HelpResources;
import twitter4j.auth.AccessToken;
import twitter4j.auth.OAuth2Token;
import twitter4j.auth.RequestToken;


public class  MainActivity extends ActionBarActivity implements ConnectTwitterTask.OnConnectTwitterListener {

    ConnectTwitterTask twitterTask;
    private static final int URL_LOADER = 0;

    MapFragment mMapFragment;
    GoogleMap mGoogleMap;
    MarkerOptions mMarkerOptions;

    private MenuItem mSearchAction;
    private MenuItem mLastSearchAction;
    private MenuItem mCurrentSearchAction;
    private boolean isSearchOpened = false;
    private EditText edtSearch;
    private long mIdLastSearch;

    @Bind(R.id.button)
    Button button;

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

            //se obtiene el fragmento que va a contener el mapa
            mMapFragment = (MapFragment)getFragmentManager().findFragmentById(R.id.map);

            if(mMapFragment != null){
                mGoogleMap = mMapFragment.getMap();

                if(mGoogleMap == null){
                    Toast.makeText(this, "can't get the map", Toast.LENGTH_SHORT).show();
                }else{

                    SearchSchema searchSchema = new SearchSchema(this);
                    mIdLastSearch = searchSchema.getIdLastSearch();

                    mGoogleMap.setMyLocationEnabled(true);
                    mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);

                    mGoogleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                        @Override
                        public boolean onMyLocationButtonClick() {
                            getTweetsByAddress(mGoogleMap.getMyLocation().getLatitude(),
                                    mGoogleMap.getMyLocation().getLongitude(),
                                    getString(R.string.my_location));
                            centerMap(mGoogleMap, mGoogleMap.getMyLocation().getLatitude(),
                                    mGoogleMap.getMyLocation().getLongitude(), 12);
                            return true;
                        }
                    });

                    mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker) {

                            Bundle args = new Bundle();
                            args.putString("tweetId", marker.getSnippet());
                            TweetDialog dialog = new TweetDialog();
                            dialog.setArguments(args);

                            dialog.show(MainActivity.this.getSupportFragmentManager(), "Dialog Fragment");

                            return true;
                        }
                    });
                }

            }

        } else {
            Toast.makeText(this, getString(R.string.error_network), Toast.LENGTH_LONG).show();

        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchTwitter();
            }
        });
    }

    private void launchTwitter() {
        AsyncTwitter twitter = new TwitterHelper(this).getAsyncTwitter();
        twitter.addListener(new TwitterListener() {
            @Override
            public void gotMentions(ResponseList<Status> statuses) {

            }

            @Override
            public void gotHomeTimeline(ResponseList<Status> statuses) {

            }

            @Override
            public void gotUserTimeline(ResponseList<Status> statuses) {
                for (Status s : statuses) {
                    Log.d("Twitter", "tweet: " + s.getText());
                }
            }

            @Override
            public void gotRetweetsOfMe(ResponseList<Status> statuses) {

            }

            @Override
            public void gotRetweets(ResponseList<Status> retweets) {

            }

            @Override
            public void gotShowStatus(Status status) {

            }

            @Override
            public void destroyedStatus(Status destroyedStatus) {

            }

            @Override
            public void updatedStatus(Status status) {

            }

            @Override
            public void retweetedStatus(Status retweetedStatus) {

            }

            @Override
            public void gotOEmbed(OEmbed oembed) {

            }

            @Override
            public void lookedup(ResponseList<Status> statuses) {

            }

            @Override
            public void searched(QueryResult queryResult) {

            }

            @Override
            public void gotDirectMessages(ResponseList<DirectMessage> messages) {

            }

            @Override
            public void gotSentDirectMessages(ResponseList<DirectMessage> messages) {

            }

            @Override
            public void gotDirectMessage(DirectMessage message) {

            }

            @Override
            public void destroyedDirectMessage(DirectMessage message) {

            }

            @Override
            public void sentDirectMessage(DirectMessage message) {

            }

            @Override
            public void gotFriendsIDs(IDs ids) {

            }

            @Override
            public void gotFollowersIDs(IDs ids) {

            }

            @Override
            public void lookedUpFriendships(ResponseList<Friendship> friendships) {

            }

            @Override
            public void gotIncomingFriendships(IDs ids) {

            }

            @Override
            public void gotOutgoingFriendships(IDs ids) {

            }

            @Override
            public void createdFriendship(User user) {

            }

            @Override
            public void destroyedFriendship(User user) {

            }

            @Override
            public void updatedFriendship(Relationship relationship) {

            }

            @Override
            public void gotShowFriendship(Relationship relationship) {

            }

            @Override
            public void gotFriendsList(PagableResponseList<User> users) {

            }

            @Override
            public void gotFollowersList(PagableResponseList<User> users) {

            }

            @Override
            public void gotAccountSettings(AccountSettings settings) {

            }

            @Override
            public void verifiedCredentials(User user) {

            }

            @Override
            public void updatedAccountSettings(AccountSettings settings) {

            }

            @Override
            public void updatedProfile(User user) {

            }

            @Override
            public void updatedProfileBackgroundImage(User user) {

            }

            @Override
            public void updatedProfileColors(User user) {

            }

            @Override
            public void updatedProfileImage(User user) {

            }

            @Override
            public void gotBlocksList(ResponseList<User> blockingUsers) {

            }

            @Override
            public void gotBlockIDs(IDs blockingUsersIDs) {

            }

            @Override
            public void createdBlock(User user) {

            }

            @Override
            public void destroyedBlock(User user) {

            }

            @Override
            public void lookedupUsers(ResponseList<User> users) {

            }

            @Override
            public void gotUserDetail(User user) {

            }

            @Override
            public void searchedUser(ResponseList<User> userList) {

            }

            @Override
            public void gotContributees(ResponseList<User> users) {

            }

            @Override
            public void gotContributors(ResponseList<User> users) {

            }

            @Override
            public void removedProfileBanner() {

            }

            @Override
            public void updatedProfileBanner() {

            }

            @Override
            public void gotMutesList(ResponseList<User> blockingUsers) {

            }

            @Override
            public void gotMuteIDs(IDs blockingUsersIDs) {

            }

            @Override
            public void createdMute(User user) {

            }

            @Override
            public void destroyedMute(User user) {

            }

            @Override
            public void gotUserSuggestions(ResponseList<User> users) {

            }

            @Override
            public void gotSuggestedUserCategories(ResponseList<Category> category) {

            }

            @Override
            public void gotMemberSuggestions(ResponseList<User> users) {

            }

            @Override
            public void gotFavorites(ResponseList<Status> statuses) {

            }

            @Override
            public void createdFavorite(Status status) {

            }

            @Override
            public void destroyedFavorite(Status status) {

            }

            @Override
            public void gotUserLists(ResponseList<UserList> userLists) {

            }

            @Override
            public void gotUserListStatuses(ResponseList<Status> statuses) {

            }

            @Override
            public void destroyedUserListMember(UserList userList) {

            }

            @Override
            public void gotUserListMemberships(PagableResponseList<UserList> userLists) {

            }

            @Override
            public void gotUserListSubscribers(PagableResponseList<User> users) {

            }

            @Override
            public void subscribedUserList(UserList userList) {

            }

            @Override
            public void checkedUserListSubscription(User user) {

            }

            @Override
            public void unsubscribedUserList(UserList userList) {

            }

            @Override
            public void createdUserListMembers(UserList userList) {

            }

            @Override
            public void checkedUserListMembership(User users) {

            }

            @Override
            public void createdUserListMember(UserList userList) {

            }

            @Override
            public void destroyedUserList(UserList userList) {

            }

            @Override
            public void updatedUserList(UserList userList) {

            }

            @Override
            public void createdUserList(UserList userList) {

            }

            @Override
            public void gotShowUserList(UserList userList) {

            }

            @Override
            public void gotUserListSubscriptions(PagableResponseList<UserList> userLists) {

            }

            @Override
            public void gotUserListMembers(PagableResponseList<User> users) {

            }

            @Override
            public void gotSavedSearches(ResponseList<SavedSearch> savedSearches) {

            }

            @Override
            public void gotSavedSearch(SavedSearch savedSearch) {

            }

            @Override
            public void createdSavedSearch(SavedSearch savedSearch) {

            }

            @Override
            public void destroyedSavedSearch(SavedSearch savedSearch) {

            }

            @Override
            public void gotGeoDetails(Place place) {

            }

            @Override
            public void gotReverseGeoCode(ResponseList<Place> places) {

            }

            @Override
            public void searchedPlaces(ResponseList<Place> places) {

            }

            @Override
            public void gotSimilarPlaces(ResponseList<Place> places) {

            }

            @Override
            public void gotPlaceTrends(Trends trends) {

            }

            @Override
            public void gotAvailableTrends(ResponseList<Location> locations) {

            }

            @Override
            public void gotClosestTrends(ResponseList<Location> locations) {

            }

            @Override
            public void reportedSpam(User reportedSpammer) {

            }

            @Override
            public void gotOAuthRequestToken(RequestToken token) {

            }

            @Override
            public void gotOAuthAccessToken(AccessToken token) {

            }

            @Override
            public void gotOAuth2Token(OAuth2Token token) {

            }

            @Override
            public void gotAPIConfiguration(TwitterAPIConfiguration conf) {

            }

            @Override
            public void gotLanguages(ResponseList<HelpResources.Language> languages) {

            }

            @Override
            public void gotPrivacyPolicy(String privacyPolicy) {

            }

            @Override
            public void gotTermsOfService(String tof) {

            }

            @Override
            public void gotRateLimitStatus(Map<String, RateLimitStatus> rateLimitStatus) {

            }

            @Override
            public void onException(TwitterException te, TwitterMethod method) {

            }
        });
        twitter.getUserTimeline();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mSearchAction = menu.findItem(R.id.action_search);
        mLastSearchAction = menu.findItem(R.id.action_last_search);
        mCurrentSearchAction = menu.findItem(R.id.action_current_search);
        mCurrentSearchAction.setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        SearchSchema searchSchema;
        Search search;

        switch (id) {
            case R.id.action_search:
                handleMenuSearch();
                return true;
            case R.id.action_last_search:
                searchSchema = new SearchSchema(this);

                if(mIdLastSearch == DBHelper.INVALID_ID) {
                    Toast.makeText(this, R.string.not_last_search, Toast.LENGTH_SHORT).show();
                    return true;
                }

                search = searchSchema.query(mIdLastSearch);
                getTweetsByAddressOfLastSearch(search);
                centerMap(mGoogleMap, search.getLatitude(), search.getLongitude(), 10);

                mCurrentSearchAction.setVisible(true);
                mLastSearchAction.setVisible(false);
                mSearchAction.setVisible(false);
                return true;
            case R.id.action_current_search:

                searchSchema = new SearchSchema(this);
                search = searchSchema.query(searchSchema.getIdLastSearch());
                getTweetsByAddressOfLastSearch(search);
                centerMap(mGoogleMap, search.getLatitude(), search.getLongitude(), 10);

                mCurrentSearchAction.setVisible(false);
                mLastSearchAction.setVisible(true);
                mSearchAction.setVisible(true);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(isSearchOpened) {
            handleMenuSearch();
            return;
        }
        super.onBackPressed();
    }

    protected void handleMenuSearch(){
        ActionBar action = getSupportActionBar();

        if(isSearchOpened){

            action.setDisplayShowCustomEnabled(false);
            action.setDisplayShowTitleEnabled(true);

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(edtSearch.getWindowToken(), 0);

            mSearchAction.setIcon(ContextCompat.getDrawable(this, android.R.drawable.ic_menu_search));

            mLastSearchAction.setVisible(true);

            isSearchOpened = false;
        } else {

            action.setDisplayShowCustomEnabled(true);
            action.setCustomView(R.layout.search_bar);
            action.setDisplayShowTitleEnabled(false);

            edtSearch = (EditText)action.getCustomView().findViewById(R.id.search_view);

            edtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        doSearch();
                        return true;
                    }
                    return false;
                }
            });


            edtSearch.requestFocus();

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(edtSearch, InputMethodManager.SHOW_IMPLICIT);

            mSearchAction.setIcon(
                    ContextCompat.getDrawable(this, android.R.drawable.ic_menu_close_clear_cancel));

            mLastSearchAction.setVisible(false);

            isSearchOpened = true;
        }
    }

    private void doSearch() {
        try{
            Geocoder geocoder = new Geocoder(this);
            List<Address> addressList = geocoder.getFromLocationName(edtSearch.getText().toString(), 1);
            if(addressList.size() > 0){
                Address address = addressList.get(0);
                getTweetsByAddress(address.getLatitude(), address.getLongitude(), edtSearch.getText().toString());
                centerMap(mGoogleMap, address.getLatitude(), address.getLongitude(), 10);

                SearchSchema searchSchema = new SearchSchema(this);

                View view = this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }else{
                Toast.makeText(this, R.string.invalid_search_address, Toast.LENGTH_SHORT).show();
            }
        }catch(Exception ex){
            Log.d(getString(R.string.app_name), "ERROR: " + ex.getMessage());
        }
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

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        final Uri uri = intent.getData();
        if (uri != null && uri.toString().indexOf(TwitterHelper.TwitterConsts.CALLBACK_URL) != -1) {
            Log.d(getString(R.string.app_name), "Retrieving Access Token. Callback received : " + uri);
            twitterTask = new ConnectTwitterTask(this);
            twitterTask.setListener(this);

            twitterTask.execute();
        }
    }


    public void centerMap(final GoogleMap map, final double latitude, final double longitude, final int zoomLevel){

        (new Handler(Looper.getMainLooper())).post(new Runnable() {
            @Override
            public void run() {
                LatLng coordinate = new LatLng(latitude, longitude);
                CameraPosition cameraPosition = new CameraPosition.Builder().target(coordinate).zoom(zoomLevel).build();
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });

    }


    public void clearMap(final GoogleMap map){

        (new Handler(Looper.getMainLooper())).post(new Runnable() {
            @Override
            public void run() {
                map.clear();
            }
        });

    }


    public void getTweetsByAddress(final double latitude, final double longitude, final String txtAddress){
        Thread thread = new Thread() {
            @Override
            public void run() {
                try{

                    long id;
                    final TweetSchema tweetSchema = new TweetSchema(MainActivity.this);
                    final TweetInfoSchema tweetInfoSchema = new TweetInfoSchema(MainActivity.this);
                    final SearchSchema searchSchema = new SearchSchema(MainActivity.this);

                    Twitter twitter = new TwitterHelper(MainActivity.this).getTwitter();
                    Query query = new Query();
                    query.setGeoCode(new GeoLocation(latitude, longitude), 10, Query.Unit.km);
                    query.setCount(100);
                    QueryResult queryResult = twitter.search(query);

                    List<Status> tweets = queryResult.getTweets();

                    if(tweets.size() == 0){

                        Toast.makeText(MainActivity.this, R.string.no_tweets_in_address, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    clearMap(mGoogleMap);

                    Search search = new Search(latitude, longitude, txtAddress);

                    long idLastSearch = searchSchema.getIdLastSearch();
                    mIdLastSearch = idLastSearch;
                    id = searchSchema.insert(search);
                    search.setId(id);

                    if(idLastSearch != DBHelper.INVALID_ID && idLastSearch != id){
                        searchSchema.delete(id, idLastSearch);
                    }

                    for (final Status s : tweets) {
                        if (s.getGeoLocation() != null) {

                            Tweet tweet = new Tweet(
                                    s.getUser().getName(),
                                    s.getUser().getBiggerProfileImageURL(),
                                    s.getText(),
                                    search,
                                    //latitude,
                                    //longitude);
                                    s.getGeoLocation().getLatitude(),
                                    s.getGeoLocation().getLongitude());
                            id = tweetSchema.insert(tweet);
                            tweet.setId(id);

                            for(URLEntity urlEntity: s.getURLEntities()){
                                TweetInfo tweetInfo;
                                tweetInfo = new TweetInfo(
                                        urlEntity.getExpandedURL(),
                                        new WeakReference<>(tweet));
                                tweetInfoSchema.insert(tweetInfo);
                            }
                            loadImageProfileOnMap(tweet);
                        }
                    }



                }catch(Exception e){
                    Log.e(getString(R.string.app_name), e.getMessage());
                }
            }
        };

        thread.start();
    }


    public void getTweetsByAddressOfLastSearch(final Search search){
        Thread thread = new Thread() {
            @Override
            public void run() {
                try{

                    final TweetSchema tweetSchema = new TweetSchema(MainActivity.this);

                    List<Tweet> tweets = tweetSchema.query(search);

                    if(tweets.size() == 0){

                        Toast.makeText(MainActivity.this, R.string.no_tweets_in_address, Toast.LENGTH_SHORT).show();
                        return;

                    }

                    clearMap(mGoogleMap);

                    for (final Tweet tweet : tweets) {
                        if (tweet != null) {
                            loadImageProfileOnMap(tweet);
                        }
                    }

                }catch(Exception e){
                    Log.e(getString(R.string.app_name), e.getMessage());
                }
            }
        };

        thread.start();
    }


    public void loadImageProfileOnMap(final Tweet tweet){
        new Thread(new Runnable() {
            @Override
            public void run() {

                BitmapDescriptor bitmapDescriptor = null;


                try {

                    URL bitmapURL = new URL(tweet.getURLUserPhotoProfile());
                    Bitmap bmp = BitmapFactory.decodeStream(bitmapURL.openConnection().getInputStream());

                    bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bmp);

                } catch (IOException e) {
                    e.printStackTrace();
                }

                mMarkerOptions = new MarkerOptions()
                        .position(new LatLng(tweet.getLatitude(),
                                tweet.getLongitude()))
                        .title(tweet.getText())
                        .snippet(String.valueOf(tweet.getId()))
                        .icon(bitmapDescriptor);

                (new Handler(Looper.getMainLooper())).post(new Runnable() {
                    @Override
                    public void run() {
                        mGoogleMap.addMarker(mMarkerOptions);

                    }
                });


            }
        }).start();

    }

}

