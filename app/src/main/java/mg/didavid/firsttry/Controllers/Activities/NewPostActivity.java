package mg.didavid.firsttry.Controllers.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

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
import com.iceteck.silicompressorr.FileUtils;
import com.iceteck.silicompressorr.SiliCompressor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import mg.didavid.firsttry.Models.ModelePost;
import mg.didavid.firsttry.R;

public class NewPostActivity extends AppCompatActivity {

    FirebaseAuth mCurrentUser;

    EditText postDescription;
    TextView textView_addImage_post;
    ImageButton imageButton_addImage;
    Button publishBtn;
    int countImage = 0;
    final int NUMBER_MAX_OF_IMAGES = 3;
    boolean imagePicked = false;
    String[] uriString = new String[NUMBER_MAX_OF_IMAGES];

    ProgressDialog progressDialog_uploadPost;

    LinearLayout linearLayout_ImagePostAddedDynamically;
    LinearLayout linearLayout_addImagePost;


    FirebaseFirestore firestore;
    CollectionReference collectionUsers; // Firestore's collection reference : root/reference
    DocumentReference docRefProfileUser; // reference of the document in Firestoer : root/reference/document
    StorageReference storageReference; // reference of the Firebase storage

    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private static final int CAMERA_REQUEST_CODE = 110;
    private static final int STORAGE_REQUEST_CODE = 220;
    private static final int IMAGE_PICK_GALLERY_REQUEST_CODE = 330;
    private static final int IMAGE_PICK_CAMERA_REQUEST_CODE = 440;

    String [] cameraPermission;
    String [] storagePermission;
    Uri image_uri = null;

    String nomEtPrenonm, pseudo, uid, photoDeProfile;

    List<Uri> getAllUri = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint({"ResourceType", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        checkConnexion();

        mCurrentUser = FirebaseAuth.getInstance();
        checkUserStatus();

        postDescription = findViewById(R.id.editText_inputPostDescription_newPost);
        publishBtn = findViewById(R.id.button_publish_post);
        linearLayout_addImagePost = findViewById(R.id.linearLayout_addImage_newPost);
        linearLayout_ImagePostAddedDynamically = findViewById(R.id.linearLayout_forImageAddedDynamically);
        textView_addImage_post = findViewById(R.id.textView_nearAddImage_newPost);
        textView_addImage_post.setVisibility(View.VISIBLE);
        imageButton_addImage = findViewById(R.id.imageButton_addImage_post);

        progressDialog_uploadPost = new ProgressDialog(this);

        firestore = FirebaseFirestore.getInstance();
        collectionUsers = firestore.collection("Users");
        storageReference = FirebaseStorage.getInstance().getReference();

        //init array of permissions
        cameraPermission = new String[] {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //get image from camera/gallery on click
        //handle if there is 3 images added, change view for not clickable
        linearLayout_addImagePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (countImage < 3) {
                    checkImagePickDialog();
                }else {
                    Toast.makeText(NewPostActivity.this, "Vous n'avez droit qu'à 3 photos", Toast.LENGTH_LONG).show();
                }
            }
        });

        //valider et publier le nouveau post
        publishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog_uploadPost.setMessage("Publication de votre post ...");
                progressDialog_uploadPost.show();
                progressDialog_uploadPost.setCancelable(false);
                progressDialog_uploadPost.setCanceledOnTouchOutside(false);
                final String description = postDescription.getText().toString();

                    if (!(getAllUri != null) || getAllUri.isEmpty()) {
                        if (TextUtils.isEmpty(description)) {
                            Toast.makeText(NewPostActivity.this, "Veillez entrer une description à votre publication", Toast.LENGTH_SHORT).show();
                        } else {
                            //post without image
                            uploadPost(description);
                        }
                    } else if (getAllUri != null){
                        //post with image
                        final String[] downloadUri = new String[countImage];
                        final String timestamp = String.valueOf(System.currentTimeMillis());
                        for (int i = 0; i < countImage; i++) {
                            List<Uri> uriLisOfImagesCompressed = compressedAndSetImage();
                            String filePathName = "Pubblication/" + "publication_" + timestamp + "_" + i;
                            StorageReference storageReference1 = storageReference.child(filePathName);
                            final int finalI = i;
                            storageReference1.putFile(uriLisOfImagesCompressed.get(i))
                                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @SuppressLint("LongLogTag")
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                                            while (!uriTask.isSuccessful()) {
                                                Log.d("Messege importaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaannnnnnnnnnnnnnnnnnnnt", "mbola tsy succès le tache !");
                                                Toast.makeText(NewPostActivity.this, "mbola tsy succès le tache !", Toast.LENGTH_SHORT).show();
                                            }

                                            //verifier si l'image est téléversée ou pas et que l'url est bien reçu
                                            uriTask.isSuccessful();
                                            downloadUri [finalI] = uriTask.getResult().toString();
                                            Log.d("image uri", downloadUri[finalI]);
                                            int imageNumber = finalI;
                                            getUri(downloadUri, imageNumber, description);
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("message important", "******************************" + e.getMessage());
                                    Toast.makeText(NewPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    progressDialog_uploadPost.dismiss();
                                }
                            });
                        }
                    }
            }

            private void getUri(String[] downloadUri, int finalI, String description) {
                uriString [finalI] = downloadUri [finalI];
                Log.d("Image sended", uriString[finalI]);
                if (finalI == countImage - 1) {
                    uploadPost(description);
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
                            nomEtPrenonm = value.getString("name");
                            pseudo = value.getString("pseudo");
                            photoDeProfile = value.getString("profile_image");
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

    private void uploadPost(final String description) {
        String [] images = new String[NUMBER_MAX_OF_IMAGES];
        final String timestamp = String.valueOf(System.currentTimeMillis());
        for (int i = 0; i < NUMBER_MAX_OF_IMAGES; i++) {
            if (uriString [i] != null && !uriString [i].isEmpty()) {
                images [i] = uriString [i];
            }else {
                images [i] = "noImage";
            }
            Log.d("image = " + i, images[i]);
        }

        //post with image
        //store into Firestore
        ModelePost modelePost = new ModelePost(uid, nomEtPrenonm, pseudo, "0", "0", photoDeProfile, timestamp, description, images[0], images[1], images[2],timestamp);

        //store data on Firestore
        CollectionReference reference = FirebaseFirestore.getInstance().collection("Publications");
        reference.document(timestamp).set(modelePost)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(NewPostActivity.this, "Status mise à jour", Toast.LENGTH_SHORT).show();
                        //reset views after posting
                        postDescription.setText("");
                        //imagePost.setImageURI(null);
                        //imagePost.setMinimumHeight(0);

                        //go to main activity when finish
                        sendToMainActivity();
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

    //compression de l'image
    private List<Uri> compressedAndSetImage() {
        List<Uri> listUriCompressed = new ArrayList<>();
        listUriCompressed.clear();

        for (int i = 0; i < countImage; i++) {
            if (getAllUri.get(i) != null) {
                File file = new File(SiliCompressor.with(this)
                        .compress(FileUtils.getPath(this, getAllUri.get(i)), new File(this.getCacheDir(), "temp_" + i)));
                listUriCompressed.add(i, Uri.fromFile(file));
                Log.d("valeur de i = " + i, String.valueOf(listUriCompressed.get(i)));
            }
        }
        return listUriCompressed;
    }

    private void sendToMainActivity() {
        Intent intent = new Intent(NewPostActivity.this, MainActivity.class);
        startActivity(intent);
        progressDialog_uploadPost.dismiss();
        finish();
    }

    //check Camera permission
    private boolean checkCameraPermission(){
        //verifier si on est autorisé ou pas
        //retourne true si on l'est et false si on ne l'est pas
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
        return ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
    }

    //check request storage permission
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
            imagePicked = true;
            if (requestCode == IMAGE_PICK_GALLERY_REQUEST_CODE && data != null){
                image_uri = data.getData();
                addImageToTheView();
            }
            if (requestCode == IMAGE_PICK_CAMERA_REQUEST_CODE) {
                addImageToTheView();
            }
        }
    }

    //create view dynamically
    private void addImageToTheView() {
        linearLayout_ImagePostAddedDynamically.addView(tableLayout(), countImage);
        countImage = linearLayout_ImagePostAddedDynamically.getChildCount();
        Log.d("countImage", String.valueOf(countImage));
        if (countImage == 3) {
            imageButton_addImage.setVisibility(View.GONE);
            textView_addImage_post.setText("Limite d'images atteinte");
        }
    }

    //Create a table layout for containing views
    private TableLayout tableLayout() {
        TableLayout tableLayout = new TableLayout(this);
        tableLayout.addView(createRowView());
        return tableLayout;

    }

    //create table rows for containing our widgets, then add it to the table layout
    private TableRow createRowView() {
        TableRow tableRow = new TableRow(this);
        tableRow.setPadding(0,0,10,0);

        ImageView imageView = new ImageView(this);
        TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        imageView.setLayoutParams(params);
        imageView.setMaxWidth(170);
        imageView.setMinimumWidth(150);
        imageView.setMaxHeight(260);
        imageView.setMinimumHeight(200);
        imageView.setAdjustViewBounds(true);
        imageView.setImageURI(image_uri);

        //get image uri
        Uri uriTemp = image_uri;
        if (uriTemp != null && imagePicked) {
            getAllUri.add(countImage, uriTemp);
            imagePicked = false;
            Log.d("uriTemp", String.valueOf(uriTemp));
        }

        //add image to table row
        tableRow.addView(imageView);

        ImageButton imageButton = new ImageButton(this);
        TableRow.LayoutParams params1 = new TableRow.LayoutParams(45,60);
        imageButton.setLayoutParams(params1);
        imageButton.setImageResource(R.drawable.btn_clear);
        imageButton.setOnClickListener(buttonDeleteImageClicked);
        imageButton.setId(countImage);
        tableRow.addView(imageButton);

        return tableRow;
    }

    //delete view and image set
    private View.OnClickListener buttonDeleteImageClicked = new View.OnClickListener () {
        @Override
        public void onClick(View v) {
            int view_id = v.getId();
            Log.d("viewId", String.valueOf(view_id));
            //delete the image uri o this view
            for (int i = view_id; i < countImage; i++) {
                if (i < (countImage - 1)) {
                    getAllUri.set(i, getAllUri.get(i+1));
                    Log.d("image getAllUri n°" + i, String.valueOf(getAllUri.get(i)));
                    TableLayout tableLayout = (TableLayout) linearLayout_ImagePostAddedDynamically.getChildAt(i + 1);
                    TableRow tableRow = (TableRow) tableLayout.getChildAt(0);
                    ImageButton imageButton = (ImageButton) tableRow.getChildAt(1);
                    imageButton.setId(i);
                } else {
                    getAllUri.remove(i);
                }
            }
            //remove view
            TableRow tableRow = (TableRow) v.getParent();
            TableLayout tableLayout = (TableLayout) tableRow.getParent();
            linearLayout_ImagePostAddedDynamically.removeView(tableLayout);

            countImage --; //decrease image count
            imageButton_addImage.setVisibility(View.VISIBLE);
            textView_addImage_post.setText("Ajouter une image");
        }
    };


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
