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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iceteck.silicompressorr.FileUtils;
import com.iceteck.silicompressorr.SiliCompressor;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mg.didavid.firsttry.Models.ModelResto;
import mg.didavid.firsttry.Models.ModelRestoSampleMenu;
import mg.didavid.firsttry.R;
import mg.didavid.firsttry.Views.AppMode;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class RestoRegisterActivity extends AppMode implements LocationListener {

    private EditText restoName_editText, restoPassword_editText, restoConfirmPassword_editText, restoPhone_editText, restoEmail_editText, culinarySpeciality_editText;
    private ImageView restoLogo_imageView;
    private Button restoLocalisation_button, send_button;
    protected LinearLayout restoAddLogo_linearLayout;

    private String logoResto, nameResto, passwordResto, confirmPasswordResto, phoneResto, emailResto, culinarySpeciality,
            user_id, user_email, user_pseudo;
    private Double latitude, longitude;
    private List<ModelRestoSampleMenu> sampleMenuList;

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_REQUEST_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_REQUEST_CODE = 400;

    String[] cameraPermission;
    String[] storagePermission;
    Uri image_uri = null;
    Uri imageCompressed_uri = null;

    FirebaseAuth user = FirebaseAuth.getInstance();
    CollectionReference collectionReference_resto = FirebaseFirestore.getInstance().collection("Resto");

    boolean nameResto_boolean = false,
            password_boolean = false,
            phone_boolean = false,
            email_boolean = false,
            location_boolean = false,
            speciality_boolean = false;

    //private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int PERMISSIONS_REQUEST_ENABLE_GPS = 8001;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 8002;

    private LocationManager manager;

    private ProgressDialog progressDialog_registerRestoAccount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resto_register);

        //init views
        restoName_editText = findViewById(R.id.editText_nomResto_registerResto);
        restoPassword_editText = findViewById(R.id.editText_restoPassword_registerResto);
        restoConfirmPassword_editText = findViewById(R.id.editText_confirmRestoPassword_registerResto);
        restoPhone_editText = findViewById(R.id.editText_phoneResto_registerResto);
        restoEmail_editText = findViewById(R.id.editText_emailResto_registerResto);
        culinarySpeciality_editText = findViewById(R.id.editText_culinarySpeciality_registerResto);
        restoLogo_imageView = findViewById(R.id.imageView_logoResto_registerResto);
        restoLocalisation_button = findViewById(R.id.button_localisationResto_registerResto);
        send_button = findViewById(R.id.button_send_registerResto);
        restoAddLogo_linearLayout = findViewById(R.id.linearLayout_addLogoResto_registerResto);

        //init array of permissions
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        manager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

        progressDialog_registerRestoAccount = new ProgressDialog(this);
        progressDialog_registerRestoAccount.setTitle("Création de votre compte restaurant");
        progressDialog_registerRestoAccount.setMessage("Veillez patienter ...");

        //get some user informations
        checkUserInfo();

        restoAddLogo_linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePicDialog();
            }
        });

        //check resto location
        restoLocalisation_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkRestoLocation();
            }
        });

        //register resto account
        send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRestoInformations();
            }
        });
    }

    private void checkRestoLocation() {
        //create an alert dialog for checking user position
        final AlertDialog.Builder builder = new AlertDialog.Builder(RestoRegisterActivity.this);
        builder.setTitle("Vous êtes dans votre restaurant ?");
        builder.setMessage("Remarque : Vous devez être dans votre restaurant pour facilité sa localisation");
        builder.setCancelable(false);

        builder.setPositiveButton(
                "OUI",
                new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!getLocationPermission()) {
                            requestLocationPermission();
                        } else {
                            isGpsEnabled();
                        }
                    }
                }
        );

        builder.setNegativeButton(
                "Non",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        popupLocationRestoError();
                    }
                }
        );
        builder.create().show();

    }


    /**********************************************************************************************************************************************************
     *                                              FOR UPLOADING PROFILE IMAGE
     * ********************************************************************************************************************************************************
     */
    private boolean checkCameraPermission() {
        //verifier si on est autorisé ou pas
        //retourne true si on est permis et false si non
        boolean result = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_REQUEST_CODE);
    }


    private boolean checkStoragePermission() {
        //verifier si on est autorisé ou pas
        //retourne true si on est permis et false si non
        boolean result = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE);
    }

    //resultat : si on a accès ou pas à la camera et/ou au stockage de l'appareil
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            //location permission result request
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    isGpsEnabled();
                }
            }
            break;

            //pick image request
            case CAMERA_REQUEST_CODE: { //source camera selectionnée
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted) {
                        pickFromCamera();
                    } else {
                        Toast.makeText(this, "veillez activer la permission à acceder à la camera et au stockage", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;

            case STORAGE_REQUEST_CODE: { //source Gallerie selectionnée
                if (grantResults.length > 0) {
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted) {
                        pickFromGallery();
                    } else {
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
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            //result of gallery intent
            if (requestCode == IMAGE_PICK_GALLERY_REQUEST_CODE) {
                image_uri = data.getData();
                imageCompressed_uri = compressedAndSetImage();
                restoLogo_imageView.setImageURI(imageCompressed_uri);
            }

            //result of camera intent
            if (requestCode == IMAGE_PICK_CAMERA_REQUEST_CODE) {
                imageCompressed_uri = compressedAndSetImage();
                restoLogo_imageView.setImageURI(imageCompressed_uri);
            }

            //result of GPS intent
            if (requestCode == PERMISSIONS_REQUEST_ENABLE_GPS) {
                Log.d(TAG, "FT : GPS request result");
                isGpsEnabled();
            }
        }
    }

    //compression de l'image
    private Uri compressedAndSetImage() {
        Uri compressedImage = null;
        if (image_uri != null) {
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
                if (which == 0) {
                    //Camera clicked
                    if (!checkCameraPermission()) {
                        requestCameraPermission();
                    } else {
                        pickFromCamera();
                    }
                }
                if (which == 1) {
                    //Gallery ckicked
                    if (!checkStoragePermission()) {
                        requestStoragePermission();
                    } else {
                        pickFromGallery();
                    }
                }
            }
        });
        builder.create().show();
    }


    private void checkUserInfo() {
        FirebaseUser mCurrentUser = user.getCurrentUser();
        if (mCurrentUser != null) {
            user_id = mCurrentUser.getUid();
        }
        //get user pseudo and user email
        DocumentReference documentReference_user = FirebaseFirestore.getInstance().collection("Users").document(user_id);
        documentReference_user.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            user_email = documentSnapshot.getString("email");
                            user_pseudo = documentSnapshot.getString("pseudo");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RestoRegisterActivity.this, "Erreur : " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void getRestoInformations() {
        nameResto = restoName_editText.getText().toString();
        passwordResto = restoPassword_editText.getText().toString();
        confirmPasswordResto = restoConfirmPassword_editText.getText().toString();
        phoneResto = restoPhone_editText.getText().toString();
        emailResto = restoEmail_editText.getText().toString();
        culinarySpeciality = culinarySpeciality_editText.getText().toString();

        //control field culinary speciality
        if (culinarySpeciality.isEmpty()) {
            culinarySpeciality_editText.setError("Veillez renseigner votre spécialité culinare !");
        }else {
            speciality_boolean = true;
        }
        //control logo resto
        if (imageCompressed_uri == null) {
            Toast.makeText(this, "Veillez ajouter le logo de votre restaurant pour continuer", Toast.LENGTH_LONG).show();
        }

        //control name
        if (nameResto.isEmpty()) {
            restoName_editText.setError("Veuillez ajoute le nom de votre restaurant");
        } else {
            nameResto_boolean = true;
        }
        //control password
        boolean control_letter = false;
        boolean control_number = false;
        int j;
        int pwdLength = passwordResto.length();
        if (pwdLength < 6) {
            restoPassword_editText.setError("Doit contenir au moins 6 caractères");
        } else {
            for (j = 0; j < pwdLength; j++) {
                char[] ascciiCode_motDePasse = passwordResto.toCharArray();
                //check if there is one lettre at least
                if (((ascciiCode_motDePasse[j] > 0x40) && (ascciiCode_motDePasse[j] < 0x5B)) || ((ascciiCode_motDePasse[j] > 0x60) && (ascciiCode_motDePasse[j] < 0x7B))) {
                    if (!control_letter) {
                        control_letter = true;
                    }
                }
                //verification de presence d'un chiffre
                else if ((ascciiCode_motDePasse[j] > 0x29) && (ascciiCode_motDePasse[j] < 0x3A)) {
                    if (!control_number) {
                        control_number = true;
                    }
                }
            }
            //verification presence de lettre dans le mot de passe
            if (!control_letter) {
                restoPassword_editText.setError("Doit contenir au moins une lettre");
            }
            //verification presence de chiffre dans le mot de passe
            else if (!control_number) {
                restoPassword_editText.setError("Doit contenir au moins un Chiffre");
            }

            //comparaison de mot de passe
            if (control_letter && control_number) {
                if (!passwordResto.equals(confirmPasswordResto)) {
                    restoConfirmPassword_editText.setError("Le mot de passe ne correspond pas");
                } else {
                    password_boolean = true;
                }
            }
        }

        //control email
        if (!Patterns.EMAIL_ADDRESS.matcher(emailResto).matches()) {
            restoEmail_editText.setError("Email invalid");
        } else {
            email_boolean = true;
        }

        //control phone number
        if (phoneResto.length() < 10) {
            restoPhone_editText.setError("veillez entrer un numero de téléphone");
        } else {
            phone_boolean = true;
        }

        //control location
        if (latitude == null && longitude == null) {
            popupLocationRestoError();
        }


        if (email_boolean && phone_boolean && location_boolean && phone_boolean && password_boolean && nameResto_boolean && speciality_boolean) {
            progressDialog_registerRestoAccount.show();
            //store image into Firebase Storage
            String filePathName = "LogoResto/" + "logoResto_" + user_id;
            StorageReference storageReference_logoResto = FirebaseStorage.getInstance().getReference().child(filePathName);
            storageReference_logoResto.putFile(imageCompressed_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful()) {
                                Log.d("Uri tast", "download uri from image storage not yet");
                            }
                            uriTask.isSuccessful();
                            //get uri
                            logoResto = uriTask.getResult().toString();
                            //register resto account
                            singinResto();
                        }
                    });
        }
    }

    private void singinResto() {
        String id_resto = "resto_" + user_id;
        ModelResto modelResto = new ModelResto(user_id, user_email, user_pseudo, id_resto, nameResto, passwordResto, phoneResto, emailResto, logoResto, "noImage", culinarySpeciality, "0", "0", latitude, longitude, sampleMenuList);

        //store data
        collectionReference_resto.document(id_resto).set(modelResto)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(RestoRegisterActivity.this, "Compte créé avec succès", Toast.LENGTH_SHORT).show();
                        assert manager != null;
                        manager.removeUpdates(RestoRegisterActivity.this);
                        sendListeMenu();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RestoRegisterActivity.this, "Erreur : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendListeMenu() {
        startActivity(new Intent(RestoRegisterActivity.this, ListMenuRestoActivity.class));
        progressDialog_registerRestoAccount.dismiss();
        finish();
    }

    private void popupLocationRestoError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Veuillez ajouter la localisation de votre restaurant pour valider la création de votre compte !\n\nRemarque : Vous devez être dans votre restaurant pour faciliter sa localisation");
        builder.setCancelable(false);

        builder.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        builder.create().show();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //PERMISSION REQUESTS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    //REQUEST FOR ENABLING GPS
    private void buildAlertMessageNoGps() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RestoRegisterActivity.this);
        builder.setMessage("Cette application à besoin du GPS pour son bon fonctionnement, voulez-vous l'activer ?")
                .setCancelable(false)
                .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

        Log.d(TAG, "FT : GPS alert message!!");
    }


    //REQUEST FOR LOCATION PERMISSION
    private boolean getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(RestoRegisterActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION},
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
    }

    //CHECK IF GPS IS ENABLED
    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void isGpsEnabled() {
        Location location;
        if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, this);
            location = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location != null) {
                location_boolean = true;
                latitude = location.getLatitude();
                longitude = location.getLongitude();

                Toast.makeText(RestoRegisterActivity.this, "latitude : " + latitude + "\nlongitude : " + longitude, Toast.LENGTH_LONG).show();

                //change button location
                restoLocalisation_button.setText("");
                restoLocalisation_button.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_check_button_icon_dark, 0, 0, 0);
                restoLocalisation_button.setBackground(null);
                restoLocalisation_button.setCompoundDrawablePadding(10);
            }
        }else {
            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Log.d(TAG, "FT : GPS not enabled!");
                buildAlertMessageNoGps();
            }else {
                manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
                location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location != null) {
                    location_boolean = true;
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();

                    Toast.makeText(RestoRegisterActivity.this, "latitude : " + latitude + "\nlongitude : " + longitude, Toast.LENGTH_LONG).show();

                    //change button location
                    restoLocalisation_button.setText("");
                    restoLocalisation_button.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_check_button_icon_dark, 0, 0, 0);
                    restoLocalisation_button.setBackground(null);
                    restoLocalisation_button.setCompoundDrawablePadding(10);

                }
                Log.d(TAG, "FT : GPS enabled");
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
            /*locationResto.put("latitude", location.getLatitude());
            locationResto.put("longitude", location.getLongitude());
            Toast.makeText(RestoRegisterActivity.this, "latitude : " + locationResto.get("latitude") + "longitude" + locationResto.get("longitude"), Toast.LENGTH_LONG).show();*/
    }

    /**
     *
     * @param provider
     * @param status
     * @param extras
     * @deprecated This callback will never be invoked.
     */
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    /**
     * Called when the provider is enabled by the user.
     *
     * @param provider the name of the location provider associated with this
     *                 update.
     */
    @Override
    public void onProviderEnabled(String provider) {

    }

    /**
     * Called when the provider is disabled by the user. If requestLocationUpdates
     * is called on an already disabled provider, this method is called
     * immediately.
     *
     * @param provider the name of the location provider associated with this
     *                 update.
     */
    @Override
    public void onProviderDisabled(String provider) {

    }
    ///////////////////////////////////////////////////////////////////
    // END SERVICES AND PERMISSION REQUESTS
    ////////////////////////////////////////////////////////////////


}
