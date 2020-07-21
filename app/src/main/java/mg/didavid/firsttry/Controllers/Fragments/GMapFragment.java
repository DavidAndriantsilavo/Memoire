package mg.didavid.firsttry.Controllers.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.GeoPoint;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import mg.didavid.firsttry.Models.User;
import mg.didavid.firsttry.Models.UserLocation;
import mg.didavid.firsttry.Models.UserSingleton;
import mg.didavid.firsttry.R;

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
    private ArrayList<Marker> otherMarker = new ArrayList<Marker>();

    private FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mUserLocationReference = mFirebaseDatabase.getReference().child("userLocation");

    User user = new User();

    public static GMapFragment newInstance() {
        return (new GMapFragment());
    }
        MapView mMapView;
        private GoogleMap mGoogleMap;
        private CameraPosition cameraPosition;

        private Switch mShowOthers;

        // The entry point to the Fused Location Provider.
        private FusedLocationProviderClient fusedLocationProviderClient;
        private UserLocation mUserLocation;

        @Override
        public void onDetach() {
            super.onDetach();
        }

        @Override
        public void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_map, container, false);

            Log.d(TAG, "FT : OnCreateView!!");

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

            mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
            mGoogleMap.getUiSettings().setZoomGesturesEnabled(true);
            mGoogleMap.getUiSettings().setCompassEnabled(true);

            Log.d(TAG, "FT : Map Ready!!");

            if(mServicesIsGood) {
                updateLocationUI();
                checkSwitchShowOthers();
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

        //CHECK THE SWITCH CHANGE STATE
        private void checkSwitchShowOthers(){
            mShowOthers = getView().findViewById(R.id.switch_show_other);

            mShowOthers.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    getOtherLocation(isChecked);
                }
            });
        }

        //CHECK TEH SWITCH STATE
        //IF CHECKED THEN PUT THE OTHER'S POSITION INTO A LIST AND SHOW THEM
        //IF NOT DELETE THE MARKERS ANS CLEAR THE LIST
        private void getOtherLocation(Boolean isChecked) {
            Log.d(TAG, "FT : getOtherLocation()");
            if(isChecked) {
                if(otherMarker.isEmpty())
                {
                    mUserLocationReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot data : dataSnapshot.getChildren()) {
                                //Getting User object from dataSnapshot
                                UserLocation userLocation = data.getValue(UserLocation.class);

                                LatLng otherPosition = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
                                Marker marker;
                                if(userLocation.getUser_id() != user.getUser_id() ){
                                    Log.d(TAG, " FT : onDataChange: " + userLocation.getDisplay_name());
                                    marker = mGoogleMap.addMarker(new MarkerOptions()
                                            .position(otherPosition)
                                            .title(userLocation.getDisplay_name()));

                                    otherMarker.add(marker);
                                }
                            }
                            Log.d(TAG, " FT : All position get");
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "%s" + error);
                        }
                    });
                }

            }else{
                if(!otherMarker.isEmpty())
                {
                    for(Marker marker : otherMarker){
                        marker.remove();
                    }
                    otherMarker.clear();
                    Log.d(TAG, " FT : Clear the position list");
                }

            }
        }

        //GET THE USER INFO FROM THE SINGLETON AND INSTANCIATE THE USERLOCATION OBJECT
        private void setUserLocation(){
            if(mUserLocation == null)
            {
                mUserLocation = new UserLocation();
                if(((UserSingleton) getActivity().getApplicationContext()).getUser() != null)
                {
                    mUserLocation.setDisplay_name(((UserSingleton) getActivity().getApplicationContext()).getUser().getName());
                    mUserLocation.setUser_id(((UserSingleton) getActivity().getApplicationContext()).getUser().getUser_id());

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
                                GeoPoint geoPoint = new GeoPoint(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());

                                if (lastKnownLocation != null) {
                                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                            new LatLng(lastKnownLocation.getLatitude(),
                                                    lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                                    Log.d(TAG, "FT : Location updated!!");
                                }

                                mUserLocation.setLatitude(geoPoint.getLatitude());
                                mUserLocation.setLongitude(geoPoint.getLongitude());

                                TimeZone timezone = TimeZone.getTimeZone("GMT+03:00"); //MADAGASCAR TIMEZONE
                                Calendar c = Calendar.getInstance(timezone);
                                String time = String.format("%02d", c.get(Calendar.YEAR)) + "-" +
                                        String.format("%02d", c.get(Calendar.MONTH)) + "-" +
                                        String.format("%02d", c.get(Calendar.DAY_OF_MONTH)) + " " +
                                        String.format("%02d" , c.get(Calendar.HOUR_OF_DAY))+":"+
                                        String.format("%02d" , c.get(Calendar.MINUTE))+":"+
                                        String.format("%02d" , c.get(Calendar.SECOND));
                                mUserLocation.setTimestamp(time);

                                saveUserLocation();
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

        // [END maps_current_place_get_device_location]

            ///////////////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////
        // REQUEST GPS AND ACCESS PERMISSIONS
        ////////////////////////////////////////////////////////////////
        /////////////////////////////////////////////////////////////////

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
        ///////////////////////////////////////////////////////////////////
        // END REQUEST GPS AND ACCESS PERMISSIONS
        ////////////////////////////////////////////////////////////////
        /////////////////////////////////////////////////////////////////

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
            Log.d(TAG, "FT : OnDestroyView");

            otherMarker.clear();
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
}



