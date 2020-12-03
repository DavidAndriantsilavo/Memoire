package mg.didavid.firsttry.Controllers.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iceteck.silicompressorr.FileUtils;
import com.iceteck.silicompressorr.SiliCompressor;
import com.squareup.picasso.Picasso;

import java.io.File;

import mg.didavid.firsttry.Models.ModelRestoSampleMenu;
import mg.didavid.firsttry.R;
import mg.didavid.firsttry.Views.AppMode;

public class AddMenuToListActivity extends AppMode {

    String menuName, menuPrice, menuPhoto, menuIngredient;
    ImageView imageView_menuImage;
    EditText editText_menuName, editText_menuPrice, editText_menuIngredient;
    Button button_addImage, button_post;

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_REQUEST_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_REQUEST_CODE = 400;

    String [] cameraPermission;
    String [] storagePermission;
    Uri image_uri = null;
    Uri imageCompressed_uri = null;

    String user_id;

    ProgressDialog progressDialog_sendSampleMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_menu_to_list);

        //set tool bar
        Toolbar toolbar = findViewById(R.id.toolbar_addMenu);
        if (toolbar != null){
            //set toolbar title
            // Sets the Toolbar
            setSupportActionBar(toolbar);
            this.setTitle("Ajout de menu");
        }

        //init view
        imageView_menuImage = findViewById(R.id.imageView_menuPhoto_addListMenu);
        editText_menuName = findViewById(R.id.editText_menuName_addListMenu);
        editText_menuIngredient = findViewById(R.id.editText_menuIngredient_addListMenu);
        editText_menuPrice = findViewById(R.id.editText_menuPrice_addListMenu);
        button_addImage = findViewById(R.id.btn_addImage_addListMenu);
        button_post = findViewById(R.id.btn_send_addListMenu);

        user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        progressDialog_sendSampleMenu = new ProgressDialog(this);
        progressDialog_sendSampleMenu.setMessage("Enreistrement du menu...");
        progressDialog_sendSampleMenu.setCanceledOnTouchOutside(false);
        progressDialog_sendSampleMenu.setCancelable(false);

        //init array of permissions
        cameraPermission = new String[] {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //on click button add image
        button_addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePicDialog();
            }
        });

        //on click button send post
        button_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkInput();
            }
        });
    }

    private void checkInput() {
        menuName = editText_menuName.getText().toString();
        menuPrice = editText_menuPrice.getText().toString();
        menuIngredient = editText_menuIngredient.getText().toString();

        if (image_uri == null){
            Toast.makeText(this, "Veillez ajouter une photo", Toast.LENGTH_LONG).show();
        }
        if (menuName.isEmpty()){
            editText_menuName.setError("Veillez ajouter le nom de votre menu");
        }
        if (menuIngredient.isEmpty()){
            editText_menuIngredient.setError("Veillez ajouter les ingredients de votre menu");
        }
        if (menuPrice.isEmpty()) {
            editText_menuPrice.setError("Veillez indiquer le prix de votre menu");
        }

        if (image_uri != null && !menuName.isEmpty() && !menuPrice.isEmpty() && !menuIngredient.isEmpty()) {
            storeImage();
        }
    }

    private void storeImage() {
        progressDialog_sendSampleMenu.show();

        final String timestamp = String.valueOf(System.currentTimeMillis());
        String filePathAndName = "Menu_list/" + timestamp + "_" + "resto_" + user_id;
        //store image
        StorageReference storageReference_sampleMenu = FirebaseStorage.getInstance().getReference().child(filePathAndName);
        storageReference_sampleMenu.putFile(imageCompressed_uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) { }
                        uriTask.isSuccessful();
                        menuPhoto = uriTask.getResult().toString();
                        sendSampleMenu();
                    }
                });
    }

    private void sendSampleMenu() {
        ModelRestoSampleMenu modelRestoSampleMenu = new ModelRestoSampleMenu("resto_" + user_id, String.valueOf(System.currentTimeMillis()), menuIngredient, menuPhoto, menuName, menuPrice);
        CollectionReference collectionReference_sampleMenu = FirebaseFirestore.getInstance().collection("Menu_list");
        final String timestamp = String.valueOf(System.currentTimeMillis());
        //store data
        collectionReference_sampleMenu.document(timestamp).set(modelRestoSampleMenu)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(AddMenuToListActivity.this, "Menu de menu enregister avec succès", Toast.LENGTH_LONG).show();
                        progressDialog_sendSampleMenu.dismiss();
                        imageCompressed_uri = null;
                        imageView_menuImage.setImageURI(imageCompressed_uri);
                        editText_menuName.setText("");
                        editText_menuIngredient.setText("");
                        editText_menuPrice.setText("");
                        imageView_menuImage.setBackgroundResource(R.drawable.ic_photo_icon_dark);
                    }
                });
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
                imageView_menuImage.setImageURI(imageCompressed_uri);
            }
            if (requestCode == IMAGE_PICK_CAMERA_REQUEST_CODE){
                imageCompressed_uri = compressedAndSetImage();
                imageView_menuImage.setImageURI(imageCompressed_uri);
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
}
