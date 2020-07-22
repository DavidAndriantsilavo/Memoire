package mg.didavid.firsttry.Controllers.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Toolbar;

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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

import mg.didavid.firsttry.R;

public class NewPostActivity extends AppCompatActivity {

    FirebaseAuth mCurrentUser;

    EditText postTitle, postDescription;
    ImageView imagePost;
    Button publishBtn, getImage;

    ProgressDialog progressDialog_uploadPost;


    FirebaseFirestore firestore;
    CollectionReference collectionUsers; // Firestore's collection reference : root/reference
    DocumentReference docRefProfileUser; // reference of the document in Firestoer : root/reference/document
    StorageReference storageReference; // reference of the Firebase storage

    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_REQUEST_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_REQUEST_CODE = 400;

    String [] cameraPermission;
    String [] storagePermission;
    Uri image_uri = null;

    String nomEtPrenonm, pseudo, uid, photoDeProfile;

    @SuppressLint({"ResourceType", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);



        checkConnexion();

        mCurrentUser = FirebaseAuth.getInstance();
        checkUserStatus();

        postTitle = findViewById(R.id.editText_inputTitlePost_newPost);
        postDescription = findViewById(R.id.editText_inputPostDescription_newPost);
        imagePost = findViewById(R.id.imageView_inputImage_newPost);
        publishBtn = findViewById(R.id.button_publish_post);
        getImage = findViewById(R.id.button_addImage_post);

        progressDialog_uploadPost = new ProgressDialog(this);

        firestore = FirebaseFirestore.getInstance();
        collectionUsers = firestore.collection("Users");
        storageReference = FirebaseStorage.getInstance().getReference();

        //get image from camera/gallery on click
        getImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkImagePickDialog();
            }
        });

        //valider et publier le nouveau post
        publishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = postTitle.getText().toString();
                String description = postDescription.getText().toString();

                if (image_uri == null) {
                    if (TextUtils.isEmpty(title)) {
                        Toast.makeText(NewPostActivity.this, "Veillez entrer un titre à votre publication", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (TextUtils.isEmpty(description)) {
                        Toast.makeText(NewPostActivity.this, "Veillez entrer une description à votre publication", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //post without image
                    uploadPost(title, description, "sansImage");
                }else {
                    if (TextUtils.isEmpty(title)) {
                        Toast.makeText(NewPostActivity.this, "Veillez entrer un titre à votre publication", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //post with image
                    uploadPost(title, description, String.valueOf(image_uri));
                }
            }
        });

        //get some info of current user to include in post
        uid = user.getUid();
        docRefProfileUser = collectionUsers.document(uid);
        docRefProfileUser.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    docRefProfileUser.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                            nomEtPrenonm = value.getString("nom") + " " + value.getString("prenom");
                            pseudo = value.getString("pseudo");
                            photoDeProfile = value.getString("photo de profile");
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(NewPostActivity.this, "il y a eu une erreur lors de la verification de vos informations", Toast.LENGTH_SHORT).show();
            }
        });

    }

    // pour que le bouton pour importer une image soit cliquable
    // durant toute la vie de l'activité NewPostActivity
    // on e met dans le onPostResume
    @Override
    protected void onPostResume() {
        super.onPostResume();
        //get image from camera/gallery on click
        getImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkImagePickDialog();
            }
        });
    }

    //posting
    private void uploadPost(final String title, final String description, String uri) {
        progressDialog_uploadPost.setTitle("Publication de votre post ...");
        progressDialog_uploadPost.show();

        final String timestamp = String.valueOf(System.currentTimeMillis());
        String filePathName = "Pubblication/" + "publication_" + timestamp;

        //post with image
        if (!uri.equals("sansImage")) {
            StorageReference storageReference1 = storageReference.child(filePathName);
            storageReference1.putFile(Uri.parse(uri))
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @SuppressLint("LongLogTag")
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful()) {
                                Log.d("Messege importaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaannnnnnnnnnnnnnnnnnnnt", "mbola tsy succès le tache !");
                                Toast.makeText(NewPostActivity.this, "mbola tsy succès le tache !", Toast.LENGTH_SHORT).show();
                            }
                            String downloadUri = uriTask.getResult().toString();

                            //verifier si l'image est téléversée ou pas et que l'url est bien reçu
                            if (uriTask.isSuccessful()) {
                                //store into Firestore
                                Map<String, Object> result = new HashMap<>();
                                result.put("Uid", uid);
                                result.put("nom et prenom", nomEtPrenonm);
                                result.put("pseudo", pseudo);
                                result.put("photo de profile", photoDeProfile);
                                result.put("pId", timestamp);
                                result.put("pTitre", title);
                                result.put("pDesrciption", description);
                                result.put("pImage", downloadUri);
                                result.put("pTemps", timestamp);

                                DocumentReference reference = collectionUsers.document("Publications");
                                reference.set(result)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                progressDialog_uploadPost.dismiss();
                                                Toast.makeText(NewPostActivity.this, "Status mise à jour", Toast.LENGTH_SHORT).show();
                                                //reset views after posting
                                                postTitle.setText("");
                                                postDescription.setText("");
                                                imagePost.setImageURI(null);
                                                imagePost.setMinimumHeight(0);
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog_uploadPost.dismiss();
                                        Log.d("message important", "******************************" + e.getMessage());
                                        Toast.makeText(NewPostActivity.this, "Erreur lors de la mise à jour", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                Toast.makeText(NewPostActivity.this, "Une erreur est survenue!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("message important", "******************************" + e.getMessage());
                    Toast.makeText(NewPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog_uploadPost.dismiss();
                }
            });
        }else{//post without image
            Map<String, Object> result = new HashMap<>();
            result.put("Uid", uid);
            result.put("nom et prenom", nomEtPrenonm);
            result.put("pseudo", pseudo);
            result.put("photo de profile", photoDeProfile);
            result.put("pId", timestamp);
            result.put("pTitre", title);
            result.put("pDesrciption", description);
            result.put("pImage", "sans Image");
            result.put("pTemps", timestamp);

            DocumentReference reference = collectionUsers.document("Publications");
            reference.set(result)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressDialog_uploadPost.dismiss();
                            Toast.makeText(NewPostActivity.this, "Status mise à jour", Toast.LENGTH_SHORT).show();

                            //reset views after posting
                            postTitle.setText("");
                            postDescription.setText("");
                            imagePost.setImageURI(null);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog_uploadPost.dismiss();
                    Log.d("message important", "******************************" + e.getMessage());
                    Toast.makeText(NewPostActivity.this, "Erreur lors de la mise à jour", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void checkImagePickDialog() {
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

    //check Camera permission
    private boolean checkCameraPermission(){
        //verifier si on est autorisé ou pas
        //retourne true si on est permis et false si non
        boolean result = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    //check request camera permission
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this,cameraPermission, CAMERA_REQUEST_CODE);
    }

    //check gallery permission
    private boolean checkStoragePermission(){
        //verifier si on est autorisé ou pas
        //retourne true si on est permis et false si non
        boolean result = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    //check request camera permission
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE);
    }

    //resultat : si on a accès ou pas à la camera et/ou à la gallerie de l'appareil
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
                imagePost.setImageURI(image_uri);

                //suprimer l'image séléctionnée
                getImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        imagePost.setImageURI(null);
                        imagePost.setMinimumHeight(0);
                    }
                });
            }
            if (requestCode == IMAGE_PICK_CAMERA_REQUEST_CODE){
                imagePost.setImageURI(image_uri);
            }
        }
    }

    private void checkUserStatus() {
        FirebaseUser user = mCurrentUser.getCurrentUser();
        if (user != null){

        }else {

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkConnexion();
        checkUserStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkConnexion();
        checkUserStatus();
    }

    // CHECK IF INTERNET CONNEXION IS AVAILABLE
    public boolean checkConnexion(){
        ConnectivityManager cm =
                (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if(!isConnected)
        {
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
            alert.show();
        }
        return isConnected;
    }

    //THE FOLLOWING METHOD IS USED TO DETACH EDIT_TEXT FOCUS WHEN WE CLICK OUTSIDE OF IT
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    assert imm != null;
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }
}
