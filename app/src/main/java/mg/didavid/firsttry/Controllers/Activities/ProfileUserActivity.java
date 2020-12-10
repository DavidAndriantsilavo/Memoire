package mg.didavid.firsttry.Controllers.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
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
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import mg.didavid.firsttry.Controllers.Adapteurs.AdapteursPost;
import mg.didavid.firsttry.Models.ModelComment;
import mg.didavid.firsttry.Models.ModelePost;
import mg.didavid.firsttry.Models.LocationService;
import mg.didavid.firsttry.Models.User;
import mg.didavid.firsttry.Models.UserSingleton;
import mg.didavid.firsttry.R;
import mg.didavid.firsttry.Utils.AppointmentNotificationDialog;
import mg.didavid.firsttry.Utils.SelectUserDialog;
import mg.didavid.firsttry.Views.AppMode;

public class ProfileUserActivity extends AppMode implements AppointmentNotificationDialog.AppointmentNotificationDialogListner {

    TextView textView_displayLastname, textView_email;
    String user_name, value_name;
    final Map<String, Object> profileImageChanged_result = new HashMap<>();
    final Map<String, Object> nameChanged_result = new HashMap<>();

    ImageView imageView_photoDeProfile;
    FloatingActionButton floatingActionButton_editProfile;
    Button btnAddProfileImage, btnNewPost;
    private Button button_appointment, button_favorite;

    FirebaseFirestore firestore;
    CollectionReference collectionUsers, collectioonPost, collectionComment; // Firestore's collection reference : root/reference
    DocumentReference docRefProfileUser; // reference of the document in Firestoer : root/reference/document
    StorageReference storageReference; // reference of the Firebase storage
    String storagePdPPath = "UsersPhotoDeProfie/"; // storageReference/UsersPhotoDeProfile/
    ProgressDialog progressDialog_editProfile, progressDialog_del_account, progressDialog_logout, progressDialog_loadingProfile;

    User currentUser;

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_REQUEST_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_REQUEST_CODE = 400;

    String [] cameraPermission;
    String [] storagePermission;
    Uri image_uri = null;
    Uri imageCompressed_uri = null;

    List<ModelePost> modelePosts_profile;
    AdapteursPost adapteursPost_profile;
    RecyclerView profile_recyclerView;
    String user_id;

    ArrayList<String> notificationList;

    private DatabaseReference mNotificationReference = FirebaseDatabase.getInstance().getReference().child("notification");
    private String TAG = "ProfileUserActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        currentUser = ((UserSingleton) getApplicationContext()).getUser();
        user_id = currentUser.getUser_id();

        //set tool bar
        Toolbar toolbar = findViewById(R.id.toolbar_userProfile);
        if (toolbar != null){
            //set toolbar title
            // Sets the Toolbar
            setSupportActionBar(toolbar);
            this.setTitle("Mon profil");

            // Get a support ActionBar corresponding to this toolbar
            ActionBar ab = getSupportActionBar();
            // Enable the Up button
            ab.setDisplayHomeAsUpEnabled(true);
        }

        //recuperation des vues
        textView_displayLastname = findViewById(R.id.texteView_lastname_profile);
        textView_email = findViewById(R.id.texteView_email_profile);
        imageView_photoDeProfile = findViewById(R.id.imageView_photoDeProfile_profile);
        floatingActionButton_editProfile = findViewById(R.id.floating_btn_editProfil);
        btnAddProfileImage = findViewById(R.id.add_profile_photo_profile);
        profile_recyclerView = findViewById(R.id.recyclerView_post);
        btnNewPost = findViewById(R.id.button_newPost_profile);
        button_appointment = findViewById(R.id.button_appointment);
//        button_favorite = findViewById(R.id.button_favorite);

        notificationList = new ArrayList<>();

//        button_appointment.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_red_dot, 0);
//        button_appointment.setCompoundDrawablePadding(10);

        //init progressDialog
        progressDialog_del_account = new ProgressDialog(this);
        progressDialog_del_account.setTitle("Suppression de votre compte");
        progressDialog_del_account.setMessage("Cela peut prendre quelques minutes, veuillez patienter !");
        progressDialog_del_account.setCanceledOnTouchOutside(false);
        progressDialog_del_account.setCancelable(false);
        progressDialog_logout = new ProgressDialog(this);
        progressDialog_logout.setMessage("Déconnexion...");
        progressDialog_editProfile = new ProgressDialog(this);
        progressDialog_editProfile.setMessage("Mises à jour de vos informations...");
        progressDialog_loadingProfile = new ProgressDialog(this);
        progressDialog_loadingProfile.setMessage("Chargement de vos informations ...");
        progressDialog_loadingProfile.show();

        firestore = FirebaseFirestore.getInstance();
        collectionUsers = firestore.collection("Users");
        storageReference = FirebaseStorage.getInstance().getReference();
        collectioonPost = FirebaseFirestore.getInstance().collection("Publications");
        collectionComment = FirebaseFirestore.getInstance().collection("Comments");

        //linear layout for recyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(ProfileUserActivity.this);
        //show newest post first (the newest post is in the last of the post list store on th database)
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set this layout to recyclerView
        profile_recyclerView.setLayoutManager(layoutManager);
        modelePosts_profile = new ArrayList<>();

        //init array of permissions
        cameraPermission = new String[] {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE};

        floatingActionButton_editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogEditProfile();
            }
        });

        btnAddProfileImage.setOnClickListener(new View.OnClickListener() {
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
                Intent intent = new Intent(ProfileUserActivity.this, NewPostActivity.class);
                intent.putExtra("key", "user_profile");
                startActivity(intent);
                finish();
            }
        });

//        button_appointment.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //startActivity(new Intent(ProfileUserActivity.this, AppointmentListActivity.class));
//                mNotificationReference.child("appointment").child(user_id).child("hasNews").setValue(false);
//
//                AppointmentNotificationDialog appointmentNotificationDialog = new AppointmentNotificationDialog(ProfileUserActivity.this, notificationList);
//                appointmentNotificationDialog.show(getSupportFragmentManager(), "appointment Notification Dialog");
//            }
//        });

        //Check for new appointment notification
        //if yes, show a dialog showing those notifications
        //if not go to appointmentListActivity
        mNotificationReference.child("appointment").child(user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Map<String, Object> map = (Map<String, Object>) snapshot.getValue();
                    if((Boolean)map.get("hasNews")){
                        addAppointmentBadge();

                        map.remove("hasNews");

                        notificationList.clear();

                        Iterator iterator = map.keySet().iterator();
                        while(iterator.hasNext()) {
                            String key = (String)iterator.next();
                            String value = (String)map.get(key);

                            notificationList.add(value);
                        }

                        button_appointment.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //startActivity(new Intent(ProfileUserActivity.this, AppointmentListActivity.class));
//                                mNotificationReference.child("appointment").child(user_id).child("hasNews").setValue(false);

                                AppointmentNotificationDialog appointmentNotificationDialog = new AppointmentNotificationDialog(ProfileUserActivity.this, notificationList);
                                appointmentNotificationDialog.show(getSupportFragmentManager(), "appointment Notification Dialog");
                            }
                        });

                    }else{
                        button_appointment.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(ProfileUserActivity.this, AppointmentListActivity.class));
                            }
                        });

                        removeBadge();
                    }
                }else{
                    button_appointment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(ProfileUserActivity.this, AppointmentListActivity.class));
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        checkingUserInfo();
        loadMyPost();

        snapshootUserInfo();
    }

    @Override
    public void startAppointmentListActivity() {
        HashMap<String, Boolean> map = new HashMap<>();
        map.put("hasNews", false);
        mNotificationReference.child("appointment").child(user_id).setValue(map);

        startActivity(new Intent(ProfileUserActivity.this, AppointmentListActivity.class));
    }

    private void addAppointmentBadge(){
        button_appointment.setTextColor(Color.RED);
        button_appointment.setTextSize(13);
    }

    private void removeBadge(){
        button_appointment.setTextColor(Color.BLACK);
        button_appointment.setTextSize(11);
    }

    private void snapshootUserInfo() {
        docRefProfileUser.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value != null) {
                    String imagegProfile = value.getString("profile_image");
                    if (imagegProfile != null || !imagegProfile.isEmpty()){
                        uploadProfileImageEverywhere(profileImageChanged_result);
                    }
                    String name = value.getString("name");
                    if (name != null || !name.isEmpty()){
                        uploadUserNameEverywhere(nameChanged_result);
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
                                    if (modelePost.get(i).getUser_id().equals(user_id)) {
                                        String post_id = modelePost.get(i).getPost_id();
                                        collectioonPost.document(post_id).update(nameChanged_result);
                                    }
                                }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ProfileUserActivity.this, "Update pdp_post failed !\n"+e.getMessage(), Toast.LENGTH_LONG).show();
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
                                    if (modelComments.get(i).getUser_id().equals(user_id)) {
                                        String comment_id = modelComments.get(i).getComment_time();
                                        collectionComment.document(comment_id).update(nameChanged_result);
                                    }
                                }
                        }
                    }
                });
    }

    private void goToShowImage(String imageUri) {
        Intent intent = new Intent(ProfileUserActivity.this, ShowImageActivity.class);
        intent.putExtra("showImage", imageUri);
        Log.d("valeur de image uri", " :" + imageUri);
        startActivity(intent);
    }

    private void loadMyPost() {
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
                                    if (modelePost.get(i).getUser_id().equals(user_id)) {
                                        modelePosts_profile.add(modelePost.get(i));
                                    }
                                }
                                //adapter
                                adapteursPost_profile = new AdapteursPost(ProfileUserActivity.this, modelePosts_profile);
                                //set adapter to recyclerView
                                profile_recyclerView.setAdapter(adapteursPost_profile);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileUserActivity.this, ""+ e.getMessage(), Toast.LENGTH_LONG).show();
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

    //check if currentUser has already informations
    private void checkingUserInfo() {
        progressDialog_loadingProfile.show();
        docRefProfileUser = collectionUsers.document(user_id);
        docRefProfileUser.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    docRefProfileUser.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                            user_name = value.getString("name");
                            String pseudo = value.getString("pseudo");
                            final String photoDeProfile = value.getString("profile_image");

                            //setting data from Firestore
                            setData(user_name, pseudo, photoDeProfile);

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
                    progressDialog_loadingProfile.dismiss();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileUserActivity.this, "il y a eu une erreur lors de la verification de vos informations", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //setting currentUser's data from Firestore
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
        String filePathAndName = storagePdPPath + timestamp + "_profile_image" + "_" + user_id; //nom de l'image

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
                        }
                        String downloadUri = uriTask.getResult().toString();

                        //verifier si l'image est téléversée ou pas et que l'url est bien reçu
                        if (uriTask.isSuccessful()){
                            //update profile image
                            profileImageChanged_result.put("profile_image", downloadUri);
                            docRefProfileUser.update(profileImageChanged_result)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            uploadProfileImageEverywhere(profileImageChanged_result);
                                            progressDialog_editProfile.dismiss();
                                            Toast.makeText(ProfileUserActivity.this, "Photo de profile mise à jour", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressDialog_editProfile.dismiss();
                                            Log.d("message important", "******************************" +e.getMessage());
                                            Toast.makeText(ProfileUserActivity.this, "Erreur lors de la mise à jour", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }else {
                            progressDialog_editProfile.dismiss();
                            Toast.makeText(ProfileUserActivity.this, "Une erreur est survenue!", Toast.LENGTH_SHORT).show();
                        }
                        progressDialog_editProfile.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("message important", "******************************" +e.getMessage());
                progressDialog_editProfile.dismiss();
                Toast.makeText(ProfileUserActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
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
                                    if (modelePost.get(i).getUser_id().equals(user_id)) {
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
                        Toast.makeText(ProfileUserActivity.this, "Update pdp_post failed !\n"+e.getMessage(), Toast.LENGTH_LONG).show();
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
                                    if (modelComments.get(i).getUser_id().equals(user_id)) {
                                        String comment_id = modelComments.get(i).getComment_time(); //comment_time is the id of the comment
                                        collectionComment.document(comment_id).update(result);
                                    }
                                }
                        }
                    }
                });
    }

    private void showDialogEditProfile() {
        String[] options = {"Changer la photo de profile", "Modifier votre nom"};
        //constructin de l'alert dialogue
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Option de modification");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0){
                    //modifier photo de profile
                    progressDialog_editProfile.setMessage("Edition de la photo de profile");
                    showImagePicDialog();
                }
                if (which == 1){
                    //modifier le nom de l'currentUser
                    progressDialog_editProfile.setMessage("Edition de votre nom");
                    showNameUpdateDialog("nom");
                }
            }
        });
        builder.create().show();
    }

    private void showNameUpdateDialog(final String key) {
        //custom dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Modifier votre " + key);
        //set layout of dialog
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);
        //add EditText
        final EditText editText = new EditText(this);
        editText.setText(user_name);
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
                    Toast.makeText(ProfileUserActivity.this, "Veillez entre votre nouveau " + key, Toast.LENGTH_SHORT).show();
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

        docRefProfileUser = collectionUsers.document(user_id);
        docRefProfileUser.update(nameChanged_result)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        uploadUserNameEverywhere(nameChanged_result);
                        progressDialog_editProfile.dismiss();
                        Toast.makeText(ProfileUserActivity.this, "Nom mis à jour", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog_editProfile.dismiss();
                Toast.makeText(ProfileUserActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    @Override
    public boolean onCreateOptionsMenu(@NonNull final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_logout_et_deleteprofile_profile, menu);
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);

        //hide others menu
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
                    searchPost(query);
                }else {
                    loadMyPost();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //called as and when currentUser press any lettre
                if (!TextUtils.isEmpty(newText)){
                    searchPost(newText);
                }else {
                    loadMyPost();
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
                    adapteursPost_profile = new AdapteursPost(ProfileUserActivity.this, modelePosts_profile);
                    //set adapter to recyclerView
                    profile_recyclerView.setAdapter(adapteursPost_profile);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileUserActivity.this, ""+e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //3 - Handle actions on menu items
        switch (item.getItemId()) {
            case R.id.menu_logout_profil:
                avertissement();
                return true;
            case R.id.menu_deleteprofile_profile:
                deleteAccount();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //supression du compteUser
    private void deleteAccount() {
        if(currentUser !=null)
        {
            //BUILD ALERT DIALOG TO CONFIRM THE SUPPRESSION
            AlertDialog.Builder builder = new AlertDialog.Builder(ProfileUserActivity.this);
            builder.setTitle("Attention !!");
            builder.setMessage("Etes-vous sûr de vouloir supprimer votre compte?\nCela entrainera la suppression de toutes vos données ainsi que ceux de votre restaurant si vous en avez.");
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
                                if (!documentSnapshot.getString("comment_image").equals("noImage") && documentSnapshot.getString("user_id").contains(user_id)) {
                                    FirebaseStorage.getInstance().getReferenceFromUrl(documentSnapshot.getString("comment_image")).delete();
                                }
                                if (documentSnapshot.getString("user_id").contains(user_id) && documentSnapshot.getString("comment_image").equals("noImage")){
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
                                if (documentSnapshot.getString("user_id").contains(user_id)) {
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
        FirebaseFirestore.getInstance().collection("HasRatingResto").document("resto_" + user_id).delete()
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
                                if (documentSnapshot.getString("id_resto").contains(user_id)) {
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
                                if (documentSnapshot.getString("id_resto").contains(user_id)) {
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
                                if (documentSnapshot.getString("id_resto").contains(user_id)) {
                                    FirebaseStorage.getInstance().getReferenceFromUrl(documentSnapshot.getString("logo_resto")).delete();
                                    documentSnapshot.getReference().delete();
                                }
                            }
                        }else {
                            deleteUsersInformations();
                        }
                        deleteResto();
                    }
                });
    }


    private void deleteUsersInformations() {
        collectionUsers.document(user_id).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (!documentSnapshot.exists()) {
                            FirebaseStorage.getInstance().getReferenceFromUrl(documentSnapshot.getString("profile_image")).delete()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            collectionUsers.document(user_id).delete()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    deleteUser();
                                                }
                                            });
                                        }
                                    });
                        }
                    }
                });
    }

    private void deleteUser() {
        FirebaseAuth.getInstance().getCurrentUser().delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Votre compte a été supprimé avec succès ...\nOn éspère vous revoire bientôt !", Toast.LENGTH_LONG).show();

                            logOut();
                            progressDialog_del_account.dismiss();

                            Intent logOut = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(logOut);
                            finish();
                        }
                    }
                });
    }

    private void avertissement() {
        if(currentUser !=null)
        {
            //BUILD ALERT DIALOG TO CONFIRM THE SUPPRESSION
            AlertDialog.Builder builder = new AlertDialog.Builder(ProfileUserActivity.this);
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
        progressDialog_logout.cancel();
        FirebaseAuth.getInstance().signOut();
        GoogleSignIn.getClient(
                this,
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        ).signOut();

        Intent logOut =  new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(logOut);

        stopService(new Intent(ProfileUserActivity.this, LocationService.class));

        MainActivity.stopActivity.finish();
        this.finish();
    }

}
