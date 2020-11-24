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
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
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
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iceteck.silicompressorr.FileUtils;
import com.iceteck.silicompressorr.SiliCompressor;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import mg.didavid.firsttry.Models.ModelePost;
import mg.didavid.firsttry.Models.User;
import mg.didavid.firsttry.Models.UserSingleton;
import mg.didavid.firsttry.R;
import mg.didavid.firsttry.Views.AppMode;

public class WelcomeActivity extends AppMode {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference userCollectionReference = db.collection("Users");

    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

    private String user_id = "NULL";
    private String lastname = "NULL";
    private String firstname = "NULL";
    private String display_name = "NULL";
    private String pseudo = "NULL";
    private String sexe = "NULL";
    private String phone = "NULL";
    private String email = "NULL";
    private String profileImage_Uri = "https://firebasestorage.googleapis.com/v0/b/first-try-280722.appspot.com/o/UsersPhotoDeProfie%2Fdefault_profile_picture.png?alt=media&token=16300f76-a703-4983-adc7-7f8645b3d8f5";
    private final String TAG= "MainActivity";

    private String[] separated_name;

    private EditText editText_nom, editText_prenom, editText_pseudo, editText_phone, editText_email;
    private RadioGroup radioGroup_sexe;
    private RadioButton radioButton_male, radioButton_female, radioButton_selected;
    private Button button_send;
    private LinearLayout addProfileImage;
    private ImageView profileImage_imageView;
    private TextView textView_pdp;

    StorageReference storageReference; // reference of the Firebase storage
    String storagePdPPath = "UsersPhotoDeProfie/"; // storageReference/UsersPhotoDeProfile/


    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_REQUEST_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_REQUEST_CODE = 400;

    String [] cameraPermission;
    String [] storagePermission;
    Uri image_uri = null;
    Uri imageCompressed_uri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        editText_nom = findViewById(R.id.editText_nom);
        editText_prenom = findViewById(R.id.editText_prenom);
        editText_pseudo = findViewById(R.id.editText_pseudo_welcome);
        editText_phone = findViewById(R.id.editText_phone);
        editText_email = findViewById(R.id.editText_email);
        radioGroup_sexe = findViewById(R.id.radioGroup_sexe);
        radioButton_male = findViewById(R.id.radioButton_male);
        radioButton_female = findViewById(R.id.radioButton_female);
        button_send = findViewById(R.id.button_send);
        addProfileImage = findViewById(R.id.linearLayout_add_profileImage_welcome);
        profileImage_imageView = findViewById(R.id.imageView_profileImage_welcome);
        textView_pdp = findViewById(R.id.textView_pdp);

        int textView_color = textView_pdp.getCurrentTextColor();

        //set default profile picture


        storageReference = FirebaseStorage.getInstance().getReference();

        //always check if we are connected into the internet
        checkConnexion();

        //init array of permissions
        cameraPermission = new String[] {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE};

        Intent intent = getIntent();
        String singinPseudo = "" + intent.getStringExtra("key");
        String register_pseudo = "" + intent.getStringExtra("pseudo");
        String register_pwd = "" + intent.getStringExtra("password");
        String password = null;
        final String finalPassword = password;
        button_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(TextUtils.isEmpty(editText_nom.getText().toString().trim())) {
                    editText_nom.setError("Veuillez rensigner cette information");
                    return;
                }
                else if(TextUtils.isEmpty(editText_prenom.getText().toString().trim())){
                    editText_prenom.setError("Veuillez rensigner cette information");
                    return;
                }
                else if(TextUtils.isEmpty(editText_pseudo.getText().toString().trim())){
                    editText_pseudo.setError("Veuillez rensigner cette information");
                    return;
                }else if(imageCompressed_uri == null){
                    textView_pdp.setTextColor(Color.RED);
                }
                else{
                    radioButton_selected =findViewById(radioGroup_sexe.getCheckedRadioButtonId());

                    user_id = firebaseUser.getUid();
                    lastname = editText_nom.getText().toString();
                    firstname = editText_prenom.getText().toString();
                    display_name = firstname + " " + lastname;
                    pseudo = editText_pseudo.getText().toString();
                    sexe = radioButton_selected.getText().toString();
                    phone = editText_phone.getText().toString();
                    email = editText_email.getText().toString();
                    profileImage_Uri = imageCompressed_uri.toString();

                    final User user = new User(user_id, display_name, sexe,
                            pseudo, email, phone, finalPassword, profileImage_Uri);

                    storeUserData(user);
                }
            }
        });

        //IF USER IS AUTH TO FIREBASE AND NO SINGLETON SET
        if (singinPseudo.equals("Pseudo and Password")) {
            editText_pseudo.setEnabled(false);
            editText_pseudo.setText(register_pseudo);
            password = register_pwd;
        }else {
                configureUser();
        }

        //Layout (ImageView, TextView : "Ajouter une photo de profil") clicked
        addProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePicDialog();
            }
        });

        //ImageView that content profile image is clicked
        profileImage_imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePicDialog();
            }
        });
    }

    private void configureUser() {
        DocumentReference documentReference = userCollectionReference.document(firebaseUser.getUid());

        //CHECK IF THE USER IS ALREADY STORED IN THE DATABASE OR NOT
        //IF NOT THEN CREATE A NEW DOCUMENT WITH THE UID
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                //Checking request result
                if (task.isSuccessful()) {
                    //Request was successful but it never means that data is found
                    DocumentSnapshot data = task.getResult();
                    if (data.exists()) {
                        Toast.makeText(WelcomeActivity.this, "YOU ARE ALREADY SAVED IN THE DATABASE !!!", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                        startActivity(intent);

                        finish();
                    } else {
                        email = firebaseUser.getEmail();
                        user_id = firebaseUser.getUid();
                        display_name = firebaseUser.getDisplayName();
                        phone = firebaseUser.getPhoneNumber();

                        if(email != null){
                            editText_email.setText(email);
                            editText_email.setEnabled(false);
                        }

                        if(display_name != null){
                            separated_name = display_name.split(" ");
                            firstname = separated_name[0];
                            lastname = separated_name[1];

                            editText_prenom.setText(firstname);
                            editText_nom.setText(lastname);
                        }

                        if(phone != null){
                            editText_phone.setText(phone);
                        }
                    }

                } else {
                    //Request was not successful
                    //Could be some rules or internet problem
                    Log.i(TAG, "onComplete: Request unsuccessful, error: " + task.getException().getLocalizedMessage());
                }
            }
        });
    }
    //CREATE NEW USER IN FIRESTORE AND STORE DATAS
    private void storeUserData(final User user){

        final String timestamp = String.valueOf(System.currentTimeMillis());
        String filePathAndName = storagePdPPath + timestamp + "_profile_image" + "_" + firebaseUser.getUid(); //nom de l'image

        //storing imagge to Firabase Storage
        StorageReference storageReference1 = storageReference.child(filePathAndName);
        storageReference1.putFile(imageCompressed_uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @SuppressLint("LongLogTag")
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        DocumentReference documentReference = userCollectionReference.document(user_id);

                        //Writing data and using call-back functions
                        documentReference.set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    createPreferences(user.getUser_id());

                                    final AlertDialog.Builder builder = new AlertDialog.Builder(WelcomeActivity.this);
                                    builder.setMessage("Votre compte a été créé avec succes");
                                    builder.setCancelable(false);
                                    final AlertDialog alert = builder.create();

                                    builder.setPositiveButton(
                                            "Continuer",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                                                    startActivity(intent);
                                                    alert.cancel();
                                                    finish();
                                                }
                                            }).show();
                                }else{
                                    //Something went wrong
                                    Log.e(TAG, "onComplete: Error: " + task.getException().getLocalizedMessage() );
                                }
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("message important", "******************************" +e.getMessage());
                Toast.makeText(WelcomeActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createPreferences(String id){
        DatabaseReference mUserPreferencesReference = FirebaseDatabase.getInstance().getReference().child("userPreferences");

        mUserPreferencesReference.child(id).child("seeMyPosition").setValue(true);
        mUserPreferencesReference.child(id).child("radius").setValue(0);
    }

    /**********************************************************************************************************************************************************
     *                                              FOR UPLOADING PROFILE IMAGE
     * ********************************************************************************************************************************************************
     */
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
                profileImage_imageView.setImageURI(imageCompressed_uri);
            }
            if (requestCode == IMAGE_PICK_CAMERA_REQUEST_CODE){
                imageCompressed_uri = compressedAndSetImage();
                profileImage_imageView.setImageURI(imageCompressed_uri);
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
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
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

}
