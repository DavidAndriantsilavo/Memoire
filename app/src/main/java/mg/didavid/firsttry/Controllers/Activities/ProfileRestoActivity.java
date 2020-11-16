package mg.didavid.firsttry.Controllers.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import com.google.firebase.storage.UploadTask;
import com.iceteck.silicompressorr.FileUtils;
import com.iceteck.silicompressorr.SiliCompressor;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import mg.didavid.firsttry.Controllers.Adapteurs.AdapterSampleMenu;
import mg.didavid.firsttry.Controllers.Adapteurs.AdapteursPost;
import mg.didavid.firsttry.Models.ModelComment;
import mg.didavid.firsttry.Models.ModelRestoSampleMenu;
import mg.didavid.firsttry.Models.ModelePost;
import mg.didavid.firsttry.R;
import mg.didavid.firsttry.Views.AppMode;

public class ProfileRestoActivity extends AppMode {

    String resto_name, value_name, speciality_resto, rating_resto, logo_resto;
    final Map<String, Object> imageChanged = new HashMap<>();
    final Map<String, Object> nameChanged_result = new HashMap<>();

    List<ModelePost> modelePosts_profile;
    AdapteursPost adapteursPost_profile;
    RecyclerView restoProfile_recyclerView;

    TextView textView_restoName;
    ImageView imageView_logoResto, imageView_coverPhoto;
    FloatingActionButton floatingActionButton_editProfileResto;
    Button btnAddLogo, btnNewPost;
    RecyclerView recyclerView_sampleMenu;
    TextView textView_showAllMenu;
    Button button_addCoverPhoto;
    ImageButton imageButton_fleche;
    RatingBar ratingBar;

    FirebaseFirestore firestore;
    CollectionReference collectionResto, collectioonPost, collectionComment; // Firestore's collection reference : root/reference
    DocumentReference docRefProfileResto; // reference of the document in Firestoer : root/reference/document
    StorageReference storageReference; // reference of the Firebase storage
    String storagePdPPath = "LogoResto/"; // storageReference/LogoResto/
    ProgressDialog progressDialog_editProfile, progressDialog_loadingProfile, progressDialog_del_account;

    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_REQUEST_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_REQUEST_CODE = 400;

    String [] cameraPermission;
    String [] storagePermission;
    Uri image_uri = null;
    Uri imageCompressed_uri = null;

    String user_id = Objects.requireNonNull(user).getUid();
    String id_resto = "resto_" + user_id;

    boolean addLogoImageClicked = false, addCoverPhotoClicked = false;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resto_profile);

        //recuperation des vues
        textView_restoName = findViewById(R.id.texteView_nameResto_restoProfile);
        imageView_logoResto = findViewById(R.id.imageView_logoResto_restoProfile);
        imageView_coverPhoto = findViewById(R.id.imageView_photoDeCouverture_restoProfile);
        floatingActionButton_editProfileResto = findViewById(R.id.floating_btn_editProfil_restoProfile);
        btnAddLogo = findViewById(R.id.add_profile_photo_restoProfile);
        btnNewPost = findViewById(R.id.button_newPost_restoProfile);
        button_addCoverPhoto = findViewById(R.id.add_cover_photo_restoProfile);
        textView_showAllMenu = findViewById(R.id.textView_showAllMenuList_restoProfile);
        recyclerView_sampleMenu = findViewById(R.id.recyclerView_sampleMenu_restoProfile);
        restoProfile_recyclerView = findViewById(R.id.recyclerView_post_restoProfile);
        imageButton_fleche = findViewById(R.id.flecheBtn_restoProfile);
        ratingBar = findViewById(R.id.ratingBar_restoProfile);

        //linear layout for recyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(ProfileRestoActivity.this);
        //show newest post first (the newest post is in the last of the post list store on th database)
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set this layout to recyclerView
        restoProfile_recyclerView.setLayoutManager(layoutManager);
        modelePosts_profile = new ArrayList<>();

        //init progressDialog
        progressDialog_editProfile = new ProgressDialog(this);
        progressDialog_editProfile.setMessage("Mises à jour de vos informations...");
        progressDialog_loadingProfile = new ProgressDialog(this);
        progressDialog_loadingProfile.setMessage("Chargement de vos informations ...");
        progressDialog_loadingProfile.show();
        progressDialog_del_account = new ProgressDialog(this);
        progressDialog_del_account.setTitle("Suppression de votre compte");
        progressDialog_del_account.setMessage("Cela peut prendre quelques minutes, veuillez patienter !");
        progressDialog_del_account.setCanceledOnTouchOutside(false);
        progressDialog_del_account.setCancelable(false);

        firestore = FirebaseFirestore.getInstance();
        collectionResto = firestore.collection("Resto");
        storageReference = FirebaseStorage.getInstance().getReference();
        docRefProfileResto = collectionResto.document(id_resto);

        collectioonPost = FirebaseFirestore.getInstance().collection("Publications");
        collectionComment = FirebaseFirestore.getInstance().collection("Comments");

        //init array of permissions
        cameraPermission = new String[] {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE};

        floatingActionButton_editProfileResto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogEditProfile();
            }
        });

        button_addCoverPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCoverPhotoClicked = true;
                showImagePicDialog();
            }
        });

        textView_showAllMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileRestoActivity.this, ListMenuRestoActivity.class);
                intent.putExtra("key", id_resto);
                startActivity(intent);
            }
        });
        imageButton_fleche.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileRestoActivity.this, ListMenuRestoActivity.class);
                intent.putExtra("key", id_resto);
                startActivity(intent);
            }
        });

        btnAddLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //modifier photo de profile
                addLogoImageClicked = true;
                showImagePicDialog();
            }
        });

        btnNewPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileRestoActivity.this, NewPostActivity.class);
                intent.putExtra("key", "resto");
                intent.putExtra("name", resto_name);
                intent.putExtra("pseudo", rating_resto);
                intent.putExtra("user_id", id_resto);
                intent.putExtra("logo_resto", logo_resto);
                startActivity(intent);
                finish();
            }
        });

        checkingRestoInfo();
        loadSampleMenu();
        loadMyRestoPost();
        snapshootUserInfo();
    }

    private void snapshootUserInfo() {
        docRefProfileResto.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value != null) {
                    String logo_resto = value.getString("logo_resto");
                    HashMap<String, Object> logo = new HashMap<>();
                    logo.put("profile_image", logo_resto);
                    if (logo_resto != null || !logo_resto.isEmpty()){
                        uploadProfileImageEverywhere(logo);
                    }
                    String name_resto = value.getString("name_resto");
                    HashMap<String, Object> name = new HashMap<>();
                    name.put("name", name_resto);
                    if (name_resto != null || !name_resto.isEmpty()){
                        uploadUserNameEverywhere(name);
                    }

                    //configure toolbar
                    configureToolbar(name_resto, value.getString("speciality_resto"));
                }
            }
        });
    }

    private void uploadProfileImageEverywhere(final Map<String, Object> result) {
        //update profile image of all currentUser's posts
        collectioonPost.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()){
                                List<ModelePost> modelePost = queryDocumentSnapshots.toObjects(ModelePost.class);
                                int size = modelePost.size();
                                for (int i = 0; i < size; i++) {
                                    if (modelePost.get(i).getUser_id().equals(id_resto)) {
                                        String post_id = modelePost.get(i).getPost_id();
                                        collectioonPost.document(post_id).update(result);
                                    }
                                }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ProfileRestoActivity.this, "Update pdp_post failed !\n"+e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
        //update also profile image of all currentUser's comment on post
        collectionComment.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()){
                                List<ModelComment> modelComments = queryDocumentSnapshots.toObjects(ModelComment.class);
                                int size = modelComments.size();
                                for (int i = 0; i < size; i++) {
                                    if (modelComments.get(i).getUser_id().equals(id_resto)) {
                                        String comment_id = modelComments.get(i).getComment_time(); //comment_time is the id of the comment
                                        collectionComment.document(comment_id).update(result);
                                    }
                                }
                        }
                    }
                });
    }

    private void uploadUserNameEverywhere(final Map<String, Object> nameChanged_result) {
        //update also current currentUser name in all his publications
        collectioonPost.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()){
                                List<ModelePost> modelePost = queryDocumentSnapshots.toObjects(ModelePost.class);
                                int size = modelePost.size();
                                for (int i = 0; i < size; i++) {
                                    if (modelePost.get(i).getUser_id().equals(id_resto)) {
                                        collectioonPost.document(modelePost.get(i).getPost_id()).update(nameChanged_result);
                                    }
                                }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ProfileRestoActivity.this, "Update pdp_post failed !\n"+e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
        //update also current currentUser name on all comments whom he has commented
        collectionComment.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()){
                                List<ModelComment> modelComments = queryDocumentSnapshots.toObjects(ModelComment.class);
                                int size = modelComments.size();
                                for (int i = 0; i < size; i++) {
                                    if (modelComments.get(i).getUser_id().equals(id_resto)) {
                                        collectionComment.document(modelComments.get(i).getComment_time()).update(nameChanged_result);
                                    }
                                }
                        }
                    }
                });
    }

    private void loadMyRestoPost() {
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
                                adapteursPost_profile = new AdapteursPost(ProfileRestoActivity.this, modelePosts_profile);
                                //set adapter to recyclerView
                                restoProfile_recyclerView.setAdapter(adapteursPost_profile);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileRestoActivity.this, ""+ e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadSampleMenu() {

        final ArrayList<ModelRestoSampleMenu> restoSampleMenuArrayList = new ArrayList<>();

        final CollectionReference collectionReference_sampleMenu = FirebaseFirestore.getInstance().collection("Sample_menu");
        collectionReference_sampleMenu.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        collectionReference_sampleMenu
                                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                    if (!value.isEmpty()) {
                                            restoSampleMenuArrayList.clear();
                                            List<ModelRestoSampleMenu> restoSampleMenus = value.toObjects(ModelRestoSampleMenu.class);
                                            int size = restoSampleMenus.size();
                                            for (int i = 0; i < size; i++) {
                                                if (restoSampleMenus.get(i).getId_resto().contains(id_resto)) {
                                                    restoSampleMenuArrayList.add(restoSampleMenus.get(i));
                                                }
                                            }

                                        AdapterSampleMenu adapterSampleMenu = new AdapterSampleMenu(ProfileRestoActivity.this, restoSampleMenuArrayList);
                                        recyclerView_sampleMenu.setLayoutManager(new LinearLayoutManager(ProfileRestoActivity.this, LinearLayoutManager.HORIZONTAL, false));
                                        recyclerView_sampleMenu.setAdapter(adapterSampleMenu);
                                    }
                                }
                            });
                    }
                });
    }

    private void goToShowImage(String imageUri) {
        Intent intent = new Intent(ProfileRestoActivity.this, ShowImageActivity.class);
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

    //check if currentUser has already informations
    private void checkingRestoInfo() {
        progressDialog_loadingProfile.show();
        docRefProfileResto.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    docRefProfileResto.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                            resto_name = value.getString("name_resto");
                            speciality_resto = value.getString("speciality_resto");
                            rating_resto = value.getString("rating_resto");
                            logo_resto = value.getString("logo_resto");
                            final String coverPhoto_resto = value.getString("coverPhoto_resto");

                            //configure toolbar
                            configureToolbar(resto_name, speciality_resto);

                            //setting data from Firestore
                            setData(resto_name, speciality_resto, logo_resto, coverPhoto_resto, rating_resto);

                            //profile image clicked
                            if (!logo_resto.equals("noImage")) {
                                imageView_logoResto.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        goToShowImage(logo_resto);
                                    }
                                });
                            }
                            //cover photo clicked
                            if (coverPhoto_resto != null && !coverPhoto_resto.equals("noImage")) {
                                imageView_coverPhoto.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        goToShowImage(coverPhoto_resto);
                                    }
                                });
                            }
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
                Toast.makeText(ProfileRestoActivity.this, "il y a eu une erreur lors de la verification de vos informations", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //setting currentUser's data from Firestore
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


    private boolean checkCameraPermission(){
        //verifier si on est autorisé ou pas
        //retourne true si on est permis et false si non
        boolean result = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this,cameraPermission, CAMERA_REQUEST_CODE);
    }


    private boolean checkStoragePermission(){
        //verifier si on est autorisé ou pas
        //retourne true si on est permis et false si non
        boolean result = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE);
    }

    //resultat : si on a accès ou pas à la camera et/ou au stockage de l'appareil
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_REQUEST_CODE: { //source camera selectionnée
                if (grantResults.length > 0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted){
                        pickFromCamera();
                    }else {
                        Toast.makeText(this, "veillez activer la permission à acceder à la camera et au stockage", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;

            case STORAGE_REQUEST_CODE: { //source Gallerie selectionnée
                if (grantResults.length > 0){
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted){
                        pickFromGallery();
                    }else {
                        Toast.makeText(this, "veillez activer la permission à accederau stockage", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
    }

    private void pickFromCamera() {
        //intent of picking image from device camera
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");
        //put image uri
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        //intent to start camera
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_REQUEST_CODE);
    }

    private void pickFromGallery() {
        //pick from gallery
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_REQUEST_CODE);
    }

    //méthode appelée après avoir prise une image dans la gallerie ou via la camera
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            if (requestCode == IMAGE_PICK_GALLERY_REQUEST_CODE){
                image_uri = data.getData();
                imageCompressed_uri = compressedAndSetImage();
                uploadProfileImage(imageCompressed_uri);
            }
            if (requestCode == IMAGE_PICK_CAMERA_REQUEST_CODE){
                imageCompressed_uri = compressedAndSetImage();
                uploadProfileImage(imageCompressed_uri);
            }
        }
    }

    //compression de l'image
    private Uri compressedAndSetImage() {
        Uri compressedImage = null;
        if (image_uri != null){
            File file = new File(SiliCompressor.with(this)
                    .compress(FileUtils.getPath(this, image_uri), new File(this.getCacheDir(), "temp")));
            compressedImage = Uri.fromFile(file);
        }
        return compressedImage;
    }

    private void uploadProfileImage(Uri uri) {
        progressDialog_editProfile.show();
        final String timestamp = String.valueOf(System.currentTimeMillis());
        String filePathAndName = "", key = "";
        if (addLogoImageClicked) {
            //update logo_resto
            filePathAndName = "LogoResto/" + "logoResto_" + user_id + "_" + timestamp;
            key = "logo_resto";
            addLogoImageClicked = false;
        } else if (addCoverPhotoClicked) {
            //update coverPhoto_resto
            filePathAndName = "CoverPhoto_resto/" + "coverPhoto_" + user_id + "_" + timestamp;
            key = "coverPhoto_resto";
            addCoverPhotoClicked = false;
        }

        //storing imagge to Firabase Storage
        StorageReference storageReference1 = storageReference.child(filePathAndName);
        final String finalKey = key;
        storageReference1.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @SuppressLint("LongLogTag")
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()){
                            Log.d("Messege importaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaannnnnnnnnnnnnnnnnnnnt", "mbola tsy succès le tache !");
                            Toast.makeText(ProfileRestoActivity.this, "mbola tsy succès le tache !", Toast.LENGTH_SHORT).show();
                        }
                        //verifier si l'image est téléversée ou pas et que l'url est bien reçu
                        uriTask.isSuccessful();
                        String downloadUri = uriTask.getResult().toString();
                        imageChanged.put(finalKey, downloadUri);
                        docRefProfileResto.update(imageChanged)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        uploadProfileImageEverywhere(imageChanged);
                                        progressDialog_editProfile.dismiss();
                                        Toast.makeText(ProfileRestoActivity.this, "Photo mise à jour", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog_editProfile.dismiss();
                                Log.d("message important", "******************************" +e.getMessage());
                                Toast.makeText(ProfileRestoActivity.this, "Erreur lors de la mise à jour", Toast.LENGTH_SHORT).show();
                            }
                        });
                        progressDialog_editProfile.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("message important", "******************************" +e.getMessage());
                progressDialog_editProfile.dismiss();
                Toast.makeText(ProfileRestoActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDialogEditProfile() {
        String[] options = {"Changer le logo de votre restaurant", "Changer la photo de couverture", "Modifier le nom de votre restaurant"};
        //constructin de l'alert dialogue
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Option de modification");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0){
                    //modifier le logo du restaurant
                    showImagePicDialog();
                    addLogoImageClicked = true;
                }
                if (which == 1){
                    //modifier la photo de couverture du restaurant
                    showImagePicDialog();
                    addCoverPhotoClicked = true;
                }
                if (which == 2){
                    //modifier le nom du restaurant
                    showNameUpdateDialog("name_resto");
                }
            }
        });
        builder.create().show();
    }

    private void showNameUpdateDialog(final String key) {
        //custom dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Modifier le nom de votre restaurant ");
        //set layout of dialog
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);
        //add EditText
        final EditText editText = new EditText(this);
        editText.setText(resto_name);
        linearLayout.addView(editText);

        builder.setView(linearLayout);

        //add button in dialog
        builder.setPositiveButton("Valider", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //get the input text
                value_name = editText.getText().toString();
                if (!TextUtils.isEmpty(value_name)){
                    uploadUserName(value_name, key);
                }else {
                    Toast.makeText(ProfileRestoActivity.this, "Veillez entre le nouveau nom de votre restaurant", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        //create and show dialog
        builder.create().show();

    }

    private void uploadUserName(String value, String key) {
        progressDialog_editProfile.show();
        nameChanged_result.put(key, value);

        docRefProfileResto = collectionResto.document(id_resto);
        docRefProfileResto.update(nameChanged_result)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        uploadUserNameEverywhere(nameChanged_result);
                        progressDialog_editProfile.dismiss();
                        Toast.makeText(ProfileRestoActivity.this, "Nom du restaurant mis à jour", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog_editProfile.dismiss();
                Toast.makeText(ProfileRestoActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showImagePicDialog() {
        String[] options = {"Prendre une photo", "Importer depuis la gallerie"};
        //constructin de l'alert dialogue
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Source de l'image");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0){
                    //Camera clicked
                    if (!checkCameraPermission()){
                        requestCameraPermission();
                    }else {
                        pickFromCamera();
                    }
                }
                if (which == 1){
                    //Gallery ckicked
                    if (!checkStoragePermission()){
                        requestStoragePermission();
                    }else {
                        pickFromGallery();
                    }
                }
            }
        });
        builder.create().show();
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
                //called when currentUser press search button
                if (!TextUtils.isEmpty(query)){
                    search(query);
                }else {
                    loadMyRestoPost();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //called as and when currentUser press any lettre
                if (!TextUtils.isEmpty(newText)){
                    search(newText);
                }else {
                    loadMyRestoPost();
                }
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    private void search(final String query) {
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
                    adapteursPost_profile = new AdapteursPost(ProfileRestoActivity.this, modelePosts_profile);
                    //set adapter to recyclerView
                    restoProfile_recyclerView.setAdapter(adapteursPost_profile);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileRestoActivity.this, ""+e.getMessage(), Toast.LENGTH_LONG).show();
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
        Toolbar toolbar = findViewById(R.id.toolbar_restoprofile);
        if (toolbar != null){
            //set toolbar title
            // Sets the Toolbar
            setSupportActionBar(toolbar);
            this.setTitle(name);
            toolbar.setSubtitle(pseudo);
            toolbar.setTitleTextAppearance(this, R.style.toolBarOtherUsers);

            // Get a support ActionBar corresponding to this toolbar
            ActionBar ab = getSupportActionBar();

            // Enable the Up button
            ab.setDisplayHomeAsUpEnabled(true);
        }else {
            Toast.makeText(this, "Tsy misy titre :-(", Toast.LENGTH_SHORT).show();
        }
    }


    //supression du compteUser
    private void deleteAccount() {
        if(user!=null)
        {
            //BUILD ALERT DIALOG TO CONFIRM THE SUPPRESSION
            AlertDialog.Builder builder = new AlertDialog.Builder(ProfileRestoActivity.this);
            builder.setTitle("Attention !!");
            builder.setMessage("Etes-vous sûr de vouloir supprimer votre compte restaurant?\nCela entrainera la suppression de toutes vos données sur votre restaurant.");
            builder.setCancelable(true);

            builder.setPositiveButton(
                    "SUPPRIMER",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            progressDialog_del_account.show();

                            deleteUsersComments();
                        }
                    });

            builder.setNegativeButton(
                    "Annuler",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            progressDialog_del_account.dismiss();

            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private boolean deleteUsersComments() {
        collectionComment.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()){
                                if (!documentSnapshot.getString("comment_image").equals("noImage") && documentSnapshot.getString("user_id").equals(id_resto)) {
                                    FirebaseStorage.getInstance().getReferenceFromUrl(documentSnapshot.getString("comment_image")).delete();
                                }
                                if (documentSnapshot.getString("user_id").equals(id_resto) && documentSnapshot.getString("comment_image").equals("noImage")){
                                    documentSnapshot.getReference().delete();
                                }
                            }
                        }else {
                            deleteUsersPosts();
                        }
                    }
                });
        return true;
    }

    private boolean deleteUsersPosts() {
        collectioonPost.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                if (documentSnapshot.getString("user_id").equals(id_resto)) {
                                    String post_id = documentSnapshot.getString("post_id");
                                    String post_image1 = documentSnapshot.getString("post_image1");
                                    String post_image2 = documentSnapshot.getString("post_image2");
                                    String post_image3 = documentSnapshot.getString("post_image3");
                                    beginDelete(post_id, post_image1, post_image2, post_image3);
                                }
                            }
                        }else {
                            deleteUsersResto();
                        }
                    }
                });
        return true;
    }
    private void beginDelete(String post_id, String post_image1, String post_image2, String post_image3) {
        //delete post image
        deletePostImage(post_image1);
        deletePostImage(post_image2);
        deletePostImage(post_image3);
        //delete post
        deletePost(post_id);
    }

    private void deletePostImage(final String post_image) {
        if (!post_image.equals("noImage")) {
            //we must delete image stored in Firebase storage
            //after that deleting post from Firestore
            StorageReference storagePickReference = FirebaseStorage.getInstance().getReferenceFromUrl(post_image);
            storagePickReference.delete();
        }
    }

    private void deletePost(final String post_id) {
        //delete data from Firestore
        DocumentReference documentReference = collectioonPost.document(post_id);
        documentReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //delete post's comment
                final CollectionReference documentReference1 = FirebaseFirestore.getInstance().collection("Comments");
                documentReference1.get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                if (!queryDocumentSnapshots.isEmpty()){
                                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()){
                                        if (documentSnapshot.getString("post_id").equals(post_id)){
                                            String comment_id = documentSnapshot.getString("comment_time");
                                            documentReference1.document(comment_id).delete();
                                        }
                                    }
                                }
                            }
                        });

                //delete post kiffs
                FirebaseFirestore.getInstance().collection("Kiffs").document(post_id).delete();

            }
        });
    }

    private void deleteUsersResto() {
        deleteHasRatingRresto();
    }

    private void deleteHasRatingRresto() {
        FirebaseFirestore.getInstance().collection("HasRatingResto").document(id_resto).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        deleteMenuList();
                    }
                });
    }

    private void deleteMenuList() {
        FirebaseFirestore.getInstance().collection("Menu_list").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                if (documentSnapshot.getString("id_resto").equals(id_resto)) {
                                    FirebaseStorage.getInstance().getReferenceFromUrl(documentSnapshot.getString("menuPhoto")).delete();
                                    documentSnapshot.getReference().delete();
                                }
                            }
                        }else {
                            deleteSampleMenu();
                        }
                        deleteMenuList();
                    }
                });
    }

    private void deleteSampleMenu() {
        FirebaseFirestore.getInstance().collection("Sample_menu").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                if (documentSnapshot.getString("id_resto").equals(id_resto)) {
                                    documentSnapshot.getReference().delete();
                                }
                            }
                        }else {
                            deleteResto();
                        }
                        deleteSampleMenu();
                    }
                });
    }

    private void deleteResto() {
        FirebaseFirestore.getInstance().collection("Resto").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                if (documentSnapshot.getString("id_resto").equals(id_resto)) {
                                    FirebaseStorage.getInstance().getReferenceFromUrl(documentSnapshot.getString("logo_resto")).delete();
                                    documentSnapshot.getReference().delete();
                                }
                            }
                        }else {
                            goToMainActivity();
                        }
                        deleteResto();
                    }
                });
    }

    private void goToMainActivity() {
        startActivity(new Intent(ProfileRestoActivity.this, MainActivity.class));
        finish();
    }


    @Override
    public boolean onNavigateUp() {
        onBackPressed();
        return super.onNavigateUp();
    }
}
