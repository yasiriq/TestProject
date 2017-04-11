package com.speedautosystems.firebaseproject;


import android.*;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Yasir on 2/27/2017.
 */
@SuppressWarnings("ResourceType")
public class LocationUpdateActivity extends AppCompatActivity implements LocationListener, GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,OnMapReadyCallback
{
    private static final String TAG = "LocationActivity";
    private static final long INTERVAL = 1000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;
    int PLAY_SERVICES_RESOLUTION_REQUEST=11;
    int PERMISSION_REQUEST_CODE=12;
    TextView tvLocation;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation,mUserLocation;
    String mLastUpdateTime;
    private GoogleMap mMap;
    Marker marker;
    MarkerOptions markerOption;
    private DatabaseReference mFirebaseDatabase;
    boolean isTracking;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isTracking=getIntent().getBooleanExtra("IS_TRACKING",false);
        if (!isGooglePlayServicesAvailable()) {

        }
        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        setContentView(R.layout.activity_map);
        tvLocation=(TextView)findViewById(R.id.loc);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        if(RequestPermission())
            NetworkUtil.checkLocationNetwork(this);
        mFirebaseDatabase = FirebaseDatabase.getInstance().getReference("users");

        if(isTracking)
        valueUpdateListener();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if(!isTracking && mCurrentLocation!=null)
        {
            LatLng start=new LatLng(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(start, 16);
            mMap.animateCamera(cameraUpdate);

        }
        mMap.setMyLocationEnabled(true);
       /* mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.setTrafficEnabled(false);*/

    }
    public void valueUpdateListener()
    {
        mFirebaseDatabase.child("jfgvlZJyhyHVg4").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                User user = dataSnapshot.getValue(User.class);
                if(user!=null)
                {
                    if(mUserLocation==null)
                        mUserLocation=new Location("network");
                    mUserLocation.setLatitude(user.getLat());
                    mUserLocation.setLongitude(user.getLon());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateMarker();
                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
              //  Log.w(getClass().getName(), "Failed to read value.", error.toException());
            }
        });
    }

    private void updateMarker()
    {
        if(mMap==null)
            return;
        if(marker!=null && mUserLocation!=null)
        {
               LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;

                if(!bounds.contains(marker.getPosition()))
                {
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 16);
                    mMap.animateCamera(cameraUpdate);

                }

            MarkerUtils.animateMarkerToHC(marker,new LatLng(mUserLocation.getLatitude(),mUserLocation.getLongitude()),new LatLngInterpolator.Spherical());
        }else {
            if(mUserLocation!=null)
            {
                LatLng start=new LatLng(mUserLocation.getLatitude(),mUserLocation.getLongitude());
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(start, 16);
                marker=mMap.addMarker(new MarkerOptions().position(start));
                mMap.animateCamera(cameraUpdate);

            }
        }
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if(result != ConnectionResult.SUCCESS) {
            if(googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }

            return false;
        }

        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart fired ..............");
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop fired ..............");
        if(mGoogleApiClient!=null)
        mGoogleApiClient.disconnect();
        Log.d(TAG, "isConnected ...............: " + mGoogleApiClient.isConnected());
    }

    // Google Api Clients Callbacks

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected - isConnected ...............: " + mGoogleApiClient.isConnected());
           startLocationUpdates();

    }
    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    protected void startLocationUpdates() {

        if(checkLocationPermission()&& NetworkUtil.isLocationNetworkAvailable(this)) {
            PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

        Log.d(TAG, "Location update started ..............: ");
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
           if(mGoogleApiClient.isConnected())
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        Log.d(TAG, "Location update stopped .......................");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
              startLocationUpdates();
            Log.d(TAG, "Location update resumed .....................");
        }
    }

    // Locations Update Callbacks


    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        tvLocation.setText(mLastUpdateTime+" : "+mCurrentLocation.getLatitude()+" ,"+mCurrentLocation.getLongitude());

        if(!isTracking)
        {
             mFirebaseDatabase.child("jfgvlZJyhyHVg4").child("lat").setValue(mCurrentLocation.getLatitude());
            mFirebaseDatabase.child("jfgvlZJyhyHVg4").child("lon").setValue(mCurrentLocation.getLongitude());

        }

    }

    private boolean checkLocationPermission()
    {
        int permissionLocation = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        return (permissionLocation == PackageManager.PERMISSION_GRANTED);
    }

    private boolean RequestPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        int permissionLocation = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (permissionLocation != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            requestPermissions(listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE )
        {
            Map<String, Integer> perms = new HashMap<>();
            perms.put(android.Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);

            if (grantResults.length > 0) {
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);

                if(perms.get(android.Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED )
                {
                    NetworkUtil.checkLocationNetwork(this);
                    if(mGoogleApiClient.isConnected())
                        startLocationUpdates();

                }
            }


        }
    }
}
