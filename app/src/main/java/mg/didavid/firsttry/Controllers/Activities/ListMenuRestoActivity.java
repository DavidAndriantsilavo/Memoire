package mg.didavid.firsttry.Controllers.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iceteck.silicompressorr.FileUtils;
import com.iceteck.silicompressorr.SiliCompressor;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import mg.didavid.firsttry.Controllers.Adapteurs.AdapterListMenu;
import mg.didavid.firsttry.Models.ModelRestoSampleMenu;
import mg.didavid.firsttry.R;

public class ListMenuRestoActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<ModelRestoSampleMenu> modelRestoSampleMenuList;
    private AdapterListMenu adapterListMenu;
    private TextView textView_aboutListMenu;

    private String id_resto, user_id;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_menu_resto);

        //init recycler view
        textView_aboutListMenu = findViewById(R.id.textView_indication_menuList);
        recyclerView = findViewById(R.id.recyclerView_menuList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        modelRestoSampleMenuList = new ArrayList<>();

        user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //get data from intent
        Intent intent = getIntent();
        id_resto = intent.getStringExtra("key");
        if (id_resto.contains(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            textView_aboutListMenu.setVisibility(View.VISIBLE);
        }else {
            textView_aboutListMenu.setVisibility(View.GONE);
        }

        setData();
        configureToolbar();
    }

    private void setData() {
        //get data
        final CollectionReference collectionReference_menuList = FirebaseFirestore.getInstance().collection("Menu_list");
        collectionReference_menuList.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots != null) {
                            //get data snapshot
                            collectionReference_menuList
                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                            if (!value.isEmpty()) {
                                                modelRestoSampleMenuList.clear();
                                                List<ModelRestoSampleMenu> sampleMenuList = value.toObjects(ModelRestoSampleMenu.class);
                                                int size = sampleMenuList.size();
                                                for (int i = 0; i < size; i++) {
                                                    if (sampleMenuList.get(i).getId_resto().equals(id_resto)) {
                                                        modelRestoSampleMenuList.add(sampleMenuList.get(i));
                                                    }
                                                }
                                                //add data to adapter
                                                adapterListMenu = new AdapterListMenu(ListMenuRestoActivity.this, modelRestoSampleMenuList);
                                                //set adapter to recyclerView
                                                recyclerView.setAdapter(adapterListMenu);
                                            }
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ListMenuRestoActivity.this, "Impossible de charger les menus. Veuillez recommencer", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);

        //hide others menu
        menu.findItem(R.id.menu_search_button).setVisible(false);
        menu.findItem(R.id.menu_activity_main_profile).setVisible(false);
        menu.findItem(R.id.menu_logout_profil).setVisible(false);
        if (id_resto.equals("resto_" + user_id)) {
            menu.findItem(R.id.menu_activity_main_addNewPost).setVisible(true);
        }else {
            menu.findItem(R.id.menu_activity_main_addNewPost).setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_activity_main_addNewPost :
                //add new menu in menu list
                //send user to AddMenuListActivity
                startActivity(new Intent(ListMenuRestoActivity.this, AddMenuToListActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void configureToolbar(){
        // Get the toolbar view inside the activity layout
        Toolbar toolbar = findViewById(R.id.toolbar_menuList);
        if (toolbar != null){
            //set toolbar title
            // Sets the Toolbar
            setSupportActionBar(toolbar);
            this.setTitle("Liste de menu");
            toolbar.setTitleTextAppearance(this, R.style.toolBarOtherUsers);
        }else {
            Toast.makeText(this, "Tsy misy titre :-(", Toast.LENGTH_SHORT).show();
        }
    }
}
