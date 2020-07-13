package mg.didavid.firsttry.Controllers.Activities;
;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import androidx.core.view.KeyEventDispatcher;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import mg.didavid.firsttry.Controllers.Fragments.ActuFragment;
import mg.didavid.firsttry.Controllers.Fragments.GMapFragment;
import mg.didavid.firsttry.Controllers.Fragments.MessageFragment;
import mg.didavid.firsttry.Controllers.Fragments.ParametreFragment;
import mg.didavid.firsttry.Controllers.Fragments.RestoFragment;
import mg.didavid.firsttry.R;

public class MainActivity extends AppCompatActivity{

    BottomNavigationView navigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.configureToolbar();

        navigationView = findViewById(R.id.menu_nav); //associate view with the BottomNavigationView object
        navigationView.setOnNavigationItemSelectedListener(selectedListener); //set BottomNavigationView focus onto the selected item

        //default view
        accueil();



    }

    private void accueil() {
        navigationView.setSelectedItemId(R.id.fil_d_actu_nav);
        itemActu();
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
        if (item.getItemId() == R.id.menu_activity_main_profile) {
            Intent toProfile = new Intent(getApplicationContext(), ProfileActivity.class);
            startActivity(toProfile);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}