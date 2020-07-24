package mg.didavid.firsttry.Controllers.Activities;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;

import androidx.fragment.app.FragmentTransaction;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

import mg.didavid.firsttry.Controllers.Fragments.ActuFragment;
import mg.didavid.firsttry.Controllers.Fragments.GMapFragment;
import mg.didavid.firsttry.Controllers.Fragments.MessageFragment;
import mg.didavid.firsttry.Controllers.Fragments.ParametreFragment;
import mg.didavid.firsttry.Controllers.Fragments.RestoFragment;
import mg.didavid.firsttry.Models.User;
import mg.didavid.firsttry.Models.UserSingleton;
import mg.didavid.firsttry.R;

public class MainActivity extends AppCompatActivity{

    BottomNavigationView navigationView;

    ProgressDialog  progressDialog_del_account, progressDialog_logout;

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference userCollectionReference = db.collection("Users");

    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

    private String email = "NULL";
    private String user_id = "NULL";
    private String name = "NULL";
    private String phone = "NULL";
    private String password = "NULL";
    private final String TAG= "MainActivity";

    public static Activity stopActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        stopActivity = this;

        this.configureToolbar();

        navigationView = findViewById(R.id.menu_nav); //associate view with the BottomNavigationView object
        navigationView.setOnNavigationItemSelectedListener(selectedListener); //set BottomNavigationView focus onto the selected item


        //init progressDialog
        progressDialog_del_account = new ProgressDialog(this);
        progressDialog_del_account.setMessage("Supression de votre compte...");
        progressDialog_logout = new ProgressDialog(this);
        progressDialog_logout.setMessage("Déconnexion...");


        //default view
        accueil();



    }

    private void accueil() {
        navigationView.setSelectedItemId(R.id.fil_d_actu_nav);
        itemActu();
        navigationView.setSelectedItemId(R.id.fil_d_actu_nav);

        //IF USER IS AUTH TO FIREBASE AND NO SINGLETON SET
            if(firebaseUser != null && ((UserSingleton)(getApplicationContext())).getUser() == null){
                configureUser();
            }
    }

    BottomNavigationView.OnNavigationItemSelectedListener selectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()){

                        //page GMap
                        case R.id.map_navv:
                            itemGMap();
                            return true;

                         //page actu
                        case R.id.fil_d_actu_nav:
                            itemActu();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //3 - Handle actions on menu items
        switch (item.getItemId()) {
            case R.id.menu_logout_profil:
                avertissement();
                return true;
            case R.id.menu_activity_main_profile:
                startActivity(new Intent(getApplicationContext(), ProfileUserActivity.class));
                return true;
            case R.id.menu_activity_main_addNewPost:
                startActivity(new Intent(getApplicationContext(), NewPostActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void avertissement() {
        if(user!=null)
        {
            //BUILD ALERT DIALOG TO CONFIRM THE SUPPRESSION
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
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
                this,
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        ).signOut();

        Intent logOut =  new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(logOut);

        this.finish();
    }


    @Override
    public void onBackPressed() {
        if (navigationView.getSelectedItemId() != R.id.fil_d_actu_nav) {
            navigationView.setSelectedItemId(R.id.fil_d_actu_nav);
            itemActu();
        } else {
            super.onBackPressed(); // This will pop the Activity from the stack.
        }
        private void configureUser ()
        {
            DocumentReference documentReference = userCollectionReference.document(user_id);

            //CHECK IF THE USER IS ALREADY STORED IN THE DATABASE OR NOT
            //IF NOT THEN CREATE A NEW DOCUMENT WITH THE UID
            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    //Checking request result
                    if (task.isSuccessful()) {
                        //Request was successful but it never means that data is found
                        DocumentSnapshot data = task.getResult();
                        if (data.exists()) {
                            Toast.makeText(MainActivity.this, "YOU ARE ALREADY SAVED IN THE DATABASE !!!", Toast.LENGTH_SHORT).show();

                            //USER INSTANCE TO STORE THE USER FROM FIRESTORE IF THE DOCUMENT ALREADY EXISTS
                            User user = data.toObject(User.class);

                            //SET USER SINGLETON
                            ((UserSingleton) (getApplicationContext())).setUser(user);
                            Log.d(TAG, "singleton : created from existing user" + user.getName());

                        } else {
                            email = firebaseUser.getEmail();
                            user_id = firebaseUser.getUid();
                            name = firebaseUser.getDisplayName();
                            phone = firebaseUser.getPhoneNumber();

                            final User user = new User(email, user_id, name, phone, password);

                            storeUserData(user);
                        }

                    } else {
                        //Request was not successful
                        //Could be some rules or internet problem
                        Log.i(TAG, "onComplete: Request unsuccessful, error: " + task.getException().getLocalizedMessage());
                    }
                }
            });
        }
        //CREATE NEW USER IN FIRESTORE AND STORE DATAS
        private void storeUserData(final User user){
            DocumentReference documentReference = userCollectionReference.document(user_id);

            //Writing data and using call-back functions
            documentReference.set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        //Tas was successful
                        Toast.makeText(MainActivity.this, "User Added", Toast.LENGTH_SHORT).show();

                        //SET USER SINGLETON
                        ((UserSingleton) (getApplicationContext())).setUser(user);
                        Log.d(TAG, "singleton : created from new user" + user.getName());
                    } else {
                        //Something went wrong
                        Log.e(TAG, "onComplete: Error: " + task.getException().getLocalizedMessage());
                    }
                }
            });
        }
    }
}