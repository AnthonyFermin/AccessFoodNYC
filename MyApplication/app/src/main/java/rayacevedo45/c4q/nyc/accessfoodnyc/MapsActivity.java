package rayacevedo45.c4q.nyc.accessfoodnyc;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.lsjwzh.widget.recyclerviewpager.RecyclerViewPager;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import rayacevedo45.c4q.nyc.accessfoodnyc.accounts.LoginActivity;
import rayacevedo45.c4q.nyc.accessfoodnyc.api.yelp.models.Business;
import rayacevedo45.c4q.nyc.accessfoodnyc.api.yelp.models.Coordinate;
import rayacevedo45.c4q.nyc.accessfoodnyc.api.yelp.models.YelpResponse;
import rayacevedo45.c4q.nyc.accessfoodnyc.api.yelp.service.ServiceGenerator;
import rayacevedo45.c4q.nyc.accessfoodnyc.api.yelp.service.YelpSearchService;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks,
        GoogleMap.OnCameraChangeListener, SortDialogListener, TouchableWrapper.UpdateMapAfterUserInterection {

    private static final String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    private static final String LOCATION_KEY = "location-key";
    private static final String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";

    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;
    private LocationRequest mLocationRequest;
    public static Location mLastLocation;
    private Location mCurrentLocation;
    private boolean mRequestingLocationUpdates;
    private String mLastUpdateTime;

    private CollapsingToolbarLayout mToolbarLayout;
    private FloatingActionButton mButtonFilter;

    private RecyclerViewPager mRecyclerView;
    private VendorListAdapter mAdapter;

    private List<ParseObject> mVendorList;
    private List<Business> mYelpList;
    public static String businessId;

    private static String latLngForSearch = "40.740949, -73.932157";
    private static LatLng lastLatLng;

    public static ParseApplication sApplication;

    // Declare a variable for the cluster manager.
    ClusterManager<MarkerCluster> mClusterManager;

    private Toolbar mToolbar;

    private RecyclerView mRecyclerViewList;
    private boolean isListed = false;
    private boolean isFetched;

    public HashMap<Marker, Integer> markerHashMap;
    private Integer previous;
    private boolean isMarkerClicked;
    private Button mButtonSearchThisArea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("MapsActivity", "it creates!!!!!!!!!");
        setContentView(R.layout.activity_maps);
        isListed = false;
        isFetched = false;
        mToolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        markerHashMap = new HashMap<>();

        isMarkerClicked = false;

        buildGoogleApiClient();
        createLocationRequest();

        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mMap = mapFragment.getMap();

        initializeViews();


        //setSupportActionBar(mToolbar);
//        mToolbar.setTitle("Maps");
//        //mToolbar.inflateMenu(R.menu.menu_map);
//        setSupportActionBar(mToolbar);

        // this is enable to back button arrow icon
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mGoogleApiClient.connect();
    }

    private void initializeViews() {
        mRecyclerView = (RecyclerViewPager) findViewById(R.id.recyclerView_grid);
        mRecyclerViewList = (RecyclerView) findViewById(R.id.recyclerView_list);
        mButtonSearchThisArea = (Button) findViewById(R.id.search_this_area);
        //mRecyclerView.setHasFixedSize(true);
        //mRecyclerViewList.setHasFixedSize(true);

        LinearLayoutManager layout = new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.HORIZONTAL,false);
        //GridLayoutManager gm = new GridLayoutManager(getApplicationContext(), 1, GridLayoutManager.HORIZONTAL, false);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setOrientation(LinearLayoutManager.VERTICAL);

        mRecyclerView.setLayoutManager(layout);

        mRecyclerViewList.setLayoutManager(lm);

//        mAdapter = new VendorListAdapter(getApplicationContext());
//        mRecyclerView.setAdapter(mAdapter);
//        mRecyclerViewList.setAdapter(mAdapter);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int scrollState) {
                // do something
                int childCount = mRecyclerView.getCurrentPosition();
                int position = mRecyclerView.getCurrentPosition();
                switch (scrollState) {
                    case RecyclerView.SCROLL_STATE_IDLE:

                        if (!isMarkerClicked) {
                            mMap.animateCamera(CameraUpdateFactory.newLatLng(mAdapter.getItem(position).getMarker().getPosition()));
                            if (previous != null) {
                                mAdapter.getItem(previous).getMarker().setIcon(BitmapDescriptorFactory.fromResource(R.drawable.food_truck_red));
                            }
                            mAdapter.getItem(position).getMarker().setIcon(BitmapDescriptorFactory.fromResource(R.drawable.food_blue));
                            previous = position;
                        }


                        //mMap.animateCamera(CameraUpdateFactory.zoomTo(13));

                        break;

                }


            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int i, int i2) {
                int childCount = mRecyclerView.getChildCount();
                int width = mRecyclerView.getChildAt(0).getWidth();
                int padding = (mRecyclerView.getWidth() - width) / 2;
                //mCountText.setText("Count: " + childCount);


                for (int j = 0; j < childCount; j++) {
                    View v = recyclerView.getChildAt(j);
                    float rate = 0;
                    if (v.getLeft() <= padding) {
                        if (v.getLeft() >= padding - v.getWidth()) {
                            rate = (padding - v.getLeft()) * 1f / v.getWidth();
                        } else {
                            rate = 1;
                        }
                        v.setScaleY(1 - rate * 0.1f);
                    } else {
                        if (v.getLeft() <= recyclerView.getWidth() - padding) {
                            rate = (recyclerView.getWidth() - padding - v.getLeft()) * 1f / v.getWidth();
                        }
                        v.setScaleY(0.9f + rate * 0.1f);
                    }
                }
            }
        });
        // registering addOnLayoutChangeListener  aim to setScale at first layout action
        mRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (mRecyclerView.getChildCount() < 3) {
                    if (mRecyclerView.getChildAt(1) != null) {
                        View v1 = mRecyclerView.getChildAt(1);
                        v1.setScaleY(0.9f);
                    }
                } else {
                    if (mRecyclerView.getChildAt(0) != null) {
                        View v0 = mRecyclerView.getChildAt(0);
                        v0.setScaleY(0.9f);
                    }
                    if (mRecyclerView.getChildAt(2) != null) {
                        View v2 = mRecyclerView.getChildAt(2);
                        v2.setScaleY(0.9f);
                    }
                }

            }
        });



        totalX = 0;
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

//                totalX += dx;
//                mRecyclerView.getChildPosition(recyclerView.)
            }
        });

    }
    private void setUpClusterer() {

        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        mClusterManager = new ClusterManager<MarkerCluster>(getApplicationContext(), mMap);
        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        mMap.setOnCameraChangeListener(mClusterManager);


        mClusterManager.setRenderer(new ClusterRendring(getApplicationContext(), mMap, mClusterManager));

        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MarkerCluster>() {
            @Override
            public boolean onClusterItemClick(MarkerCluster markerCluster) {

                String a = markerCluster.getTitle();
                Toast.makeText(getApplicationContext(), a, Toast.LENGTH_SHORT).show();
                return false;
            }

        });


    }

    protected class YelpSearchCallback implements Callback<YelpResponse> {

        public String TAG = "YelpSearchCallback";

        @Override
        public void success(YelpResponse data, Response response) {
            Log.d(TAG, "Success");
            sApplication = ParseApplication.getInstance();
            sApplication.sYelpResponse = data;
            List<Business> yelpRawList = sApplication.sYelpResponse.getBusinesses();

            final ParseUser user = ParseUser.getCurrentUser();
            for (final Business business : yelpRawList) {
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Vendor");
                query.whereEqualTo("yelpId", business.getId());
                query.getFirstInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(final ParseObject vendor, ParseException e) {
                        if (vendor == null || vendor.getString("name") == null) {

                            rayacevedo45.c4q.nyc.accessfoodnyc.api.yelp.models.Location location = business.getLocation();
                            Coordinate coordinate = location.getCoordinate();
                            final double latitude = coordinate.getLatitude();
                            final double longitude = coordinate.getLongitude();
                            // create marker
//                MarkerOptions marker = new MarkerOptions().position(new LatLng(latitude, longitude)).title(business.getName());
                            final Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title(business.getName())); //...
//                MarkerCluster mc = new MarkerCluster(latitude, longitude, business.getName(),business.getId());
//                mClusterManager.addItem(mc);
                            // Changing marker icon
                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.food_truck_red));
                            //markerHashMap.put(marker, business.getId());
                            final List<String> address = DetailsFragment.addressGenerator(business);

                            if (vendor == null) {
                                Vendor truck = new Vendor.Builder(business.getId()).isYelp(true).isLiked(false)
                                        .setName(business.getName()).setFriends(new ArrayList<ParseObject>())
                                        .setMarker(marker).setLocation(new ParseGeoPoint(latitude, longitude))
                                        .setRating(business.getRating()).setPicture(business.getImageUrl())
                                        .setAddress(address.get(0)).build();
                                mAdapter.addVendor(truck);
                            } else {
                                ParseRelation<ParseUser> friends = user.getRelation("friends");
                                friends.getQuery().findInBackground(new FindCallback<ParseUser>() {
                                    @Override
                                    public void done(List<ParseUser> list, ParseException e) {

                                        ParseQuery<ParseObject> favorites = ParseQuery.getQuery("Favorite");
                                        favorites.include("follower");
                                        favorites.whereEqualTo("vendor", vendor).whereContainedIn("follower", list);
                                        favorites.findInBackground(new FindCallback<ParseObject>() {
                                            @Override
                                            public void done(List<ParseObject> list, ParseException e) {
                                                Vendor truck = new Vendor.Builder(business.getId()).isYelp(true).isLiked(false)
                                                        .setName(business.getName()).setFriends(new ArrayList<ParseObject>())
                                                        .setMarker(marker).setLocation(new ParseGeoPoint(latitude, longitude))
                                                        .setRating(business.getRating()).setPicture(business.getImageUrl())
                                                        .setAddress(address.get(0)).build();
                                                mAdapter.addVendor(truck);
                                            }
                                        });

                                    }
                                });
                            }
                        }
                    }
                });

            }

            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {

                    isMarkerClicked = true;
                    int position = mAdapter.getPosition(marker);
                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.food_blue));
                    marker.showInfoWindow();
                    if (previous != null) {
                        mAdapter.getItem(previous).getMarker().setIcon(BitmapDescriptorFactory.fromResource(R.drawable.food_truck_red));
                    }
                    previous = position;
                    mRecyclerView.smoothScrollToPosition(position);
                    isMarkerClicked = false;
                    //marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.food_blue));

                    //previous = position;

                    //mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                    return true;
                }
            });

            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
//                    Intent intent = new Intent(getApplicationContext(), VendorInfoActivity.class);
//                    String objectId;
//                    Object item = mAdapter.getObject(marker);
//                    if (item instanceof Business) {
//                        Business business = (Business) item;
//                        objectId = business.getId();
//                        intent.putExtra(Constants.EXTRA_KEY_IS_YELP, true);
//                    } else {
//                        ParseObject vendor = (ParseObject) item;
//                        objectId = vendor.getObjectId();
//                        intent.putExtra(Constants.EXTRA_KEY_IS_YELP, false);
//
//                    }
//                    intent.putExtra(Constants.EXTRA_KEY_OBJECT_ID, objectId);
//                    startActivity(intent);
                }
            });


        }

        @Override
        public void failure(RetrofitError error) {
            Log.e(TAG, error.getMessage());
        }

    }

    protected ClusterManager<MarkerCluster> generateClusterManager(ClusterManager<MarkerCluster> mClusterManager){
        return mClusterManager;
    }

        @Override
    protected void onStart() {
        super.onStart();
            Log.i("MapsActivity", "It starts!!!!!!!!!!");
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpListener(true);
        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
        Log.i("MapsActivity", "it resumes!!!!!!!");

    }

    public void goToVendorInfoAcvitity(int position) {
        Intent intent = new Intent(getApplicationContext(), VendorInfoActivity.class);
        Vendor vendor = mAdapter.getItem(position);
        intent.putExtra(Constants.EXTRA_KEY_OBJECT_ID, vendor.getId());
        if (vendor.isYelp()) {
            intent.putExtra(Constants.EXTRA_KEY_IS_YELP, true);
        } else {
            intent.putExtra(Constants.EXTRA_KEY_IS_YELP, false);
        }
        startActivity(intent);
    }

    public void setUpListener(boolean isResumed) {
        if (isResumed) {
            mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getApplicationContext(), new RecyclerItemClickListener.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                   goToVendorInfoAcvitity(position);
                }
            })
            );
            mRecyclerViewList.addOnItemTouchListener(new RecyclerItemClickListener(getApplicationContext(), new RecyclerItemClickListener.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            goToVendorInfoAcvitity(position);
                        }
                    })
            );
            mButtonSearchThisArea.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    searchVendors(mMap.getCameraPosition().target);
                    mButtonSearchThisArea.setVisibility(View.GONE);
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        setUpListener(false);
        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
        Log.i("MapsActivity", "it pauses!!!!!!!");
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
        Log.i("MapsActivity", "it stops!!!!!!!");
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.action_profile:
                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                startActivity(intent);
                break;
            case R.id.action_logout:
                logOut();
                break;
            case R.id.action_settings:
                break;
            case R.id.action_list:
                if (isListed) {
                    mRecyclerViewList.setVisibility(View.GONE);
                    mRecyclerView.setVisibility(View.VISIBLE);
                    isListed = false;
                } else {
                    mRecyclerViewList.setVisibility(View.VISIBLE);
                    mRecyclerView.setVisibility(View.GONE);
                    isListed = true;
                }
                break;
            case R.id.action_sort:
                sort();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        googleMap.setMyLocationEnabled(true);
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.setOnCameraChangeListener(this);

    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i("MapsActivity", "Connected to Map!!!!!!!!");
        LatLng defaultLatLng = new LatLng(Constants.DEFAULT_LATITUDE, Constants.DEFAULT_LONGITUDE);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(defaultLatLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
        ParseUser user = ParseUser.getCurrentUser();
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        final ParseGeoPoint point = new ParseGeoPoint(mLastLocation.getLatitude(), mLastLocation.getLongitude());

        user.put("location", point);
        user.saveInBackground();

        mAdapter = new VendorListAdapter(getApplicationContext(), point);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerViewList.setAdapter(mAdapter);
        searchVendors(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
    }


    public void searchVendors(LatLng currentLocation) {

        final ParseGeoPoint point = new ParseGeoPoint(currentLocation.latitude, currentLocation.longitude);
        final ParseUser user = ParseUser.getCurrentUser();

        mAdapter = new VendorListAdapter(getApplicationContext(), point);
        mRecyclerView.swapAdapter(mAdapter, true);
        mRecyclerViewList.swapAdapter(mAdapter, true);


        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        final String today = "day" + Integer.toString(day);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Vendor");
        query.whereNear("location", point).setLimit(50).findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                for (final ParseObject vendor : list) {
                    if (vendor.getString("name") != null) {

                        final ParseGeoPoint vendorLocation = vendor.getParseGeoPoint("location");
                        LatLng position = new LatLng(vendorLocation.getLatitude(), vendorLocation.getLongitude());
                        final Marker marker = mMap.addMarker(new MarkerOptions().position(position).title(vendor.getString("name")));
                        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.food_truck_red));
                        //markerHashMap.put(marker, vendor.getObjectId());

                        final String json = vendor.getString(today);

                        ParseRelation<ParseUser> friends = user.getRelation("friends");
                        friends.getQuery().findInBackground(new FindCallback<ParseUser>() {
                            @Override
                            public void done(List<ParseUser> friends, ParseException e) {

                                ParseQuery<ParseObject> favorites = ParseQuery.getQuery("Favorite");
                                favorites.include("follower");
                                favorites.whereEqualTo("vendor", vendor).whereContainedIn("follower", friends);
                                favorites.findInBackground(new FindCallback<ParseObject>() {
                                    @Override
                                    public void done(final List<ParseObject> list, ParseException e) {

                                        ParseQuery<ParseObject> fav = ParseQuery.getQuery("Favorite");
                                        fav.whereEqualTo("follower", user).whereEqualTo("vendor", vendor);
                                        fav.getFirstInBackground(new GetCallback<ParseObject>() {
                                            @Override
                                            public void done(ParseObject parseObject, ParseException e) {
                                                Vendor truck;
                                                if (parseObject == null) {
                                                    truck = new Vendor.Builder(vendor.getObjectId())
                                                            .setName(vendor.getString("name")).setAddress(vendor.getString("address"))
                                                            .isYelp(false).setCategory(vendor.getString("category"))
                                                            .setFriends(list).setLocation(vendorLocation).setHours(json).setMarker(marker)
                                                            .setPicture(vendor.getString("profile_url")).setRating(vendor.getDouble("rating"))
                                                            .isLiked(false).build();
                                                } else {
                                                    truck = new Vendor.Builder(vendor.getObjectId())
                                                            .setName(vendor.getString("name")).setAddress(vendor.getString("address"))
                                                            .isYelp(false).setCategory(vendor.getString("category"))
                                                            .setFriends(list).setLocation(vendorLocation).setHours(json).setMarker(marker)
                                                            .setPicture(vendor.getString("profile_url")).setRating(vendor.getDouble("rating"))
                                                            .isLiked(true).build();
                                                }
                                                mAdapter.addVendor(truck);
                                            }
                                        });

                                    }
                                });

                            }
                        });
                    }


                    ParseQuery<ParseObject> reviewQuery = ParseQuery.getQuery("Review");
                    reviewQuery.whereEqualTo("vendor", vendor).findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> list, ParseException e) {
                            if (list.size() == 0) {

                            } else {

                                double ratingsum = 0;
                                for (final ParseObject review : list) {
                                    ratingsum += (Integer) review.get("rating");
                                }
//                                Toast.makeText(getActivity(), String.valueOf(ratingsum), Toast.LENGTH_SHORT).show();
                                double averageRating = Math.round((ratingsum / (list.size())) * 10.0) / 10.0;

                                //Toast.makeText(getApplicationContext(), String.valueOf(averageRating), Toast.LENGTH_SHORT).show();
                                vendor.put("rating", averageRating);
                                vendor.saveInBackground();
                            }
                        }
                    });

                }

            }
        });

        Geocoder geocoder;
        List<Address> addresses = null;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(currentLocation.latitude, currentLocation.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String postalCode = addresses.get(0).getPostalCode();

            YelpSearchService yelpService = ServiceGenerator.createYelpSearchService();
//        yelpService.searchFoodCarts(String.valueOf(lastLatLng), new YelpSearchCallback());
            yelpService.searchFoodCarts(address + " " + postalCode, new YelpSearchCallback());
//        yelpService.searchFoodCarts("3100 47th Ave 11101", new YelpSearchCallback());

        } catch (IOException e) {
            e.printStackTrace();
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(13));
    }

    protected void startLocationUpdates() {
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mCurrentLocation = location;
                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            }
        };
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, locationListener);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(15000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        outState.putParcelable(LOCATION_KEY, mCurrentLocation);
        outState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

    }

    private void logOut() {
        LoginManager.getInstance().logOut();
        ParseUser.logOut();
        Toast.makeText(getApplicationContext(), "Successfully logged out!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void sort() {
        new SortDialogFragment().show(getSupportFragmentManager(), "");
    }

    @Override
    public void sortOptionSelected(int which) {
        switch (which) {
            case 0:
                mAdapter.sortByDistance();
                break;
            case 1:
                mAdapter.sortByRating();
                break;
        }
    }

    @Override
    public void onUpdateMapAfterUserInterection() {
        mButtonSearchThisArea.setVisibility(View.VISIBLE);
    }
}