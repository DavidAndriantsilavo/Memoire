package mg.didavid.firsttry.Controllers.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mg.didavid.firsttry.Controllers.Adapteurs.AdapterSampleMenu;
import mg.didavid.firsttry.Controllers.Adapteurs.AdapteursPost;
import mg.didavid.firsttry.Models.ModelRestoSampleMenu;
import mg.didavid.firsttry.Models.ModelePost;
import mg.didavid.firsttry.R;
import mg.didavid.firsttry.Views.AppMode;

public class OtherRestoProfileActivity extends AppMode {

    String resto_name, rating_resto, speciality_resto, id_resto;

    TextView textView_restoName;
    ImageView imageView_logoResto, imageView_coverPhoto;
    RecyclerView recyclerView_sampleMenu;
    TextView textView_showAllMenu;
    ImageButton imageButton_fleche;
    RatingBar ratingBar;
    Button btn_noter, btn_commander;

    List<ModelePost> modelePosts_profile;
    AdapteursPost adapteursPost_profile;
    RecyclerView restoProfile_recyclerView;

    FirebaseFirestore firestore;
    CollectionReference collectionResto, collectioonPost, collectionComment; // Firestore's collection reference : root/reference
    DocumentReference docRefProfileResto; // reference of the document in Firestoer : root/reference/document

    private static final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private static final CollectionReference collectionReference_hasRatingResto = FirebaseFirestore.getInstance().collection("HasRatingResto");

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_resto_profile);

        //recuperation des vues
        textView_restoName = findViewById(R.id.texteView_nameResto_otherRestoprofile);
        imageView_logoResto = findViewById(R.id.imageView_logoResto_otherRestoprofile);
        imageView_coverPhoto = findViewById(R.id.imageView_photoDeCouverture_otherRestoprofile);
        recyclerView_sampleMenu = findViewById(R.id.recyclerView_sampleMenu_otherRestoprofile);
        restoProfile_recyclerView = findViewById(R.id.recyclerView_post_otherRestoprofile);
        textView_showAllMenu = findViewById(R.id.textView_showListMene_otherRestoProfile);
        imageButton_fleche = findViewById(R.id.flecheBtn_OtherRestoProfile);
        btn_noter = findViewById(R.id.btn_rate_otherRestoProfile);
        btn_commander = findViewById(R.id.btn_rate_otherRestoProfile);
        ratingBar = findViewById(R.id.ratingBar_otherRestoProfile);

        //linear layout for recyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(OtherRestoProfileActivity.this);
        //show newest post first (the newest post is in the last of the post list store on th database)
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set this layout to recyclerView
        restoProfile_recyclerView.setLayoutManager(layoutManager);
        modelePosts_profile = new ArrayList<>();

        firestore = FirebaseFirestore.getInstance();
        collectionResto = firestore.collection("Resto");

        collectioonPost = FirebaseFirestore.getInstance().collection("Publications");
        collectionComment = FirebaseFirestore.getInstance().collection("Comments");

        Intent intent = getIntent();
        id_resto = intent.getStringExtra("id_resto");

        imageButton_fleche.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OtherRestoProfileActivity.this, ListMenuRestoActivity.class);
                intent.putExtra("key", id_resto);
                startActivity(intent);
            }
        });

        textView_showAllMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OtherRestoProfileActivity.this, ListMenuRestoActivity.class);
                intent.putExtra("key", id_resto);
                startActivity(intent);
            }
        });

        btn_noter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check if user has rating resto yet, if not show rating dialog
                collectionReference_hasRatingResto.document(id_resto).get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    if (documentSnapshot.get(user.getUid()) != null) {
                                        Toast.makeText(OtherRestoProfileActivity.this, "Vous avez déjà noté ce restaurant", Toast.LENGTH_LONG).show();
                                    }else {
                                        showRatingDialog(id_resto);
                                    }
                                }else {
                                    showRatingDialog(id_resto);
                                }
                            }
                        });
            }
        });

        btn_commander.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChoiceDialog(id_resto);
            }
        });


        checkingRestoInfo();
        loadSampleMenu();
        loadRestoPost();
    }

    private void showChoiceDialog(final String id_resto) {
        String[] options = {"Appeler", "Envoyer un email"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        callRestaurant(id_resto);
                        break;
                    case 1:
                        sendEmailToRestaurant(id_resto);
                        break;
                }
            }
        });
        builder.create().show();
    }

    private void sendEmailToRestaurant(String id_resto) {
    }

    private void callRestaurant(String id_resto) {
        FirebaseFirestore.getInstance().collection("Resto").document(id_resto).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Intent callResto = new Intent(Intent.ACTION_CALL);
                        callResto.setData(Uri.parse("tel:" + documentSnapshot.getString("phone_resto")));
                        if (ActivityCompat.checkSelfPermission(OtherRestoProfileActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                            ActivityCompat.requestPermissions(OtherRestoProfileActivity.this, new String[] {Manifest.permission.CALL_PHONE}, 1996);
                            return;
                        }
                        startActivity(callResto);
                    }
                });
    }

    private void loadRestoPost() {
        CollectionReference reference_post = FirebaseFirestore.getInstance().collection("Publications");
        reference_post.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            modelePosts_profile.clear();
                            List<ModelePost> modelePost = queryDocumentSnapshots.toObjects(ModelePost.class);
                            int size = modelePost.size();
                            for (int i = 0; i < size; i++) {
                                if (modelePost.get(i).getUser_id().equals(id_resto)) {
                                    modelePosts_profile.add(modelePost.get(i));
                                }
                            }
                            //adapter
                            adapteursPost_profile = new AdapteursPost(OtherRestoProfileActivity.this, modelePosts_profile);
                            //set adapter to recyclerView
                            restoProfile_recyclerView.setAdapter(adapteursPost_profile);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(OtherRestoProfileActivity.this, ""+ e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showRatingDialog(final String id_resto) {
        //create dialog
        final Dialog ratingDialog = new Dialog(this);
        ratingDialog.setContentView(R.layout.dialog_rating);
        ratingDialog.setCanceledOnTouchOutside(false);

        //init dialog views
        final RatingBar ratingBar = ratingDialog.findViewById(R.id.ratingBar_ratingDialog_actuFragment);
        Button button_annuler = ratingDialog.findViewById(R.id.btn_annuler_ratingDialog);
        Button button_envoyer = ratingDialog.findViewById(R.id.btn_envoyer_ratingDialog);

        button_annuler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ratingDialog.dismiss();
            }
        });

        button_envoyer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DocumentReference documentReference_resto = FirebaseFirestore.getInstance().collection("Resto").document(id_resto);
                documentReference_resto.get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                float ratingResto = Float.parseFloat(documentSnapshot.getString("rating_resto"));
                                int nbrRatingResto = Integer.parseInt((documentSnapshot.getString("nbrRating_resto")));
                                float thisRatingResto = ratingBar.getRating();

                                ratingResto = ((ratingResto * nbrRatingResto) + thisRatingResto) / (nbrRatingResto + 1);
                                nbrRatingResto += 1;

                                //store new values
                                HashMap<String, Object> rating = new HashMap<>();
                                rating.put("rating_resto", String.valueOf(ratingResto));
                                rating.put("nbrRating_resto", String.valueOf(nbrRatingResto));
                                documentReference_resto.set(rating, SetOptions.merge());

                                //set user as having rate this restaurant
                                HashMap<String, Object> userRating = new HashMap<>();
                                userRating.put(user.getUid(), "rating");
                                collectionReference_hasRatingResto.document(id_resto).set(userRating, SetOptions.merge());

                                //update rating on restaurant post
                                final float finalRatingResto = ratingResto;
                                FirebaseFirestore.getInstance().collection("Publications").get()
                                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                            @Override
                                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                if (!queryDocumentSnapshots.isEmpty()) {
                                                    List<ModelePost> modelePostList = queryDocumentSnapshots.toObjects(ModelePost.class);
                                                    int size = modelePostList.size();
                                                    for (int i = 0; i < size; i++) {
                                                        if (modelePostList.get(i).getUser_id().equals(id_resto)) {
                                                            HashMap<String, Object> pseudo = new HashMap<>();
                                                            pseudo.put("pseudo", String.valueOf(finalRatingResto));
                                                            FirebaseFirestore.getInstance().collection("Publications").document(modelePostList.get(i).getPost_id()).update(pseudo);
                                                        }
                                                    }
                                                }
                                                ratingDialog.dismiss();
                                                Toast.makeText(OtherRestoProfileActivity.this, "Merci pour votre appreciation", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        });
            }
        });

        //show dialog
        ratingDialog.show();
    }

    private void loadSampleMenu() {

        final ArrayList<ModelRestoSampleMenu> restoSampleMenuArrayList = new ArrayList<>();

        CollectionReference collectionReference_sampleMenu = FirebaseFirestore.getInstance().collection("Sample_menu");
        collectionReference_sampleMenu.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                restoSampleMenuArrayList.clear();
                                List<ModelRestoSampleMenu> restoSampleMenus = queryDocumentSnapshots.toObjects(ModelRestoSampleMenu.class);
                                int size = restoSampleMenus.size();
                                for (int i = 0; i < size; i++) {
                                    if (restoSampleMenus.get(i).getId_resto().contains(id_resto)) {
                                        restoSampleMenuArrayList.add(restoSampleMenus.get(i));
                                    }
                                }
                            }

                            AdapterSampleMenu adapterSampleMenu = new AdapterSampleMenu(OtherRestoProfileActivity.this, restoSampleMenuArrayList);
                            recyclerView_sampleMenu.setLayoutManager(new LinearLayoutManager(OtherRestoProfileActivity.this, LinearLayoutManager.HORIZONTAL, false));
                            recyclerView_sampleMenu.setAdapter(adapterSampleMenu);
                        }
                    }
                });
    }

    private void goToShowImage(String imageUri) {
        Intent intent = new Intent(OtherRestoProfileActivity.this, ShowImageActivity.class);
        intent.putExtra("showImage", imageUri);
        Log.d("valeur de image uri", " :" + imageUri);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkConnexion();
        checkingRestoInfo();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        checkConnexion();
        checkingRestoInfo();
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

    //check if user has already informations
    private void checkingRestoInfo() {
        final ProgressDialog progressDialog_loadingProfile = new ProgressDialog(this);
        progressDialog_loadingProfile.setMessage("Chargement du profile");
        progressDialog_loadingProfile.show();
        docRefProfileResto = collectionResto.document(id_resto);
        docRefProfileResto.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    docRefProfileResto.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                            progressDialog_loadingProfile.dismiss();
                            resto_name = value.getString("name_resto");
                            speciality_resto = value.getString("speciality_resto");
                            rating_resto = value.getString("rating_resto");
                            final String logo_resto = value.getString("logo_resto");
                            final String coverPhoto_resto = value.getString("coverPhoto_resto");

                            //configure toolbar
                            configureToolbar(resto_name, speciality_resto);

                            //setting data from Firestore
                            setData(resto_name, speciality_resto, logo_resto, coverPhoto_resto, rating_resto);

                            //profile image clicked
                            imageView_logoResto.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    goToShowImage(logo_resto);
                                }
                            });
                            //cover photo clicked
                            imageView_coverPhoto.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    goToShowImage(coverPhoto_resto);
                                }
                            });
                        }
                    });
                }
            }
        }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    progressDialog_loadingProfile.dismiss();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(OtherRestoProfileActivity.this, "il y a eu une erreur lors de la verification de vos informations", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //setting user's data from Firestore
    private void setData(String resto_name, String speciality_resto, String logo_resto, String coverPhoto_resto, String rating_resto) {
        textView_restoName.setText(resto_name);
        ratingBar.setRating(Float.parseFloat(rating_resto));
        //set logo resto
        try {
            Picasso.get().load(logo_resto).placeholder(R.drawable.ic_image_profile_icon_dark).into(imageView_logoResto);
        }catch (Exception e){
            Picasso.get().load(R.drawable.ic_image_profile_icon_dark).into(imageView_logoResto);
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_LONG).show();
        }
        //set cover photo
        try {
            Picasso.get().load(coverPhoto_resto).into(imageView_coverPhoto);
        }catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(@NonNull final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);

        //hide others menu
        menu.findItem(R.id.menu_activity_main_addNewPost).setVisible(false);
        menu.findItem(R.id.menu_activity_main_profile).setVisible(false);
        menu.findItem(R.id.menu_logout_profil).setVisible(false);

        //searchView to seach post bydescription
        MenuItem item_search =  menu.findItem(R.id.menu_search_button);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item_search);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //called when user press search button
                if (!TextUtils.isEmpty(query)){
                    searchPost(query);
                }else {
                    loadRestoPost();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //called as and when user press any lettre
                if (!TextUtils.isEmpty(newText)){
                    searchPost(newText);
                }else {
                    loadRestoPost();
                }
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    private void searchPost(final String query) {
        //path of all post
        final CollectionReference collectionUsers = FirebaseFirestore.getInstance().collection("Publications");
        //get all data from this reference
        collectionUsers.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    modelePosts_profile.clear(); //for deleting auto redundancy
                    List<ModelePost> modelePost = queryDocumentSnapshots.toObjects(ModelePost.class);
                    int size = modelePost.size();
                    for (int i = 0; i < size; i++) {
                        if (modelePost.get(i).getPost_description().toLowerCase().contains(query.toLowerCase()) && modelePost.get(i).getUser_id().equals(id_resto)) {
                            modelePosts_profile.add(modelePost.get(i));
                        }
                    }
                    //adapter
                    adapteursPost_profile = new AdapteursPost(OtherRestoProfileActivity.this, modelePosts_profile);
                    //set adapter to recyclerView
                    restoProfile_recyclerView.setAdapter(adapteursPost_profile);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(OtherRestoProfileActivity.this, ""+e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void configureToolbar(String name, String pseudo){
        // Get the toolbar view inside the activity layout
        Toolbar toolbar = findViewById(R.id.toolbar_otherRestoprofile);
        if (toolbar != null){
            //set toolbar title
            // Sets the Toolbar
            setSupportActionBar(toolbar);
            this.setTitle(name);
            toolbar.setSubtitle(pseudo);
            toolbar.setTitleTextAppearance(this, R.style.toolBarOtherUsers);
        }else {
            Toast.makeText(this, "Tsy misy titre :-(", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onNavigateUp() {
        onBackPressed();
        return super.onNavigateUp();
    }
}
