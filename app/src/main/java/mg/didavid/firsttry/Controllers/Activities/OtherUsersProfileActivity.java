package mg.didavid.firsttry.Controllers.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import mg.didavid.firsttry.Controllers.Adapteurs.AdapteursPost;
import mg.didavid.firsttry.Models.ModelePost;
import mg.didavid.firsttry.R;
import mg.didavid.firsttry.Views.AppMode;

public class OtherUsersProfileActivity extends AppMode {

    TextView textView_displayLastname, textView_email;

    ImageView imageView_photoDeProfile;

    FirebaseFirestore firestore;
    CollectionReference collectionUsers, collectioonPost; // Firestore's collection reference : root/reference
    DocumentReference docRefProfileUser; // reference of the document in Firestoer : root/reference/document
    StorageReference storageReference; // reference of the Firebase storage
    ProgressDialog progressDialog_loadingProfile;

    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    List<ModelePost> modelePosts_profile;
    AdapteursPost adapteursPost_profile;
    RecyclerView profile_recyclerView;
    String user_id;
    String lastName;
    String pseudo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_users_profile);

        //recuperation des vues
        textView_displayLastname = findViewById(R.id.texteView_otherLastname_profile);
        textView_email = findViewById(R.id.texteView_otherPseudo_profile);
        imageView_photoDeProfile = findViewById(R.id.imageView_otherImageProfile_profile);
        profile_recyclerView = findViewById(R.id.recyclerView_otherPost);

        //init progressDialog
        progressDialog_loadingProfile = new ProgressDialog(this);
        progressDialog_loadingProfile.setMessage("Chargement des informations ...");
        progressDialog_loadingProfile.show();

        firestore = FirebaseFirestore.getInstance();
        collectionUsers = firestore.collection("Users");
        storageReference = FirebaseStorage.getInstance().getReference();
        collectioonPost = FirebaseFirestore.getInstance().collection("Publications");

        //linear layout for recyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(OtherUsersProfileActivity.this);
        //show newest post first (the newest post is in the last of the post list store on th database)
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set this layout to recyclerView
        profile_recyclerView.setLayoutManager(layoutManager);
        modelePosts_profile = new ArrayList<>();

        //get data from intent
        Intent intent_userId = getIntent();
        user_id = intent_userId.getStringExtra("user_id");

        if (user_id != null) {
            checkingUserInfo();
            loadUserPost();
        }

    }

    private void loadUserPost() {
        CollectionReference reference_post = FirebaseFirestore.getInstance().collection("Publications");
        reference_post.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                modelePosts_profile.clear();
                                List<ModelePost> modelePost = queryDocumentSnapshots.toObjects(ModelePost.class);
                                int size = modelePost.size();
                                for (int i = 0; i < size; i++) {
                                    if (modelePost.get(i).getUser_id().equals(user_id)) {
                                        modelePosts_profile.add(modelePost.get(i));
                                    }
                                }
                                //adapter
                                adapteursPost_profile = new AdapteursPost(OtherUsersProfileActivity.this, modelePosts_profile);
                                //set adapter to recyclerView
                                profile_recyclerView.setAdapter(adapteursPost_profile);
                                progressDialog_loadingProfile.dismiss();
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(OtherUsersProfileActivity.this, ""+ e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkConnexion();
        checkingUserInfo();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        checkConnexion();
        checkingUserInfo();
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
    private void checkingUserInfo() {
        docRefProfileUser = collectionUsers.document(user_id);
        docRefProfileUser.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    docRefProfileUser.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                            lastName = value.getString("name");
                            pseudo = value.getString("pseudo");
                            final String photoDeProfile = value.getString("profile_image");

                            //configure toolbar
                            configureToolbar(lastName, pseudo);

                            //setting data from Firestore
                            setData(lastName, pseudo, photoDeProfile);

                            //profile image clicked
                            imageView_photoDeProfile.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    goToShowImage(photoDeProfile);
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
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(OtherUsersProfileActivity.this, "il y a eu une erreur lors de la verification des informations", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //setting user's data from Firestore
    private void setData(String lastName, String pseudo, String photoDeProfile) {
        textView_displayLastname.setText(lastName);
        textView_email.setText(pseudo);
        try {
            Picasso.get().load(photoDeProfile).placeholder(R.drawable.ic_image_profile_icon_dark).into(imageView_photoDeProfile);
        }catch (Exception e){
            Picasso.get().load(R.drawable.ic_image_profile_icon_dark).into(imageView_photoDeProfile);
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void goToShowImage(String imageUri) {
        Intent intent = new Intent(OtherUsersProfileActivity.this, ShowImageActivity.class);
        intent.putExtra("showImage", imageUri);
        Log.d("valeur de image uri", " :" + imageUri);
        startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void configureToolbar(String name_resto, String phone_resto){
        // Get the toolbar view inside the activity layout
        Toolbar toolbar = findViewById(R.id.toolbar_otherUsersProfil);
        if (toolbar != null){
            //set toolbar title
            // Sets the Toolbar
            setSupportActionBar(toolbar);
            this.setTitle(name_resto);
            toolbar.setSubtitle(phone_resto);
            toolbar.setTitleTextAppearance(this, R.style.toolBarOtherUsers);
        }else {
            Toast.makeText(this, "Tsy misy titre :-(", Toast.LENGTH_SHORT).show();
        }
    }

    //inflate option menu
    @Override
    public boolean onCreateOptionsMenu(@NonNull final Menu menu) {
        //inflate menu
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
                    loadUserPost();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //called as and when user press any lettre
                if (!TextUtils.isEmpty(newText)){
                    searchPost(newText);
                }else {
                    loadUserPost();
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
                        if (modelePost.get(i).getPost_description().toLowerCase().contains(query.toLowerCase()) && modelePost.get(i).getUser_id().equals(user_id)) {
                            modelePosts_profile.add(modelePost.get(i));
                        }
                    }
                    //adapter
                    adapteursPost_profile = new AdapteursPost(OtherUsersProfileActivity.this, modelePosts_profile);
                    //set adapter to recyclerView
                    profile_recyclerView.setAdapter(adapteursPost_profile);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(OtherUsersProfileActivity.this, ""+e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
