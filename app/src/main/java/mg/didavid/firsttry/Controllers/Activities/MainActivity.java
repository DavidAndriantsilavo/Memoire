package mg.didavid.firsttry.Controllers.Activities;
;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInApi;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;

import mg.didavid.firsttry.Controllers.Fragments.ActuFragment;
import mg.didavid.firsttry.Controllers.Fragments.GMapFragment;
import mg.didavid.firsttry.Controllers.Fragments.MessageFragment;
import mg.didavid.firsttry.Controllers.Fragments.ParametreFragment;
import mg.didavid.firsttry.Controllers.Fragments.RestoFragment;
import mg.didavid.firsttry.Models.PageAdapter;
import mg.didavid.firsttry.R;

public class MainActivity extends AppCompatActivity{

    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        actionBar = getSupportActionBar();


        BottomNavigationView navigationView = findViewById(R.id.menu_nav);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);

        GMapFragment fragment3 = new GMapFragment();
        FragmentTransaction frag3 = getSupportFragmentManager().beginTransaction();
        frag3.replace(R.id.content_nav, fragment3);
        frag3.commit();

    }

    BottomNavigationView.OnNavigationItemSelectedListener selectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    switch (item.getItemId()){
                        case R.id.map_navv:
                            //actionBar.setTitle("First try");
                            GMapFragment fragment3 = new GMapFragment();
                            FragmentTransaction frag3 = getSupportFragmentManager().beginTransaction();
                            frag3.replace(R.id.content_nav, fragment3);
                            frag3.commit();
                            return true;

                        case R.id.fil_d_actu_nav:
                            //actionBar.setTitle("First try");
                            ActuFragment fragment1 = new ActuFragment();
                            FragmentTransaction frag1 = getSupportFragmentManager().beginTransaction();
                            frag1.replace(R.id.content_nav, fragment1);
                            frag1.commit();
                            return true;

                        case R.id.resto_nav:
                            //actionBar.setTitle("First try");
                            RestoFragment fragment2 = new RestoFragment();
                            FragmentTransaction frag2 = getSupportFragmentManager().beginTransaction();
                            frag2.replace(R.id.content_nav, fragment2);
                            frag2.commit();
                            return true;

                        case R.id.message_nav:
                            //actionBar.setTitle("First try");
                            MessageFragment fragment4 = new MessageFragment();
                            FragmentTransaction frag4 = getSupportFragmentManager().beginTransaction();
                            frag4.replace(R.id.content_nav, fragment4);
                            frag4.commit();
                            return true;

                        case R.id.parametre_nav:
                            //actionBar.setTitle("First try");
                            ParametreFragment fragment5 = new ParametreFragment();
                            FragmentTransaction frag5 = getSupportFragmentManager().beginTransaction();
                            frag5.replace(R.id.content_nav, fragment5);
                            frag5.commit();
                            return true;
                    }
                    return false;
                }
            };
}