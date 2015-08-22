package rayacevedo45.c4q.nyc.accessfoodnyc;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterManager;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

import rayacevedo45.c4q.nyc.accessfoodnyc.api.yelp.models.Business;
import rayacevedo45.c4q.nyc.accessfoodnyc.api.yelp.models.Coordinate;
import rayacevedo45.c4q.nyc.accessfoodnyc.api.yelp.models.YelpResponse;
import rayacevedo45.c4q.nyc.accessfoodnyc.api.yelp.service.ServiceGenerator;
import rayacevedo45.c4q.nyc.accessfoodnyc.api.yelp.service.YelpSearchService;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, View.OnClickListener, GoogleMap.OnCameraChangeListener {

    private static final String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    private static final String LOCATION_KEY = "location-key";
    private static final String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";

    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private Location mCurrentLocation;
    private boolean mRequestingLocationUpdates;
    private String mLastUpdateTime;

    private FloatingActionButton mButtonFilter;

    private RecyclerView mRecyclerView;
    private VendorListAdapter mAdapter;

    private List<ParseObject> mVendorList;
    public static String businessId;

    private static String latLngForSearch;
    private static LatLng lastLatLng;

    public static ParseApplication sApplication;

    // Declare a variable for the cluster manager.
    ClusterManager<MarkerCluster> mClusterManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);

        //Parse.initialize(this);

        buildGoogleApiClient();
        createLocationRequest();

        initializeViews();

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mMap = mapFragment.getMap();



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
            List<Business> businessList = sApplication.sYelpResponse.getBusinesses();

            mAdapter = new VendorListAdapter(getApplicationContext(), businessList);
            mRecyclerView.setAdapter(mAdapter);
            int i = 1;
            for (Business business : businessList) {
                rayacevedo45.c4q.nyc.accessfoodnyc.api.yelp.models.Location location = business.getLocation();
                Coordinate coordinate = location.getCoordinate();

                double latitude = coordinate.getLatitude();

                double longitude = coordinate.getLongitude();
                LatLng position = new LatLng(latitude, longitude);
                // create marker
//                MarkerOptions marker = new MarkerOptions().position(new LatLng(latitude, longitude)).title(business.getName());
                MarkerCluster mc = new MarkerCluster(latitude, longitude, business.getName());
                mClusterManager.addItem(mc);
                // Changing marker icon
//                marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.food_truck));
//                mMap.addMarker(marker);
            }
            generateClusterManager(mClusterManager);

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
        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpListener(true);
        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
    }

    public void setUpListener(boolean isResumed) {
        if (isResumed) {
            mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getApplicationContext(), new RecyclerItemClickListener.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    businessId = mAdapter.getItem(position).getId();
                    Intent intent = new Intent(getApplicationContext(), VendorInfoActivity.class);
                    intent.putExtra(Constants.EXTRA_KEY_VENDOR_OBJECT_ID, businessId);
                    startActivity(intent);
                }
            })
            );

        } else {

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        setUpListener(false);
        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    private void initializeViews() {
        mButtonFilter = (FloatingActionButton) findViewById(R.id.button_filter);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(lm);
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
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
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

        LatLng defaultLatLng = new LatLng(Constants.DEFAULT_LATITUDE, Constants.DEFAULT_LONGITUDE);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(defaultLatLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        lastLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(lastLatLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(13));

        YelpSearchService yelpService = ServiceGenerator.createYelpSearchService();
        yelpService.searchFoodCarts(String.valueOf(lastLatLng), new YelpSearchCallback());

        setUpClusterer();
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
    public void onClick(View v) {

    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

    }

    private void getQuery() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Vendor");
    }

    private void cleanUpMarkers(Set<String> markersToKeep) {
//        for (String objId : new HashSet<String>(mapMarkers.keySet())) {
//            if (!markersToKeep.contains(objId)) {
//                Marker marker = mapMarkers.get(objId);
//                marker.remove();
//                mapMarkers.get(objId).remove();
//                mapMarkers.remove(objId);
//            }
//        }

    }



}

