package com.speedautosystems.firebaseproject;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
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
 * Created by Yasir on 3/9/2017.
 */
@SuppressWarnings("ResourceType")
public class PlacePickerActivity extends AppCompatActivity implements LocationListener, GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,OnMapReadyCallback
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
    TextView markerText;
    View mMapView,touchHelperView;
    LatLng latLng;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (!isGooglePlayServicesAvailable()) {

        }
        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        setContentView(R.layout.activity_place_picker);
        tvLocation=(TextView)findViewById(R.id.loc);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        if(RequestPermission())
            NetworkUtil.checkLocationNetwork(this);
        mFirebaseDatabase = FirebaseDatabase.getInstance().getReference("users");

        findViewById(R.id.navigate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri gmmIntentUri = Uri.parse("google.navigation:q="+latLng.latitude+","+latLng.longitude);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });


      /*  findViewById(R.id.locationMarker).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
        //        mMap.animateCamera(CameraUpdateFactory.zoomIn());
            }
        });*/
        markerText=(TextView)findViewById(R.id.locationMarkertext);
        touchHelperView=findViewById(R.id.helperView);


        touchHelperView.setOnTouchListener(new View.OnTouchListener() {
            private float scaleFactor = 1f;
            @Override
            public boolean onTouch(View v, MotionEvent motionEvent) {
                if (simpleGestureDetector.onTouchEvent(motionEvent)) { // Double tap
                    mMap.animateCamera(CameraUpdateFactory.zoomIn()); // Fixed zoom in
                    Log.e("Double Tap","++++++++");
                } else if (motionEvent.getPointerCount() == 1) { // Single tap
                    Log.e("Single Tap","-------");
                    if(mMapView!=null)
                    mMapView.dispatchTouchEvent(motionEvent); // Propagate the event to the map (Pan)
                    if(!mMap.getUiSettings().isScrollGesturesEnabled())
                        mMap.getUiSettings().setScrollGesturesEnabled(true);
                } else if (scaleGestureDetector.onTouchEvent(motionEvent)) { // Pinch zoom
                    mMap.getUiSettings().setScrollGesturesEnabled(false);
                    mMap.moveCamera(CameraUpdateFactory.zoomBy( // Zoom the map without panning it
                            (mMap.getCameraPosition().zoom * scaleFactor
                                    - mMap.getCameraPosition().zoom) / 5));
                    Log.e("Pinch",""+scaleFactor);
                }else {Log.e("Tap","==="+motionEvent.getPointerCount());}
                Log.e("Tap","==="+motionEvent.getPointerCount());
                return true; // Consume all the gestures
            }

            // Gesture detector to manage double tap gestures
            private GestureDetector simpleGestureDetector = new GestureDetector(
                    PlacePickerActivity.this, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    return true;
                }
            });

            // Gesture detector to manage scale gestures
            private ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(
                    PlacePickerActivity.this, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                @Override
                public boolean onScale(ScaleGestureDetector detector) {
                    scaleFactor = detector.getScaleFactor();
                    return true;
                }
            });
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMapView=getSupportFragmentManager().findFragmentById(R.id.map).getView();

        if( mCurrentLocation!=null)
        {
            LatLng start=new LatLng(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(start, 16);
            mMap.animateCamera(cameraUpdate);

        }
        mMap.setMyLocationEnabled(true);
        //mMap.getUiSettings().setZoomGesturesEnabled(false);
       // mMap.getUiSettings().setScrollGesturesEnabled(false);
       /* mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.setTrafficEnabled(false);*/
        mMap.getUiSettings().setMapToolbarEnabled(true);

        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                 latLng=mMap.getCameraPosition().target;
                if(latLng.latitude!=0 || latLng.longitude!=0)
                markerText.setText(latLng.latitude+" , "+latLng.latitude);
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


        if( mCurrentLocation==null)
        {
            mCurrentLocation = location;
            LatLng start=new LatLng(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(start, 16);
            mMap.animateCamera(cameraUpdate);

        }

        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
      //  tvLocation.setText(mLastUpdateTime+" : "+mCurrentLocation.getLatitude()+" ,"+mCurrentLocation.getLongitude());


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

