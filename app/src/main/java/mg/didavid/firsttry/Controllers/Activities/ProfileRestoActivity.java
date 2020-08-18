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
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import mg.didavid.firsttry.Controllers.Adapteurs.AdapteursPost;
import mg.didavid.firsttry.Models.ModelComment;
import mg.didavid.firsttry.Models.ModelePost;
import mg.didavid.firsttry.R;

public class ProfileRestoActivity extends AppCompatActivity {

    TextView textView_restoName, textView_restoPhone;
    String resto_name, value_name, phone_resto;
    final Map<String, Object> logoRestoChanged = new HashMap<>();
    final Map<String, Object> nameChanged_result = new HashMap<>();

    ImageView imageView_logoResto;
    FloatingActionButton floatingActionButton_editProfileResto;
    Button btnAddLogo, btnNewPost;

    FirebaseFirestore firestore;
    CollectionReference collectionResto, collectioonPost, collectionComment; // Firestore's collection reference : root/reference
    DocumentReference docRefProfileResto; // reference of the document in Firestoer : root/reference/document
    StorageReference storageReference; // reference of the Firebase storage
    String storagePdPPath = "LogoResto/"; // storageReference/LogoResto/
    ProgressDialog progressDialog_editProfile, progressDialog_loadingProfile;

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

    ActionBar actionBar;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resto_profile);

        //recuperation des vues
        textView_restoName = findViewById(R.id.texteView_nameResto_restoProfile);
        textView_restoPhone = findViewById(R.id.texteView_email_restoProfile);
        imageView_logoResto = findViewById(R.id.imageView_logoResto_restoProfile);
        floatingActionButton_editProfileResto = findViewById(R.id.floating_btn_editProfil_restoProfile);
        btnAddLogo = findViewById(R.id.add_profile_photo_restoProfile);
        btnNewPost = findViewById(R.id.button_newPost_restoProfile);

        //init progressDialog
        progressDialog_editProfile = new ProgressDialog(this);
        progressDialog_editProfile.setMessage("Mises à jour de vos informations...");
        progressDialog_loadingProfile = new ProgressDialog(this);
        progressDialog_loadingProfile.setMessage("Chargement de vos informations ...");
        progressDialog_loadingProfile.show();

        firestore = FirebaseFirestore.getInstance();
        collectionResto = firestore.collection("Resto");
        storageReference = FirebaseStorage.getInstance().getReference();

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

        btnAddLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //modifier photo de profile
                progressDialog_editProfile.setMessage("Importation de la photo de profile");
                showImagePicDialog();
            }
        });

        btnNewPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileRestoActivity.this, NewPostActivity.class));
                finish();
            }
        });

        checkingRestoInfo();
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

    //check if user has already informations
    private void checkingRestoInfo() {
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
                            resto_name = value.getString("name_resto");
                            phone_resto = value.getString("phone_resto");
                            final String logo_resto = value.getString("logo_resto");

                            //configure toolbar
                            configureToolbar(resto_name, phone_resto);

                            //setting data from Firestore
                            setData(resto_name, phone_resto, logo_resto);

                            //profile image clicked
                            imageView_logoResto.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    goToShowImage(logo_resto);
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
                Toast.makeText(ProfileRestoActivity.this, "il y a eu une erreur lors de la verification de vos informations", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //setting user's data from Firestore
    private void setData(String resto_name, String phone_resto, String logo_resto) {
        textView_restoName.setText(resto_name);
        textView_restoPhone.setText(phone_resto);
        try {
            Picasso.get().load(logo_resto).placeholder(R.drawable.ic_image_profile_icon_dark).into(imageView_logoResto);
        }catch (Exception e){
            Picasso.get().load(R.drawable.ic_image_profile_icon_dark).into(imageView_logoResto);
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
        String filePathAndName = "LogoResto/" + "logoResto_" + user_id; //nom de l'image

        //storing imagge to Firabase Storage
        StorageReference storageReference1 = storageReference.child(filePathAndName);
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
                        String downloadUri = uriTask.getResult().toString();

                        //verifier si l'image est téléversée ou pas et que l'url est bien reçu
                        if (uriTask.isSuccessful()){
                            //update profile image
                            logoRestoChanged.put("profile_image", downloadUri);
                            docRefProfileResto.update(logoRestoChanged)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            progressDialog_editProfile.dismiss();
                                            Toast.makeText(ProfileRestoActivity.this, "Photo de profile mise à jour", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog_editProfile.dismiss();
                                    Log.d("message important", "******************************" +e.getMessage());
                                    Toast.makeText(ProfileRestoActivity.this, "Erreur lors de la mise à jour", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }else {
                            progressDialog_editProfile.dismiss();
                            Toast.makeText(ProfileRestoActivity.this, "Une erreur est survenue!", Toast.LENGTH_SHORT).show();
                        }
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
        String[] options = {"Changer le logo de votre restaurant", "Modifier le nom de votre restaurant"};
        //constructin de l'alert dialogue
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Option de modification");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0){
                    //modifier photo de profile
                    progressDialog_editProfile.setMessage("Changement du logo de votre restaurant");
                    showImagePicDialog();
                }
                if (which == 1){
                    //modifier le nom de l'user
                    progressDialog_editProfile.setMessage("Changement du nom de votre restaurant");
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
                //called when user press search button
                if (!TextUtils.isEmpty(query)){
                    //searchPost(query);
                }else {
                    //loadMyPost();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //called as and when user press any lettre
                if (!TextUtils.isEmpty(newText)){
                    //searchPost(newText);
                }else {
                    //loadMyPost();
                }
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
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
