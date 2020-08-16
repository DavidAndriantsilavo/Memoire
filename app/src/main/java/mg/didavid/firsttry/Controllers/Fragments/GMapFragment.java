package mg.didavid.firsttry.Controllers.Fragments;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;

import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.GeoPoint;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import mg.didavid.firsttry.Controllers.Activities.LoginActivity;
import mg.didavid.firsttry.Controllers.Activities.NewPostActivity;
import mg.didavid.firsttry.Controllers.Activities.ProfileUserActivity;
import mg.didavid.firsttry.Models.LocationService;
import mg.didavid.firsttry.Models.User;
import mg.didavid.firsttry.Models.UserLocation;
import mg.didavid.firsttry.Models.UserSingleton;
import mg.didavid.firsttry.R;

import static android.content.Context.ACTIVITY_SERVICE;
import static androidx.constraintlayout.widget.Constraints.TAG;


public class GMapFragment extends Fragment implements OnMapReadyCallback{

    //private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int PERMISSIONS_REQUEST_ENABLE_GPS = 8001;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 8002;
    private static final int ERROR_DIALOG_REQUEST = 8003;

    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    private Location lastKnownLocation;
    private boolean mLocationPermissionGranted = false;
    private boolean mServicesIsGood = false;

    private AlertDialog alert = null;
    private ArrayList<Marker> otherMarkerList = new ArrayList<Marker>();

    private FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mUserLocationReference = mFirebaseDatabase.getReference().child("userLocation");

    private Handler mHandler = new Handler();
    private Runnable mOtherRunnable, mUserRunnable;
    private static final int LOCATION_UPDATE_INTERVAL = 3000;

    User user = new User();

    ProgressDialog progressDialog_logout;

    public static GMapFragment newInstance() {
        return (new GMapFragment());
    }
    MapView mMapView;
    private GoogleMap mGoogleMap;
    private CameraPosition cameraPosition;
    private Circle distanceCircle;

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient fusedLocationProviderClient;
    private UserLocation mUserLocation;

    private View mCustomDefaultMarkerView, mCustomClickedMarkerView;
    private ImageView imageView_marker, imageView_profile_picture;
    private TextView textView_name;
    private Button button_profile, button_directions, button_message;
    private Marker lastClickedMarker, userMarker;
    private SeekBar seekBar_distance;
    private int seekBarProgress, distanceRadius, currentDistance;

    private Bitmap lastBitmap = null;
    private Bitmap currentBitmap = null;

    private HashMap<String, Bitmap> markerBitmap = new HashMap<String, Bitmap>();


    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_map, container, false);

        mCustomDefaultMarkerView = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.marker_default_layout, null);
        mCustomClickedMarkerView = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.marker_clicked_layout, null);

        imageView_marker = mCustomDefaultMarkerView.findViewById(R.id.imageView_marker);

        Log.d(TAG, "FT : OnCreateView!!");

        //init progressDialog
        progressDialog_logout = new ProgressDialog(getContext());
        progressDialog_logout.setMessage("Déconnexion...");

        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
        user = ((UserSingleton) getActivity().getApplicationContext()).getUser();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        mMapView = (MapView) v.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this); //this is important

        return v;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        Log.d(TAG, "FT : Map Ready!!");

        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
        mGoogleMap.getUiSettings().setZoomGesturesEnabled(true);
        mGoogleMap.getUiSettings().setCompassEnabled(true);
        mGoogleMap.getUiSettings().setMapToolbarEnabled(false);

        getOtherPosition();

        seekBar_distance = getView().findViewById(R.id.seekBar_distance);
        seekBar_distance.setProgress(seekBarProgress);

        final View linearLayoutCustomView = getView().findViewById(R.id.linearLayoutCustomView);
        textView_name = linearLayoutCustomView.findViewById(R.id.textView_name);
        imageView_profile_picture = linearLayoutCustomView.findViewById(R.id.imageView_profile_picture);
        button_profile = linearLayoutCustomView.findViewById(R.id.button_profile);
        button_directions = linearLayoutCustomView.findViewById(R.id.button_directions);
        button_message = linearLayoutCustomView.findViewById(R.id.button_message);

        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                Log.d("GoogleMap", " click");

                if(marker.getTag() != null) {
                    button_directions.setVisibility(View.VISIBLE);
                    button_message.setVisibility(View.VISIBLE);

                    UserLocation clickedUser = (UserLocation) marker.getTag();
                    textView_name.setText(clickedUser.getName());
                    Picasso.get().load(clickedUser.getProfile_image()).resize(100, 100).transform(new CropCircleTransformation()).into(imageView_profile_picture);

                    Picasso.get().load(clickedUser.getProfile_image()).resize(100, 100).transform(new CropCircleTransformation()).into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//                            currentBitmap = bitmap.copy(bitmap.getConfig(), true);
//
//                            Canvas canvasBmp2 = new Canvas( currentBitmap );
//                            canvasBmp2.drawBitmap(bitmap, 0, 0, null);

                            if (marker.equals(lastClickedMarker)) {
                                if (linearLayoutCustomView.getVisibility() == View.VISIBLE) {
                                    linearLayoutCustomView.setVisibility(View.GONE);
                                    marker.setIcon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(mCustomDefaultMarkerView, bitmap)));
                                } else {
                                    linearLayoutCustomView.setVisibility(View.VISIBLE);
                                    marker.setIcon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(mCustomClickedMarkerView, bitmap)));
                                }

                            } else {
                                if (lastClickedMarker != null && lastBitmap != null) {
                                    //UserLocation lastClickedUser = (UserLocation) lastClickedMarker.getTag();
                                    lastClickedMarker.setIcon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(mCustomDefaultMarkerView, lastBitmap)));
                                }

                                lastClickedMarker = marker;
                                lastBitmap = bitmap;

                                linearLayoutCustomView.setVisibility(View.VISIBLE);
                                marker.setIcon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(mCustomClickedMarkerView, bitmap)));
                            }
                        }

                        @Override
                        public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                        }
                    });
                }

                return false;
            }
        });

        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener()
        {
            @Override
            public void onMapClick(LatLng arg0)
            {
                Log.d(TAG, "test : map clicked");
                if (linearLayoutCustomView.getVisibility() == View.VISIBLE){
                    linearLayoutCustomView.setVisibility(View.GONE);
                }
            }
        });

        if(mServicesIsGood) {
            updateLocationUI();
            checkSeekBarDistance();
        }
    }

    // [START maps_current_place_update_location_ui]
    private void updateLocationUI() {
        if (mGoogleMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mGoogleMap.setMyLocationEnabled(true);
                mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
                Log.d(TAG, "FT : UI Updated!!");

                setUserLocation();
            } else {
                mGoogleMap.setMyLocationEnabled(false);
                mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
                Log.d(TAG, "FT : UI not updated");
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }
    // [END maps_current_place_update_location_ui]

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //START REALTIME SERVICES - UPDATE USER'S LOCATION TO FIREBASE AND RETRIEVING OTHERS' LOCATIONS
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //START THE SERVICE TO SEND THE CURRENT LOCATION TO REALTIME DATABASE
    private void startLocationService(){
        if(!isLocationServiceRunning()){
            Intent serviceIntent = new Intent(getActivity(), LocationService.class);
//        this.startService(serviceIntent);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){

                getActivity().startForegroundService(serviceIntent);
            }else{
                getActivity().startService(serviceIntent);
            }
        }
    }

    //CHECK IF THE REALTIME SERVICE IS RUNNING
    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if("mg.didavid.firsttry.Models.LocationService".equals(service.service.getClassName())) {
                Log.d(TAG, "isLocationServiceRunning: location service is already running.");
                return true;
            }
        }
        Log.d(TAG, "isLocationServiceRunning: location service is not running.");
        return false;
    }


    //START THE SERVICE TO RETRIEVE OTHER'S LOCATION
    private void startOtherUserLocationsRunnable(){
        Log.d(TAG, "startOtherUserLocationsRunnable: starting runnable for retrieving updated locations.");
        mHandler.postDelayed(mOtherRunnable = new Runnable() {
            @Override
            public void run() {
                updateOtherLocation();
                mHandler.postDelayed(mOtherRunnable, LOCATION_UPDATE_INTERVAL);
            }
        }, LOCATION_UPDATE_INTERVAL);
    }

    //STOP THE SERVICE
    private void stopOtherLocationUpdates(){
        mHandler.removeCallbacks(mOtherRunnable);
    }

    //START THE SERVICE TO RETRIEVE OTHER'S LOCATION
    private void startUserLocationsRunnable(final Marker marker){
        Log.d(TAG, "startOtherUserLocationsRunnable: starting runnable for retrieving updated locations.");
        mHandler.postDelayed(mUserRunnable = new Runnable() {
            @Override
            public void run() {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                if (task.isSuccessful()) {
                                    // Set the map's camera position to the current location of the device.
                                    lastKnownLocation = task.getResult();

                                    LatLng userPosition = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                                    distanceCircle.setCenter(userPosition);
                                    marker.setPosition(userPosition);
                                }
                            }
                        });
                                    mHandler.postDelayed(mOtherRunnable, LOCATION_UPDATE_INTERVAL);
            }
        }, LOCATION_UPDATE_INTERVAL);
    }

    //STOP THE SERVICE
    private void stopUserLocationUpdates(){
        mHandler.removeCallbacks(mUserRunnable);
    }

    //CHECK THE SEEKBAR CHANGE STATE
    // IF CHECKED THEN START THE USERLOCATIONRUNNABLE AND SHOW OTHERS ON THE MAP WITH MARKERS
    //IF NOT STOP THE RUNNABLE AND CLEAR THE MARKERS AND THE LIST

    private void getOtherPosition(){
        mUserLocationReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final int[] i = {0};

                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    //Getting User object from dataSnapshot
                    final UserLocation userLocation = data.getValue(UserLocation.class);

                    if(userLocation != null){
                        final LatLng otherPosition = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
                        final Marker[] marker = new Marker[1];
                        String url = userLocation.getProfile_image();

                        if(!userLocation.getUser_id().equals(user.getUser_id())){
                            Log.d(TAG, " test : onDataChange: " + userLocation.getName());

                            Picasso.get().load(url).resize(100, 100).transform(new CropCircleTransformation()).into(new Target() {
                                @Override
                                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

                                    marker[0] = mGoogleMap.addMarker(new MarkerOptions()
                                            .position(otherPosition)
                                            .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(mCustomDefaultMarkerView, bitmap))));
                                    marker[0].setTag(userLocation);

                                    marker[0].setVisible(false);

                                    Log.d(TAG, "test : add marker " + i[0] +1);
                                    otherMarkerList.add(i[0], marker[0]);

                                    markerBitmap.put(userLocation.getUser_id(), bitmap);
                                    i[0]++;
                                }

                                @Override
                                public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                                }

                                @Override
                                public void onPrepareLoad(Drawable placeHolderDrawable) {
                                }
                            });
                        }
                    }
                }
                Log.d(TAG, " test : All position get");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "%s" + error);
            }
        });
    }

    private void checkSeekBarDistance(){
        seekBar_distance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                distanceRadius = seekBarProgress*100;

                seekBarProgress = progress;
                distanceCircle.setRadius(distanceRadius);

                mGoogleMap.getCameraPosition().s

                for(int i = 0; i < otherMarkerList.size(); i++){
                    Marker marker = otherMarkerList.get(i);

                    Location location = new Location("otherLocation");
                    location.setLatitude(marker.getPosition().latitude);
                    location.setLongitude(marker.getPosition().longitude);

                    currentDistance = Math.round(lastKnownLocation.distanceTo(location));

                    if(currentDistance < distanceRadius){
                        marker.setVisible(true);
                    }else {
                        marker.setVisible(false);
                    }
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(getActivity(), "Seek bar progress is :" + seekBarProgress,
                        Toast.LENGTH_SHORT).show();

                if(seekBarProgress == 0){
                    stopOtherLocationUpdates();
                }else {
                    if(!otherMarkerList.isEmpty()){
                        startOtherUserLocationsRunnable();
                    }

                }
            }
        });
    }

    //UPDATE OTHERS' LOCATION IF THE RUNNABLE IS RUNNING
    private void updateOtherLocation() {
        mUserLocationReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int i=0;
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    //Getting User object from dataSnapshot
                    UserLocation userLocation = data.getValue(UserLocation.class);
                    LatLng otherPosition = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
                    Marker marker;
                    try {
                        marker = otherMarkerList.get(i);
                        marker.setPosition(otherPosition);
                        otherMarkerList.set(i, marker);
                        i++;

                        Log.d(TAG, " test : other position updated");
                    }catch (Exception e){
                        Log.e(TAG, "%s" + e);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "%s" + error);
            }
        });
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //END REALTIME SERVICES - UPDATE USER'S LOCATION TO FIREBASE AND RETRIEVING OTHERS' LOCATIONS
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //START RETRIEVE USER LOCATION AND MOVE THE CAMERA - SAVE USER INFO TO FIRESTORE
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //GET THE USER INFO FROM THE SINGLETON AND INSTANCIATE THE USERLOCATION OBJECT
    private void setUserLocation(){
        if(mUserLocation == null)
        {
            mUserLocation = new UserLocation();
            if(((UserSingleton) getActivity().getApplicationContext()).getUser() != null)
            {
                mUserLocation.setName(((UserSingleton) getActivity().getApplicationContext()).getUser().getName());
                mUserLocation.setUser_id(((UserSingleton) getActivity().getApplicationContext()).getUser().getUser_id());
                mUserLocation.setProfile_image(((UserSingleton) getActivity().getApplicationContext()).getUser().getProfile_image());

                getDeviceLocation();
            }
        }
    }

    // GET THE USER DEVICE LOCATION AND ANIMATE THE CAMERA TO THAT LOCATION
    private void getDeviceLocation() {
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.getResult();

                            if (lastKnownLocation != null) {

                                GeoPoint geoPoint = new GeoPoint(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(lastKnownLocation.getLatitude(),
                                                lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                                Log.d(TAG, "FT : Location updated!!");

                                String url = user.getProfile_image();
                                final LatLng myPosition = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());

                                Picasso.get().load(url).resize(100, 100).transform(new CropCircleTransformation()).into(new Target() {
                                    @Override
                                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                        // Todo: Do something with your bitmap here

                                        userMarker = mGoogleMap.addMarker(new MarkerOptions()
                                                .position(myPosition)
                                                .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(mCustomDefaultMarkerView, bitmap))));
                                        userMarker.setTag(mUserLocation);

                                        Log.d(TAG, "FT : add marker");

                                        markerBitmap.put(user.getUser_id(), bitmap);

                                        startUserLocationsRunnable(userMarker);
                                    }

                                    @Override
                                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                                    }

                                    @Override
                                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                                    }
                                });


                                mUserLocation.setLatitude(geoPoint.getLatitude());
                                mUserLocation.setLongitude(geoPoint.getLongitude());

                                distanceCircle = mGoogleMap.addCircle(new CircleOptions()
                                        .center(new LatLng(lastKnownLocation.getLatitude(),
                                                lastKnownLocation.getLongitude()))
                                        .radius(seekBarProgress)
                                        .strokeWidth(1)
                                        .strokeColor(Color.GREEN)
                                        .fillColor(Color.argb(128, 255, 0, 0))
                                        .clickable(false));

                                TimeZone timezone = TimeZone.getTimeZone("GMT+03:00"); //MADAGASCAR TIMEZONE
                                Calendar c = Calendar.getInstance(timezone);
                                String time = String.format("%02d", c.get(Calendar.YEAR)) + "-" +
                                        String.format("%02d", c.get(Calendar.MONTH)) + "-" +
                                        String.format("%02d", c.get(Calendar.DAY_OF_MONTH)) + " " +
                                        String.format("%02d", c.get(Calendar.HOUR_OF_DAY)) + ":" +
                                        String.format("%02d", c.get(Calendar.MINUTE)) + ":" +
                                        String.format("%02d", c.get(Calendar.SECOND));
                                mUserLocation.setTimestamp(time);

                                saveUserLocation();
                                startLocationService();
                            }
                        } else {
                            Log.d(TAG, "FT : Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mGoogleMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    //SAVE THE USER LOCATION INTO REALTIME DATABASE
    private void saveUserLocation()
    {
        if(mUserLocation != null)
        {
            mUserLocationReference.child(mUserLocation.getUser_id()).setValue(mUserLocation).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        //Task was successful, data written!
                        Log.d(TAG, "FT : userLocation updated");
                    }else{
                        //Task was not successful,
                        Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_SHORT).show();

                        //Log the error message
                        Log.e(TAG, "onComplete: ERROR: " + task.getException().getLocalizedMessage() );
                    }
                }
            });
        }
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //END RETRIEVE USER LOCATION AND MOVE THE CAMERA - SAVE USER INFO TO FIRESTORE
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //START SERVICES AND PERMISSION REQUESTS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //CHECK FOR ALL NEEDED SERVICES
    private boolean checkMapServices(){
        mServicesIsGood = false;
        Log.d(TAG, "FT : allIsGood NOT OK");
        if(isServicesOK()){
            if(isGpsEnabled()){
                mServicesIsGood = true;
                Log.d(TAG, "FT : allIsGood OK");
                return mServicesIsGood;
            }
        }
        return false;
    }

    //REQUEST FOR ENABLING GPS
    private void buildAlertMessageNoGps() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        alert = builder.create();
        alert.show();

        Log.d(TAG, "FT : GPS alert message!!");
    }

    //CHECK IF GPS IS ENABLED
    public boolean isGpsEnabled(){
        final LocationManager manager = (LocationManager) getActivity().getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            Log.d(TAG, "FT : GPS not enabled!");
            buildAlertMessageNoGps();

            return false;
        }
        else
        {
            Log.d(TAG, "FT : GPS enabled");
        }
        return true;
    }

    // REQUEST FOR GPLAY SERVICES
    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getActivity());

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "FT : isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "FT : isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(getActivity(), "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    //REQUEST FOR LOCATION PERMISSION
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    //RESULT OF PERMISSION REQUEST
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    updateLocationUI();
                }
            }
        }
    }

    //RESULT OF GPS INTENT
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: called.");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS: {
                if(mLocationPermissionGranted) {
                    Log.d(TAG, "FT : GPS request result");
                }
                else{
                    getLocationPermission();
                }
            }
        }

    }

    ///////////////////////////////////////////////////////////////////
    // END SERVICES AND PERMISSION REQUESTS
    ////////////////////////////////////////////////////////////////

    //GET THE BITMAP FROM PICASSO AND PUT IT TO THE INFLATED VIEW FOR THE MARKER
    private Bitmap getMarkerBitmapFromView(View view, Bitmap bitmap) {

        imageView_marker.setImageBitmap(bitmap);
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = view.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        view.draw(canvas);
        return returnedBitmap;
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();

        Log.d(TAG, "FT : OnResume!!");

        if(checkMapServices()) {
            if (mLocationPermissionGranted) {
                updateLocationUI();
            }
            else {
                getLocationPermission();
            }
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        Log.d(TAG, "FT : OnPause");

        if(alert != null)
        {
            alert.dismiss();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mMapView.onDestroy();
        stopOtherLocationUpdates();
        Log.d(TAG, "FT : OnDestroyView");

        otherMarkerList.clear();
        stopUserLocationUpdates();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mGoogleMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mGoogleMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, lastKnownLocation);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_activity_main, menu);
        menu.findItem(R.id.menu_search_button).setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //3 - Handle actions on menu items
        switch (item.getItemId()) {
            case R.id.menu_logout_profil:
                avertissement();
                return true;
            case R.id.menu_activity_main_profile:
                startActivity(new Intent(getContext(), ProfileUserActivity.class));
                return true;
            case R.id.menu_activity_main_addNewPost:
                startActivity(new Intent(getContext(), NewPostActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void avertissement() {
        if(user!=null)
        {
            //BUILD ALERT DIALOG TO CONFIRM THE SUPPRESSION
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage("Vous voulez vous déconnecter?");
            builder.setCancelable(true);

            builder.setPositiveButton(
                    "OUI",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            progressDialog_logout.show();
                            logOut();
                        }
                    });

            builder.setNegativeButton(
                    "NON",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            progressDialog_logout.dismiss();

            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private void logOut() {
        progressDialog_logout.show();
        FirebaseAuth.getInstance().signOut();
        GoogleSignIn.getClient(
                getContext(),
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        ).signOut();

        Intent logOut =  new Intent(getContext(), LoginActivity.class);
        startActivity(logOut);

        getActivity().finish();
    }
}