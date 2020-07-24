package mg.didavid.firsttry.Controllers.Activities;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

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

        BottomNavigationView navigationView = findViewById(R.id.menu_nav);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);

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
                        case R.id.map_nav:
                            itemGMap();
                            return true;

                        case R.id.fil_d_actu_nav:
                            ActuFragment fragment1 = new ActuFragment();
                            FragmentTransaction frag1 = getSupportFragmentManager().beginTransaction();
                            frag1.replace(R.id.content_nav, fragment1);
                            frag1.commit();
                            return true;

                        case R.id.resto_nav:
                            RestoFragment fragment2 = new RestoFragment();
                            FragmentTransaction frag2 = getSupportFragmentManager().beginTransaction();
                            frag2.replace(R.id.content_nav, fragment2);
                            frag2.commit();
                            return true;

                        case R.id.message_nav:
                            MessageFragment fragment4 = new MessageFragment();
                            FragmentTransaction frag4 = getSupportFragmentManager().beginTransaction();
                            frag4.replace(R.id.content_nav, fragment4);
                            frag4.commit();
                            return true;

                        case R.id.parametre_nav:
                            ParametreFragment fragment5 = new ParametreFragment();
                            FragmentTransaction frag5 = getSupportFragmentManager().beginTransaction();
                            frag5.replace(R.id.content_nav, fragment5);
                            frag5.commit();
                            return true;
                    }
                    return false;
                }
            };

    private void itemGMap() {

        GMapFragment fragment3 = new GMapFragment();
        FragmentTransaction frag3 = getSupportFragmentManager().beginTransaction();
        frag3.replace(R.id.content_nav, fragment3);
        frag3.commit();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //2 - Inflate the menu and add it to the Toolbar
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);
        return true;
    }

    // ----

    private void configureToolbar(){
        // Get the toolbar view inside the activity layout
        Toolbar toolbar = findViewById(R.id.toolbar);
        // Sets the Toolbar
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //3 - Handle actions on menu items
        switch (item.getItemId()) {
            case R.id.menu_activity_main_profile:
                Intent toProfile =  new Intent(getApplicationContext(), ProfileActivity.class);
                startActivity(toProfile);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void configureUser()
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
                        ((UserSingleton)(getApplicationContext())).setUser(user);
                        Log.d(TAG, "singleton : created from existing user" + user.getName());

                    } else {
                        email = firebaseUser.getEmail();
                        user_id = firebaseUser.getUid();
                        name = firebaseUser.getDisplayName();
                        phone = firebaseUser.getPhoneNumber();

                        final User user = new User(email,user_id, name,phone, password);

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
                    ((UserSingleton)(getApplicationContext())).setUser(user);
                    Log.d(TAG, "singleton : created from new user" + user.getName());
                }else{
                    //Something went wrong
                    Log.e(TAG, "onComplete: Error: " + task.getException().getLocalizedMessage() );
                }
            }
        });
    }
}