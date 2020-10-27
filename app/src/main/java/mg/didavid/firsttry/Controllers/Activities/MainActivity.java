package mg.didavid.firsttry.Controllers.Activities;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import androidx.appcompat.widget.Toolbar;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentTransaction;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenu;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import mg.didavid.firsttry.Controllers.Fragments.ActuFragment;
import mg.didavid.firsttry.Controllers.Fragments.GMapFragment;
import mg.didavid.firsttry.Controllers.Fragments.MessageFragment;
import mg.didavid.firsttry.Controllers.Fragments.ParametreFragment;
import mg.didavid.firsttry.Controllers.Fragments.RestoFragment;
import mg.didavid.firsttry.Models.User;
import mg.didavid.firsttry.Models.UserSingleton;
import mg.didavid.firsttry.R;
import mg.didavid.firsttry.Views.AppMode;

public class MainActivity extends AppMode {

    BottomNavigationView navigationView;

    FirebaseUser FirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference userCollectionReference = db.collection("Users");

    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

    public static Activity stopActivity;

    private final String TAG = "MainActivity";

    private int mInterval = 900000; // 15 minutes by default, can be changed later
    private Handler mHandler;

    int i = 0;

    boolean firstTime = true, haveBadge;
    int count = 0, countFirstTime = 0;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        stopActivity = this;

        this.configureToolbar();

        mHandler = new Handler();
        startRepeatingTask();

        navigationView = findViewById(R.id.menu_nav); //associate view with the BottomNavigationView object
        navigationView.setOnNavigationItemSelectedListener(selectedListener); //set BottomNavigationView focus onto the selected item

        //default view
        accueil();

        if(firebaseUser != null){
            userCollectionReference.document(firebaseUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        DocumentSnapshot data = task.getResult();
                        if(data.exists()){
                            User user = data.toObject(User.class);

                            ((UserSingleton)getApplicationContext()).setUser(user);
                            Log.d(TAG, "Singleton set");
                        }
                    }
                }
            });
        }
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                listenerDataChange();
            } finally {
                // 100% guarantee that this always happens, even if
                // the updated method throws an exception
                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }

    private void listenerDataChange() {
        final CollectionReference collectionPost = FirebaseFirestore.getInstance().collection("Publications");
        collectionPost.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w("ErreurDeQuery", "listenerChange", error);
                    return;
                }
                count = value.getDocuments().size();
                Log.d("count", "********************************************************" + count);
                if (firstTime) {
                    countFirstTime = count;
                    Log.d("countFirstTime", "********************************************************" + countFirstTime);
                    firstTime = false;
                }else if (countFirstTime < count){
                    addBadge(true);
                    Log.d("newDocs", "add badge : " + countFirstTime + "/" + count);
                    countFirstTime = count;
                    Log.d("newDocs", "badge numnber : " + countFirstTime + "/" + count);
                }
            }
        });
    }
    private void addBadge(boolean b) {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) navigationView.getChildAt(0);
        BottomNavigationItemView itemView = (BottomNavigationItemView) menuView.getChildAt(0);


        if (b) {
            View notificationBadge = LayoutInflater.from(this).inflate(R.layout.navigation_badge, menuView, false);
            itemView.addView(notificationBadge);
            haveBadge = true;
        }else {
            //itemView.addView(notificationBadge);
            View cardView = findViewById(R.id.layoutInflated);
            ViewGroup parent = (ViewGroup) cardView.getParent();
            parent.removeView(cardView);
            Log.d("navigationView", "********** mety");
            haveBadge = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();
    }

    private void accueil() {
        navigationView.setSelectedItemId(R.id.fil_d_actu_nav);
        itemActu();
        navigationView.setSelectedItemId(R.id.fil_d_actu_nav);
    }

    BottomNavigationView.OnNavigationItemSelectedListener selectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()){
                        //page GMap
                        case R.id.map_nav:
                            itemGMap();
                            return true;

                         //page actu
                        case R.id.fil_d_actu_nav:
                            itemActu();
                            if (haveBadge) {
                                addBadge(false);
                                Log.d("testEnter", "" + i + "/" + countFirstTime + "/" + count);
                                i++;
                            }
                            return true;

                         //page resto
                        case R.id.resto_nav:
                            itemResto();
                            return true;

                         //page message
                        case R.id.message_nav:
                            itemMessage();
                            return true;

                         //page parameters
                        case R.id.parametre_nav:
                            itemParametres();
                            return true;
                    }
                    return false;
                }
            };

    private void itemParametres() {
        ParametreFragment fragment5 = new ParametreFragment();
        FragmentTransaction frag5 = getSupportFragmentManager().beginTransaction();
        frag5.replace(R.id.content_nav, fragment5); //replace default View
        frag5.commit();
    }

    private void itemMessage() {
        MessageFragment fragment4 = new MessageFragment();
        FragmentTransaction frag4 = getSupportFragmentManager().beginTransaction();
        frag4.replace(R.id.content_nav, fragment4); //replace default View
        frag4.commit();
    }

    private void itemResto() {
        RestoFragment fragment2 = new RestoFragment();
        FragmentTransaction frag2 = getSupportFragmentManager().beginTransaction();
        frag2.replace(R.id.content_nav, fragment2); //replace default View
        frag2.commit();
    }

    private void itemActu() {
        ActuFragment fragment1 = new ActuFragment();
        FragmentTransaction frag1 = getSupportFragmentManager().beginTransaction();
        frag1.replace(R.id.content_nav, fragment1); //replace default View
        frag1.commit();
    }

    private void itemGMap() {
        GMapFragment fragment3 = new GMapFragment();
        FragmentTransaction frag3 = getSupportFragmentManager().beginTransaction();
        frag3.replace(R.id.content_nav, fragment3); //replace default View
        frag3.commit();
    }


    private void configureToolbar(){
        // Get the toolbar view inside the activity layout
        Toolbar toolbar = findViewById(R.id.toolbar);
        // Sets the Toolbar
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkConnexion();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        checkConnexion();
    }

    // CHECK IF INTERNET CONNEXION IS AVAILABLE
    public boolean checkConnexion(){
        ConnectivityManager cm =
                (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Veuillez vous connecter à internet!");
        builder.setCancelable(false);

        builder.setPositiveButton(
                "retour",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();

        if(!isConnected)
        {
            alert.show();
        }else {
            alert.dismiss();
        }
        return isConnected;
    }


    @Override
    public void onBackPressed() {
        if (navigationView.getSelectedItemId() != R.id.fil_d_actu_nav) {
            navigationView.setSelectedItemId(R.id.fil_d_actu_nav);
            itemActu();
        } else {
            super.onBackPressed(); // This will pop the Activity from the stack.
        }
    }

}