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
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.algo.GridBasedAlgorithm;
import com.google.maps.android.collections.MarkerManager;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import mg.didavid.firsttry.Controllers.Activities.ChatActivity;
import mg.didavid.firsttry.Controllers.Activities.LoginActivity;
import mg.didavid.firsttry.Controllers.Activities.NewPostActivity;
import mg.didavid.firsttry.Controllers.Activities.OtherRestoProfileActivity;
import mg.didavid.firsttry.Controllers.Activities.OtherUsersProfileActivity;
import mg.didavid.firsttry.Controllers.Activities.ProfileRestoActivity;
import mg.didavid.firsttry.Controllers.Activities.ProfileUserActivity;
import mg.didavid.firsttry.Controllers.Adapteurs.AdapterMapSearch;
import mg.didavid.firsttry.Models.ClusterMarkerRestaurant;
import mg.didavid.firsttry.Models.ClusterMarkerUser;
import mg.didavid.firsttry.Models.LocationService;
import mg.didavid.firsttry.Models.ModelResto;
import mg.didavid.firsttry.Models.User;
import mg.didavid.firsttry.Models.UserLocation;
import mg.didavid.firsttry.Models.UserSingleton;
import mg.didavid.firsttry.R;
import mg.didavid.firsttry.Utils.ClusterManagerRendererRestaurant;
import mg.didavid.firsttry.Utils.ClusterManagerRendererUser;

import static android.content.Context.ACTIVITY_SERVICE;
import static androidx.constraintlayout.widget.Constraints.TAG;


public class GMapFragment extends Fragment implements OnMapReadyCallback, AdapterMapSearch.OnMapSearchListner {

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
//    private ArrayList<Marker> otherMarkerList = new ArrayList<Marker>();
    private ArrayList<MarkerOptions> userMarkerOptionsList = new ArrayList<MarkerOptions>();

    private FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mUserLocationReference = mFirebaseDatabase.getReference().child("userLocation");

    private CollectionReference restoReference = FirebaseFirestore.getInstance().collection("Resto");

    private Handler otherHandler = new Handler();
    private Handler userHandler = new Handler();

    private Runnable mOtherRunnable, mUserRunnable;
    private static final int LOCATION_UPDATE_INTERVAL = 3000;

    User currentUser = new User();

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
    private ImageView imageView_marker, imageView_profile_picture, imageView_logoResto;
    private TextView textView_name, textView_restoName, textView_restoSpeciality, textView_restoRating, textView_restoRatingCount ;
    private Button button_profile, button_direction, button_message;
    private ClusterMarkerUser userMarker;
    private SeekBar seekBar_distance;
    private int seekBarProgress, distanceRadius, currentDistance;
    private View linearLayoutCustomViewUser, linearLayoutCustomViewResto;
    private RatingBar ratingBar_restoRating;

    private Bitmap lastBitmap = null;
    private Bitmap currentBitmap = null;

    private HashMap<String, Bitmap> markerBitmap = new HashMap<String, Bitmap>();

    private ClusterManager mClusterManagerRestaurant, mClusterManagerUser;
    private ClusterManagerRendererRestaurant mClusterManagerRendererRestaurant;
    private ClusterManagerRendererUser mClusterManagerRendererUser;
    private ArrayList<ClusterMarkerRestaurant> mClusterMarkersRestaurant = new ArrayList<>();
    private ArrayList<ClusterMarkerUser> mClusterMarkersUser = new ArrayList<>();
    MarkerManager markerManager;
    ClusterMarkerUser clickedUserMarker, lastClickedUserMarker;
    ClusterMarkerRestaurant clickedRestoMarker, lastClickedRestoMarker;

    Bundle bundleRestaurantPosition;

    RecyclerView recyclerView;
    ArrayList<ModelResto> allRestoList;
    ArrayList<Object> queryList;
    ArrayList<UserLocation> otherUserLocationList;
    AdapterMapSearch adapterMapSearch;

    SearchView searchView;
    MenuItem item_search;
    String currentSearchQuery = "";

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

        //init the search recyclerview
        adapterMapSearch = new AdapterMapSearch();
        recyclerView = v.findViewById(R.id.recyclerView_map_search);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        allRestoList = new ArrayList<>();
        queryList = new ArrayList<>();
        otherUserLocationList = new ArrayList<>();

        setAdapter(queryList);

        //init progressDialog
        progressDialog_logout = new ProgressDialog(getContext());
        progressDialog_logout.setMessage("Déconnexion...");
        //////////////////////////

        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        currentUser = ((UserSingleton) getActivity().getApplicationContext()).getUser();

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

        //get the restaurant location bundle from RestoFragment
        bundleRestaurantPosition = this.getArguments();

        if(bundleRestaurantPosition != null){
            Log.d(TAG, "resto : got bundle");

            Double restoLatitude = bundleRestaurantPosition.getDouble("restoLatitude");
            Double restoLongitude = bundleRestaurantPosition.getDouble("restoLongitude");

            if(restoLatitude != null && restoLongitude != null){
                Log.d(TAG, "resto : got bundle restoLocation " + restoLatitude + " " + restoLongitude);
                LatLng restoPosition = new LatLng(restoLatitude, restoLongitude);

                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        restoPosition, DEFAULT_ZOOM));

            }
        }
        ///////////////////////////////////////////////

        seekBar_distance = getView().findViewById(R.id.seekBar_distance);
        seekBar_distance.setProgress(seekBarProgress);

        linearLayoutCustomViewUser = getView().findViewById(R.id.linearLayoutCustomViewUser);
        textView_name = linearLayoutCustomViewUser.findViewById(R.id.textView_name);
        imageView_profile_picture = linearLayoutCustomViewUser.findViewById(R.id.imageView_profile_picture);
        button_profile = linearLayoutCustomViewUser.findViewById(R.id.button_profile);
        button_direction = linearLayoutCustomViewUser.findViewById(R.id.button_direction);
        button_message = linearLayoutCustomViewUser.findViewById(R.id.button_message);

        linearLayoutCustomViewResto = getView().findViewById(R.id.linearLayoutCustomViewResto);
        textView_restoName = linearLayoutCustomViewResto.findViewById(R.id.textView_restoName);
        textView_restoSpeciality = linearLayoutCustomViewResto.findViewById(R.id.textView_restoSpeciality);
        textView_restoRating = linearLayoutCustomViewResto.findViewById(R.id.textView_restoRating);
        textView_restoRatingCount = linearLayoutCustomViewResto.findViewById(R.id.textView_restoRatingCount);
        ratingBar_restoRating = linearLayoutCustomViewResto.findViewById(R.id.ratingBar_restoRating);
        imageView_logoResto = linearLayoutCustomViewResto.findViewById(R.id.imageView_logoResto);


        button_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(clickedUserMarker != null) {
                    UserLocation userLocation = clickedUserMarker.getUserLocation();

                    if(userLocation.getUser_id() != currentUser.getUser_id()){
                        Intent intent = new Intent(getActivity(), ChatActivity.class);
                        intent.putExtra("other_user_id", userLocation.getUser_id());
                        intent.putExtra("other_user_name", userLocation.getName());
                        intent.putExtra("other_user_profile_picture", userLocation.getProfile_image());
                        startActivity(intent);
                    }
                }
            }
        });

        button_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(clickedUserMarker != null) {
                    UserLocation userLocation = clickedUserMarker.getUserLocation();
                     if(userLocation.getUser_id() == currentUser.getUser_id()){
                         startActivity(new Intent(getActivity(), ProfileUserActivity.class));
                     }else{
                         Intent intent = new Intent(getActivity(), OtherUsersProfileActivity.class);
                         intent.putExtra("user_id", userLocation.getUser_id());
                         startActivity(intent);
                     }
                }
            }
        });

        linearLayoutCustomViewResto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(clickedRestoMarker != null){
                    ModelResto resto = clickedRestoMarker.getResto();
                    if (resto.getId_resto().equals("resto_" + currentUser.getUser_id())) {
                        //send currentUser to his resto profile
                        startActivity(new Intent(getActivity(), ProfileRestoActivity.class));
                    }else {
                        //send currentUser to other resto profile
                        Intent intent = new Intent(getActivity(), OtherRestoProfileActivity.class);
                        intent.putExtra("id_resto", resto.getId_resto());
                        startActivity(intent);
                    }
                }
            }
        });

        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener()
        {
            @Override
            public void onMapClick(LatLng arg0)
            {
                Log.d(TAG, "test : map clicked");
                if (linearLayoutCustomViewUser.getVisibility() == View.VISIBLE){
                    linearLayoutCustomViewUser.setVisibility(View.GONE);
                }

                if (linearLayoutCustomViewResto.getVisibility() == View.VISIBLE){
                    linearLayoutCustomViewResto.setVisibility(View.GONE);
                }

                searchView.setQuery("", false);
                searchView.setIconified(true);
//                if(otherMarkerList.size() > 0){
//                    for(int i=0; i < otherMarkerList.size(); i++){
//                        otherMarkerList.get(i).setIcon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(mCustomDefaultMarkerView, bitmap)));
//                    }
//                }
            }


        });

        if(mServicesIsGood) {
            updateLocationUI();
            checkSeekBarDistance();
            initialiseClusters();
        }
    }

//    private void showRestaurants(){
//        restoReference.get()
//                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                    @Override
//                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                        if (!queryDocumentSnapshots.isEmpty()) {
//                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
//                                final ModelResto resto = documentSnapshot.toObject(ModelResto.class);
//
//                                Map<String, Object> restoLocation = resto.getLocation_resto();
//
//                                Double latitude = (Double) restoLocation.get("latitude");
//                                Double longitude = (Double) restoLocation.get("longitude");
//
//                                final LatLng restoPosition = new LatLng(latitude, longitude);
//
//                                Picasso.get().load(resto.getLogo_resto())
//                                        .resize(100, 100)
//                                        .transform(new CropTransformation(70, 60, CropTransformation.GravityHorizontal.CENTER, CropTransformation.GravityVertical.CENTER))
//                                        .into(new Target() {
//                                    @Override
//                                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//                                        // Todo: Do something with your bitmap here
//
//                                        mGoogleMap.addMarker(new MarkerOptions()
//                                                .position(restoPosition)
//                                                .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
//
//                                        Log.d(TAG, "self : add resto " + resto.getName_resto());
//                                    }
//
//                                    @Override
//                                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
//
//                                    }
//
//                                    @Override
//                                    public void onPrepareLoad(Drawable placeHolderDrawable) {
//                                    }
//                                });
//                            }
//                        }
//                    }
//
//                }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_LONG).show();
//            }
//        });
//
//    }

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

    //Initialise the cluster managers for Restaurants and Users and set the rendrers, and clickListners
    private void initialiseClusters(){
        markerManager = new MarkerManager(mGoogleMap);

        if(mGoogleMap != null) {
            if (mClusterManagerRestaurant == null) {
                mClusterManagerRestaurant = new ClusterManager<ClusterMarkerRestaurant>(getActivity().getApplicationContext(), mGoogleMap, markerManager);
                mClusterManagerRestaurant.setAlgorithm(new GridBasedAlgorithm<ClusterMarkerRestaurant>());
            }

            if (mClusterManagerUser == null) {
                mClusterManagerUser = new ClusterManager<ClusterMarkerUser>(getActivity().getApplicationContext(), mGoogleMap, markerManager);
                mClusterManagerUser.setAlgorithm(new GridBasedAlgorithm<ClusterMarkerUser>());
            }

            if (mClusterManagerRendererRestaurant == null) {
                mClusterManagerRendererRestaurant = new ClusterManagerRendererRestaurant(
                        getActivity(),
                        mGoogleMap,
                        mClusterManagerRestaurant
                );
                mClusterManagerRestaurant.setRenderer(mClusterManagerRendererRestaurant);
                getRestaurantLocation();

                mClusterManagerRestaurant.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener() {
                    @Override
                    public boolean onClusterItemClick(ClusterItem item) {
                        Log.d(TAG, " marker : click on restaurant " + item.getTitle());

                        if(linearLayoutCustomViewUser.getVisibility() == View.VISIBLE){
                            linearLayoutCustomViewUser.setVisibility(View.GONE);
                        }

                        clickedRestoMarker = (ClusterMarkerRestaurant) item;
                        ModelResto clickedResto = clickedRestoMarker.getResto();

//                        mClusterManagerRendererRestaurant.getMarker(clickedRestoMarker).hideInfoWindow();

                        //Set the data for the clicked Restaurant
                        textView_restoName.setText(clickedResto.getName_resto());
                        textView_restoSpeciality.setText(clickedResto.getSpeciality_resto());
                        textView_restoRating.setText(clickedResto.getRating_resto());
                        ratingBar_restoRating.setRating(Float.parseFloat(clickedResto.getRating_resto()));
                        textView_restoRatingCount.setText(clickedResto.getNbrRating_resto());
                        Picasso.get().load(clickedResto.getLogo_resto()).resize(100, 100).transform(new CropCircleTransformation()).into(imageView_logoResto);

                        if (clickedRestoMarker.equals(lastClickedRestoMarker)) {
                            if (linearLayoutCustomViewResto.getVisibility() == View.VISIBLE) {
                                linearLayoutCustomViewResto.setVisibility(View.GONE);
//
                            } else {
                                linearLayoutCustomViewResto.setVisibility(View.VISIBLE);
                            }

                        } else {

                            lastClickedRestoMarker = clickedRestoMarker;

                            linearLayoutCustomViewResto.setVisibility(View.VISIBLE);
//                                        clickedUserMarker.setIcon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(mCustomClickedMarkerView, bitmap)));
                        }

                        return false;
                    }
                });
            }

            if (mClusterManagerRendererUser == null) {
                mClusterManagerRendererUser = new ClusterManagerRendererUser(
                        getActivity(),
                        mGoogleMap,
                        mClusterManagerUser
                );
                mClusterManagerUser.setRenderer(mClusterManagerRendererUser);
                getOtherLocation();

                mClusterManagerUser.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener() {
                    @Override
                    public boolean onClusterItemClick(ClusterItem item) {
                        Log.d(TAG, " marker : click on currentUser " + item.getTitle());
                        if(linearLayoutCustomViewResto.getVisibility() == View.VISIBLE){
                            linearLayoutCustomViewResto.setVisibility(View.GONE);
                        }

                            clickedUserMarker = (ClusterMarkerUser) item;
                            UserLocation userLocation = clickedUserMarker.getUserLocation();

                            if(userLocation.getUser_id() == currentUser.getUser_id()){
                                button_direction.setVisibility(View.GONE);
                                button_message.setVisibility(View.GONE);
                            }else{
                                button_direction.setVisibility(View.VISIBLE);
                                button_message.setVisibility(View.VISIBLE);

//                                PolylineOptions polylineOptions = new PolylineOptions()
//                                        .add(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()))
//                                        .add(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()))
//                                        .color(Color.BLUE)
//                                        .endCap(new RoundCap());
//
//                                Polyline polyline = mGoogleMap.addPolyline(polylineOptions);
                            }

                            textView_name.setText(userLocation.getName());

                            Picasso.get().load(userLocation.getProfile_image()).resize(100, 100).transform(new CropCircleTransformation()).into(imageView_profile_picture);

                        if (clickedUserMarker.equals(lastClickedUserMarker)) {
                            if (linearLayoutCustomViewUser.getVisibility() == View.VISIBLE) {
                                linearLayoutCustomViewUser.setVisibility(View.GONE);
//                                            clickedUser.setIcon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(mCustomDefaultMarkerView, bitmap)));
                            } else {
                                linearLayoutCustomViewUser.setVisibility(View.VISIBLE);
//                                            marker.setIcon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(mCustomClickedMarkerView, bitmap)));
                            }

                        } else {

                            lastClickedUserMarker = clickedUserMarker;

                            linearLayoutCustomViewUser.setVisibility(View.VISIBLE);
//                                        clickedUserMarker.setIcon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(mCustomClickedMarkerView, bitmap)));
                        }

                        return false;
                    }
                });
            }

//            mGoogleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
//                @Override
//                public void onCameraChange(CameraPosition cameraPosition) {
//                    mClusterManagerRestaurant.onCameraIdle();
//                    mClusterManagerUser.onCameraIdle();
//                }
//            });
        }
    }

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
        Log.d(TAG, "self: starting runnable for retrieving updated other_locations.");
        otherHandler.postDelayed(mOtherRunnable = new Runnable() {
            @Override
            public void run() {
                updateOtherLocation();
//                updateUserLocation();
                otherHandler.postDelayed(mOtherRunnable, LOCATION_UPDATE_INTERVAL);
            }
        }, LOCATION_UPDATE_INTERVAL);
    }

    //STOP THE SERVICE
    private void stopOtherLocationUpdates(){
        otherHandler.removeCallbacks(mOtherRunnable);
    }

    //START THE SERVICE TO RETRIEVE OTHER'S LOCATION
    private void startUserLocationsRunnable(){
        Log.d(TAG, "self: starting runnable for retrieving updated self_locations.");
        userHandler.postDelayed(mUserRunnable = new Runnable() {
            @Override
            public void run() {
                updateUserLocation();
                userHandler.postDelayed(mUserRunnable, LOCATION_UPDATE_INTERVAL);
            }
        }, LOCATION_UPDATE_INTERVAL);
    }

    //STOP THE SERVICE
    private void stopUserLocationUpdates(){
        userHandler.removeCallbacks(mUserRunnable);
    }

    //CHECK THE SEEKBAR CHANGE STATE
    // IF CHECKED THEN START THE USERLOCATIONRUNNABLE AND SHOW OTHERS ON THE MAP WITH MARKERS
    //IF NOT STOP THE RUNNABLE AND CLEAR THE MARKERS AND THE LIST
    private void getRestaurantLocation(){
        restoReference.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                final ModelResto resto = documentSnapshot.toObject(ModelResto.class);

                                Double latitude = resto.getLatitude();
                                Double longitude = resto.getLongitude();

                                final LatLng restoPosition = new LatLng(latitude, longitude);

                                ClusterMarkerRestaurant clusterMarkerRestaurant = new ClusterMarkerRestaurant(
                                        restoPosition,
                                        "",
                                        "",
                                        resto
                                );

                                mClusterManagerRestaurant.addItem(clusterMarkerRestaurant);
                                mClusterMarkersRestaurant.add(clusterMarkerRestaurant);
                                allRestoList.add(resto);
                            }

                            mClusterManagerRestaurant.cluster();
                            Log.d(TAG, " search : resto size" + allRestoList.size());
                        }
                    }

                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }

    private void getOtherLocation(){
        mUserLocationReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final int[] i = {0};

                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    //Getting User object from dataSnapshot
                    final UserLocation userLocation = data.getValue(UserLocation.class);

                    if(userLocation != null){
                        final LatLng otherPosition = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
//                        final Marker[] marker = new Marker[1];
//                        String url = userLocation.getProfile_image();
//
//                        if(!userLocation.getUser_id().equals(currentUser.getUser_id())){
//                            Log.d(TAG, " test : onDataChange: " + userLocation.getName());
//
//                            Picasso.get().load(url).resize(100, 100).transform(new CropCircleTransformation()).into(new Target() {
//                                @Override
//                                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//
//                                    MarkerOptions markerOptions = new MarkerOptions()
//                                            .position(otherPosition)
//                                            .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(mCustomDefaultMarkerView, bitmap)))
//                                            .visible(false);
//
//                                    marker[0] = mGoogleMap.addMarker(markerOptions);
//                                    marker[0].setTag(userLocation);
//
////                                    marker[0].setVisible(false);
//
//                                    userMarkerOptionsList.add(markerOptions);
//
//                                    otherMarkerList.add(i[0], marker[0]);
//
//                                    markerBitmap.put(userLocation.getUser_id(), bitmap);
//                                    i[0]++;
//                                }
//
//                                @Override
//                                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
//
//                                }
//
//                                @Override
//                                public void onPrepareLoad(Drawable placeHolderDrawable) {
//                                }
//                            });
//                        }

                        if(!userLocation.getUser_id().equals(currentUser.getUser_id())){
                            ClusterMarkerUser clusterMarkerUser = new ClusterMarkerUser(
                                    otherPosition,
                                    "",
                                    "",
                                    userLocation
                            );

//                            mClusterManagerUser.addItem(clusterMarkerUser);
                            mClusterMarkersUser.add(i[0], clusterMarkerUser);
                            otherUserLocationList.add(i[0], userLocation);
                        }
                    }
                }

                mClusterManagerUser.cluster();

                Log.d(TAG, " search : otherList size" + otherUserLocationList.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "%s" + error);
            }
        });
    }

    //check the seekBar value and draw a circle relative on it
    private void checkSeekBarDistance(){
        seekBar_distance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekBarProgress = progress;

                distanceRadius = seekBarProgress*100;
                distanceCircle.setRadius(distanceRadius);

//                for(int i = 0; i < otherMarkerList.size(); i++){
//                    Marker marker = otherMarkerList.get(i);
//
//                    Location location = new Location("otherLocation");
//                    location.setLatitude(marker.getPosition().latitude);
//                    location.setLongitude(marker.getPosition().longitude);
//
//                    currentDistance = Math.round(lastKnownLocation.distanceTo(location));
//
//                    if(currentDistance < distanceRadius){
//                        marker.setVisible(true);
//                    }else {
//                        marker.setVisible(false);
//                    }
//                }

                for(int i = 0; i < mClusterMarkersUser.size(); i++){
                    ClusterMarkerUser markerUser = mClusterMarkersUser.get(i);

                    Location location = new Location("otherLocation");
                    location.setLatitude(markerUser.getPosition().latitude);
                    location.setLongitude(markerUser.getPosition().longitude);

                    currentDistance = Math.round(lastKnownLocation.distanceTo(location));

                    if(currentDistance < distanceRadius){
                        mClusterManagerUser.addItem(markerUser);
                    }else {
                        mClusterManagerUser.removeItem(markerUser);
                    }
                    mClusterManagerUser.cluster();
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
                    if(!mClusterMarkersUser.isEmpty()){
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
                    ClusterMarkerUser markerUser;

                    if(!userLocation.getUser_id().equals(currentUser.getUser_id())){
                        try {
                            markerUser = mClusterMarkersUser.get(i);
                            markerUser.setPosition(otherPosition);
                            mClusterMarkersUser.set(i, markerUser);
                            i++;

                            mClusterManagerUser.cluster();

                            Log.d(TAG, " marker : other position updated" + userLocation.getName());
                        }catch (Exception e){
                            Log.e(TAG, "%s" + e);
                        }
                    }

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "%s" + error);
            }
        });
    }

    private void updateUserLocation(){
        Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
        locationResult.addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    // Set the map's camera position to the current location of the device.
                    lastKnownLocation = task.getResult();

                    LatLng userPosition = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                    distanceCircle.setCenter(userPosition);
                    userMarker.setPosition(userPosition);

//                    Log.d(TAG, "self: set currentUser marker position");
                }
            }
        });

//        mUserLocationReference.child(currentUser.getUser_id()).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                for (DataSnapshot data : dataSnapshot.getChildren()) {
//                    //Getting User object from dataSnapshot
//                    UserLocation userLocation = data.getValue(UserLocation.class);
//
//                    Log.d(TAG, "self: " + data.getValue());
//                    LatLng userPosition = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
//                    try {
//                        distanceCircle.setCenter(userPosition);
//                        userMarker.setPosition(userPosition);
//
//                        Log.d(TAG, "self: set marker position");
//                    }catch (Exception e){
//                        Log.e(TAG, "%s" + e);
//                    }
//                }
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.e(TAG, "%s" + error);
//            }
//        });
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

                                if(bundleRestaurantPosition == null){
                                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                            new LatLng(lastKnownLocation.getLatitude(),
                                                    lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                                }


                                String url = currentUser.getProfile_image();

                                final LatLng myPosition = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());

                                Picasso.get().load(url).resize(100, 100).transform(new CropCircleTransformation()).into(new Target() {
                                    @Override
                                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                        // Todo: Do something with your bitmap here

                                        userMarker = new ClusterMarkerUser(
                                                myPosition,
                                                "",
                                                "",
                                                mUserLocation
                                        );

                                        mClusterManagerUser.addItem(userMarker);
                                        mClusterManagerUser.cluster();

                                        Log.d(TAG, "self : add marker");

                                        markerBitmap.put(currentUser.getUser_id(), bitmap);

                                        startUserLocationsRunnable();
                                    }

                                    @Override
                                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                                    }

                                    @Override
                                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                                    }
                                });

//                                if(!url.equals("NULL")){
//                                    Log.d(TAG, "self : test non null");
//
//                                    Picasso.get().load(url).resize(100, 100).transform(new CropCircleTransformation()).into(new Target() {
//                                        @Override
//                                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//                                            // Todo: Do something with your bitmap here
//
//                                            userMarker = mGoogleMap.addMarker(new MarkerOptions()
//                                                    .position(myPosition)
//                                                    .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(mCustomDefaultMarkerView, bitmap))));
//                                            userMarker.setTag(mUserLocation);
//
//                                            Log.d(TAG, "self : add marker");
//
//                                            markerBitmap.put(currentUser.getUser_id(), bitmap);
//
//                                            startUserLocationsRunnable(userMarker);
//                                        }
//
//                                        @Override
//                                        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
//
//                                        }
//
//                                        @Override
//                                        public void onPrepareLoad(Drawable placeHolderDrawable) {
//                                        }
//                                    });
//                                }else{
//                                    Log.d(TAG, "self : test null");
//
//                                    Picasso.get().load(R.drawable.ic_image_profile_icon_dark).resize(100, 100).transform(new CropCircleTransformation()).into(new Target() {
//                                        @Override
//                                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//                                            // Todo: Do something with your bitmap here
//
//                                            userMarker = mGoogleMap.addMarker(new MarkerOptions()
//                                                    .position(myPosition)
//                                                    .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(mCustomDefaultMarkerView, bitmap))));
//                                            userMarker.setTag(mUserLocation);
//
//                                            Log.d(TAG, "self : add marker");
//
//                                            markerBitmap.put(currentUser.getUser_id(), bitmap);
//
//                                            startUserLocationsRunnable(userMarker);
//                                        }
//
//                                        @Override
//                                        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
//
//                                        }
//
//                                        @Override
//                                        public void onPrepareLoad(Drawable placeHolderDrawable) {
//                                        }
//                                    });
//                                }

                                mUserLocation.setLatitude(geoPoint.getLatitude());
                                mUserLocation.setLongitude(geoPoint.getLongitude());

                                distanceCircle = mGoogleMap.addCircle(new CircleOptions()
                                        .center(new LatLng(lastKnownLocation.getLatitude(),
                                                lastKnownLocation.getLongitude()))
                                        .radius(seekBarProgress*100)
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
            //everything is fine and the currentUser can make map requests
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

        mClusterMarkersUser.clear();
        userMarkerOptionsList.clear();
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

    //Configure the recyclerView's adapter
    public void setAdapter(ArrayList list){
        adapterMapSearch = new AdapterMapSearch(getActivity(), list, this);
        recyclerView.setAdapter(adapterMapSearch);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_activity_main, menu);
        super.onCreateOptionsMenu(menu, inflater);

//        mClusterMarkersRestaurant;

        item_search =  menu.findItem(R.id.menu_search_button);
        searchView = (SearchView) MenuItemCompat.getActionView(item_search);

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!currentSearchQuery.equals("")){
                    searchView.setQuery(currentSearchQuery, false);
                }

                if (linearLayoutCustomViewUser.getVisibility() == View.VISIBLE){
                    linearLayoutCustomViewUser.setVisibility(View.GONE);
                }
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //called when currentUser press search button
                if (!TextUtils.isEmpty(query)){
                    searchQuery(query);

                    setAdapter(queryList);
                }else {
                    setAdapter(queryList);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                //called as and when currentUser press any lettre
                queryList.clear();

                if (!TextUtils.isEmpty(query)){
                    searchQuery(query);

                    setAdapter(queryList);
                }else {
                    setAdapter(queryList);
                }
                return false;
            }
        });
    }

    private void searchQuery(String query)
    {
        if(!allRestoList.isEmpty()){
            ModelResto resto;
            for (int i = 0; i < allRestoList.size(); i++) {
                resto = allRestoList.get(i);
                if (resto.getName_resto().toLowerCase().contains(query.toLowerCase())) {
                    queryList.add(resto);
                }
            }
        }
        if(!otherUserLocationList.isEmpty()){
            UserLocation userLocation;
            for (int i = 0; i < otherUserLocationList.size(); i++) {
                userLocation = otherUserLocationList.get(i);
                if (userLocation.getName().toLowerCase().contains(query.toLowerCase())) {
                    queryList.add(userLocation);
                }
            }
        }
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
        if(currentUser !=null)
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
                            dialog.cancel();
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

    //Handle clcik on restaurant search results
    @Override
    public void onMapSearchClick(int position) {

        Object object = queryList.get(position);
        Double searchLatitude = lastKnownLocation.getLatitude(),
                searchLongitude = lastKnownLocation.getLongitude();

        if(object instanceof ModelResto){
            searchLatitude = ((ModelResto) object).getLatitude();
            searchLongitude = ((ModelResto) object).getLongitude();
        }else if(object instanceof UserLocation){
            searchLatitude = ((UserLocation) object).getLatitude();
            searchLongitude = ((UserLocation) object).getLongitude();
        }

        if (mLocationPermissionGranted){
            if(!queryList.isEmpty()){
                if(searchLatitude != null && searchLongitude != null){
                    Log.d(TAG, "resto : got bundle restoLocation " + searchLatitude + " " + searchLongitude);
                    LatLng restoPosition = new LatLng(searchLatitude, searchLongitude);

                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            restoPosition, DEFAULT_ZOOM));
                }
            }
        }

        currentSearchQuery = searchView.getQuery().toString();
        searchView.setQuery("",false);
        searchView.setIconified(true);
    }
}