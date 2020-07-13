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
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;

import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;


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

    public static GMapFragment newInstance() {
        return (new GMapFragment());
    }
        MapView mMapView;
        private GoogleMap mGoogleMap;
        private CameraPosition cameraPosition;

        // The entry point to the Fused Location Provider.
        private FusedLocationProviderClient fusedLocationProviderClient;

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

            if(mServicesIsGood)
            {
                updateLocationUI();
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

                    getDeviceLocation();
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

    // [START maps_current_place_get_device_location]
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
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
                                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(lastKnownLocation.getLatitude(),
                                                lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                                Log.d(TAG, "FT : Location updated!!");
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
    // [END maps_current_place_get_device_location]

        ///////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////
    // REQUEST GPS AND ACCESS PERMISSIONS
    ////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////

    //CHECK FOR ALL NEEDED SERVICES
        private boolean checkMapServices(){
            mServicesIsGood = false;
            Log.d(TAG, "FT : allIsGood NOT OK");;
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



