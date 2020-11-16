package mg.didavid.firsttry.Controllers.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iceteck.silicompressorr.FileUtils;
import com.iceteck.silicompressorr.SiliCompressor;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import mg.didavid.firsttry.Controllers.Adapteurs.AdapteurComments;
import mg.didavid.firsttry.Models.ModelComment;
import mg.didavid.firsttry.Models.ModelePost;
import mg.didavid.firsttry.R;
import mg.didavid.firsttry.Views.AppMode;

public class PostDetailsActivity extends AppMode {

    //to get details of the post and currentUser
    String myName_temp, myName, myPseudo, myUid = FirebaseAuth.getInstance().getCurrentUser().getUid()
            , myProfile_image, post_id, post_kiff, comment_count, hidName, hisProfile_image, hisPseudo,
            postImage1, postImage2, postImage3, postDescription, user_id;

    //post details views
    TextView user_name, post_time, post_description, textView_pseudo, textView_postKiff, textView_nbrPosComment;
    ImageButton imageButton_more;
    ImageView user_profileImage, post_image1, post_image2, post_image3, imageAddedIntoComment;
    Button kiff_button, location_button;
    LinearLayout linearLayout_image23;
    RatingBar ratingBar;

    //comment views
    RecyclerView recyclerView_comments;
    ImageView comment_userProfileImage;
    EditText commnet_addComment;
    ImageButton comment_buttonSend, comment_addImage;

    ProgressDialog progressDialog_loadComment;

    List<ModelComment> modelComments;

    boolean mPressKiff = false;
    boolean mProcessComment = false;

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_REQUEST_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_REQUEST_CODE = 400;

    String [] cameraPermission;
    String [] storagePermission;
    Uri image_uri = null, imageCompressed_uri;

    String storagePdPPath = "ImagesComments/";

    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    StorageReference storageReference;

    private CollectionReference collectionReference_hasRatingResto = FirebaseFirestore.getInstance().collection("HasRatingResto");

    int commentCount;

    List<ModelComment> commentList;
    AdapteurComments adapteurComments;


    CollectionReference collectionReference_kiffs = FirebaseFirestore.getInstance().collection("Kiffs");
    CollectionReference collectionReference_post = FirebaseFirestore.getInstance().collection("Publications");

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        //set tool bar
        Toolbar toolbar = findViewById(R.id.toolbar_postDetails);
        if (toolbar != null){
            // Sets the Toolbar
            setSupportActionBar(toolbar);

            // Get a support ActionBar corresponding to this toolbar
            ActionBar ab = getSupportActionBar();

            // Enable the Up button
            ab.setDisplayHomeAsUpEnabled(true);

        }

        //init views
        //post details views
        user_name = findViewById(R.id.textView_nomUser_comment);
        post_time = findViewById(R.id.textView_temps_comment);
        post_description = findViewById(R.id.textView_descriptionPost_comment);
        textView_pseudo = findViewById(R.id.texteView_pseudo_postDetails);
        textView_postKiff = findViewById(R.id.texteView_kiffs_comment);
        textView_nbrPosComment = findViewById(R.id.texteView_commentsNbr_comment);
        imageButton_more = findViewById(R.id.button_moreAction_comment);
        user_profileImage = findViewById(R.id.imageView_photoDeProfile_postComment);
        post_image1 = findViewById(R.id.imageView_imagePost1_comment);
        post_image2 = findViewById(R.id.imageView_imagePost2_comment);
        post_image3 = findViewById(R.id.imageView_imagePost3_comment);
        imageAddedIntoComment = findViewById(R.id.imageView_inputImage_EditComment_comment);
        kiff_button = findViewById(R.id.button_kiff_comment);
        location_button = findViewById(R.id.button_partager_comment);
        linearLayout_image23 = findViewById(R.id.linearLayout_imagePost23_comment);
        ratingBar = findViewById(R.id.ratingBar_postDetails);
        //comment views
        recyclerView_comments = findViewById(R.id.recyclerView_comment);
        comment_userProfileImage = findViewById(R.id.imageView_photoDeProfile_comment);
        commnet_addComment = findViewById(R.id.editComment);
        comment_buttonSend = findViewById(R.id.btn_sendComment);
        comment_addImage = findViewById(R.id.btn_addImage_comment);

        //init array of permissions
        cameraPermission = new String[] {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE};

        storageReference = FirebaseStorage.getInstance().getReference();

        progressDialog_loadComment = new ProgressDialog(this);
        progressDialog_loadComment.setMessage("Chargement...");
        progressDialog_loadComment.show();


        //get data from intent
        final Intent intent = getIntent();
        try {
            post_id = intent.getStringExtra("post_id");
            user_id = intent.getStringExtra("user_id");
            Log.d("valeur intent", "" + post_id);
            Log.d("valeur intent", "" + user_id);
        }catch (Exception e) {
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        setKiffs(post_id);
        loadPostDetails();
        loadUserInfo();

        //button add image on comment clicked
        comment_addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePicDialog();
            }
        });

        //send comment when button send comment clicked
        comment_buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postComment();
            }
        });

        //profile image clicked, send to profile
        user_profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user_id != null && user_id.equals(myUid)){
                    startActivity(new Intent(PostDetailsActivity.this, ProfileUserActivity.class));
                }else {
                    Intent intent1 = new Intent(PostDetailsActivity.this, OtherUsersProfileActivity.class);
                    intent1.putExtra("user_id", user_id);
                    startActivity(intent1);
                }
            }
        });

        //currentUser name clicked, send to profile
        user_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user_id != null && user_id.equals(myUid)){
                    startActivity(new Intent(PostDetailsActivity.this, ProfileUserActivity.class));
                }else {
                    Intent intent1 = new Intent(PostDetailsActivity.this, OtherUsersProfileActivity.class);
                    intent1.putExtra("user_id", user_id);
                    startActivity(intent1);
                }
            }
        });

        //profile image on edit comment clicked, send to profile
        comment_userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PostDetailsActivity.this, ProfileUserActivity.class));
            }
        });

        //button share clicked
        location_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(PostDetailsActivity.this, "share button\nWill implement later", Toast.LENGTH_LONG).show();
            }
        });

        //button more actions clicked
        imageButton_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions(imageButton_more, user_id, myUid, post_id, postImage1, postImage2, postImage3, postDescription);
            }
        });

        //textView post kiff clicked, send to ShowWhokiffActivity
        textView_postKiff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PostDetailsActivity.this, ShowWhoKiffAvtivity.class);
                intent.putExtra("key", post_id);
                startActivity(intent);
            }
        });
    }


    private void loadComments(final String post_id) {
        //layout for recyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        //set layout into recyclerView
        recyclerView_comments.setLayoutManager(linearLayoutManager);

        //init comment list
        commentList = new ArrayList<>();

        //check if there is one comment at least
        final CollectionReference collectionReference_comment = FirebaseFirestore.getInstance().collection("Comments");
        collectionReference_comment.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            //get data
                            collectionReference_comment
                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                            commentList.clear();
                                            modelComments = value.toObjects(ModelComment.class);
                                            int size = modelComments.size();
                                            for (int i = 0; i < size; i++) {
                                                if (modelComments.get(i).getPost_id().contains(post_id)) {
                                                    commentList.add(modelComments.get(i));
                                                }
                                            }

                                            //setup adapter
                                            adapteurComments = new AdapteurComments(PostDetailsActivity.this, commentList);
                                            //set adapter to recyclerView
                                            recyclerView_comments.setAdapter(adapteurComments);
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(PostDetailsActivity.this, ""+e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showMoreOptions(ImageButton moreBtn, final String user_id, final String mCurrentUserId, final String post_id, final String post_image1, final String post_image2, final String post_image3, final String post_description) {
        //create popup menu
        PopupMenu popupMenu = new PopupMenu(PostDetailsActivity.this, moreBtn, Gravity.END);
        //show popup menu in only posts of currently singed-in currentUser
        if (user_id.contains(mCurrentUserId)){
            //add item in menu
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Supprimer la publication");
            popupMenu.getMenu().add(Menu.NONE, 1, 1, "Modifier la publication");
            popupMenu.getMenu().add(Menu.NONE, 5, 5, "Voir tous les menus");
            popupMenu.getMenu().add(Menu.NONE, 2, 2, "Commenter en tant que");
        }else {
            popupMenu.getMenu().add(Menu.NONE, 4, 4, "Envoyer un message");
        }
        popupMenu.getMenu().add(Menu.NONE, 3, 3, "Voir le profile");
        if (user_id.contains("resto") && !user_id.equals("resto_" + FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            popupMenu.getMenu().add(Menu.NONE, 6, 6, "Noter ce restaurant");
            popupMenu.getMenu().add(Menu.NONE, 5, 5, "Voir tous les menus");
        }

        //item click listener
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int item_id = item.getItemId();
                if (item_id == 0){
                    //option delete is checked
                    avertissement(post_id, post_image1, post_image2, post_image3);
                }else if (item_id == 1) {
                    //option edit is checked
                    editPostDescription(post_id, post_description);
                }else if (item_id == 2) {
                    //option comment as is checked
                    changeAccount();
                    Toast.makeText(PostDetailsActivity.this, "comment as ... currentUser or resto", Toast.LENGTH_SHORT).show();
                }else if (item_id == 3) {
                    //option show profile is checked
                    if (user_id.equals(mCurrentUserId)) { //set currentUser to his profile
                        if (!getClass().equals(ProfileUserActivity.class)) {
                            Intent intent = new Intent(PostDetailsActivity.this, ProfileUserActivity.class);
                            startActivity(intent);
                        }
                    }else if (user_id.equals("resto_" + mCurrentUserId)) { //send currentUser to hid resto profile
                        if (!getClass().equals(ProfileRestoActivity.class)) {
                            Intent intent = new Intent(PostDetailsActivity.this, ProfileRestoActivity.class);
                            intent.putExtra("user_id", user_id);
                            startActivity(intent);
                        }
                    }else if (user_id.contains("resto") && !user_id.equals("resto_" + mCurrentUserId)) { //send currentUser to other resto profile
                        if (!getClass().equals(OtherRestoProfileActivity.class)) {
                            Intent intent = new Intent(PostDetailsActivity.this, OtherRestoProfileActivity.class);
                            intent.putExtra("id_resto", user_id);
                            startActivity(intent);
                        }
                    }else { //send currentUser to other currentUser profile
                        if (!getClass().equals(OtherUsersProfileActivity.class)) {
                            Intent intent = new Intent(PostDetailsActivity.this, OtherUsersProfileActivity.class);
                            intent.putExtra("user_id", user_id);
                            startActivity(intent);
                        }
                    }
                }else if (item_id == 4) {
                    //option send message is checked
                    Toast.makeText(PostDetailsActivity.this, "send message...\nwill implement later", Toast.LENGTH_LONG).show();
                }else if (item_id == 5) {
                    //voir tous les menus
                    Intent intent = new Intent(PostDetailsActivity.this, ListMenuRestoActivity.class);
                    intent.putExtra("key", user_id);
                    startActivity(intent);
                }else if (item_id == 6) {
                    //raitng selected
                    //check if currentUser has rating resto yet, if not show rating dialog
                    collectionReference_hasRatingResto.document(user_id).get() // here user_id == id_resto
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if (documentSnapshot.exists()) {
                                        if (documentSnapshot.get(mCurrentUserId) != null) {
                                            Toast.makeText(PostDetailsActivity.this, "Vous avez déjà noté ce restaurant", Toast.LENGTH_LONG).show();
                                        }else {
                                            showRatingDialog(user_id); // here user_id == id_resto
                                        }
                                    }
                                }
                            });
            }
                return false;
            }
        });
        //show menu
        popupMenu.show();
    }

    private void changeAccount() {
        String[] option = {"En tant que " + hidName, "EN tant que " + myName};
        //show alert dialog to choose account
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(option, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    //comment as his restaurant account
                    loadRestoInfo();
                }
                if (which == 1) {
                    // comment as his currentUser account
                    loadUserInfo();
                }
            }
        });

        //create and show dialog
        builder.create().show();

    }

    private void loadRestoInfo() {
        //get current currentUser info
        myUid = user_id;
        final DocumentReference documentReference_currentUserResto = FirebaseFirestore.getInstance().collection("Resto").document(user_id);
        documentReference_currentUserResto.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()){
                            documentReference_currentUserResto.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                @Override
                                public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                                    if (value != null) {
                                        myName_temp = "" + value.getString("name_resto");
                                        myPseudo = "" + value.getString("rating_resto");
                                        myProfile_image = "" + value.getString("logo_resto");

                                        //set image to comment view
                                        try {
                                            Picasso.get().load(myProfile_image).placeholder(R.drawable.ic_image_profile_icon_dark).into(comment_userProfileImage);
                                        }catch (Exception e){

                                        }
                                    }
                                }
                            });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(PostDetailsActivity.this, ""+e.getMessage(), Toast.LENGTH_LONG).show();
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

                                //set currentUser as having rate this restaurant
                                HashMap<String, Object> userRating = new HashMap<>();
                                userRating.put(firebaseUser.getUid(), "rating");
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
                                                Toast.makeText(PostDetailsActivity.this, "Merci pour votre appreciation", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        });
            }
        });

        //show dialog
        ratingDialog.show();
    }

    private void editPostDescription(final String post_id, final String post_description) {
        //custom dialog
        final Dialog dialog = new Dialog(PostDetailsActivity.this);
        dialog.setContentView(R.layout.dialog_edit_post_description);
        //set the custom dialog components
        final EditText editText_description = dialog.findViewById(R.id.et_postDescription);
        editText_description.setText(post_description);
        TextView annuler = dialog.findViewById(R.id.tv_annuler);
        TextView valider = dialog.findViewById(R.id.tv_valider);

        //annuler clicked
        annuler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        //valider clicked
        valider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get input text
                final String value = editText_description.getText().toString();
                Map<String, Object> result = new HashMap<>();
                result.put("post_description", value);
                //updata the value in database
                DocumentReference documentReferencePost = FirebaseFirestore.getInstance().collection("Publications").document(post_id);
                documentReferencePost.update(result)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(PostDetailsActivity.this, "Statut mis à jout", Toast.LENGTH_LONG).show();
                                dialog.dismiss();
                            }
                        });
            }
        });
        dialog.show();
    }

    private void avertissement(final String post_id, final String post_image1, final String post_image2, final String post_image3) {
        //BUILD ALERT DIALOG TO CONFIRM THE SUPPRESSION
        AlertDialog.Builder builder = new AlertDialog.Builder(PostDetailsActivity.this);
        builder.setMessage("Etes-vous sûr de vouloir supprimer cette publication ?");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "OUI",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        beginDelete(post_id, post_image1, post_image2, post_image3);
                    }
                });

        builder.setNegativeButton(
                "NON",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
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
        final ProgressDialog progressDialog_delete = new ProgressDialog(PostDetailsActivity.this);
        progressDialog_delete.setMessage("Suppressoin de le publication en cours...");
        progressDialog_delete.show();
        //we must delete image stored in Firebase storage
        //after that deleting post from Firestore
        StorageReference storagePickReference = FirebaseStorage.getInstance().getReferenceFromUrl(post_image);
        storagePickReference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(PostDetailsActivity.this, "Impossible de supprimer la publication", Toast.LENGTH_SHORT).show();
                progressDialog_delete.dismiss();
            }
        });
    }

    private void sendToMain() {
        startActivity(new Intent(PostDetailsActivity.this, MainActivity.class));
        finish();
    }

    private void deletePost(final String post_id) {
        final ProgressDialog progressDialog_delete = new ProgressDialog(PostDetailsActivity.this);
        progressDialog_delete.setMessage("Suppressoin de le publication en cours...");
        progressDialog_delete.show();

        //delete data from Firestore
        DocumentReference documentReference = collectionReference_post.document(post_id);
        documentReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(PostDetailsActivity.this, "Publication supprimer avec succès", Toast.LENGTH_SHORT).show();
                //delete post's comment
                final CollectionReference collectionReference_comment = FirebaseFirestore.getInstance().collection("Comments");
                collectionReference_comment.get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                if (!queryDocumentSnapshots.isEmpty()){
                                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()){
                                        if (queryDocumentSnapshots.getDocuments().contains(myUid)){
                                            String comment_id = documentSnapshot.getString("comment_time");
                                            collectionReference_comment.document(comment_id).delete();
                                        }
                                    }
                                }
                            }
                        });

                //delete post kiffs
                collectionReference_kiffs.document(post_id).delete();

                sendToMain();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(PostDetailsActivity.this, "Impossible de supprimer la publication !", Toast.LENGTH_SHORT).show();
                progressDialog_delete.dismiss();
            }
        });
    }

    //upload data to database
    private void postComment() {
        final String post_comment = commnet_addComment.getText().toString();
        if (post_comment.isEmpty()){
            Toast.makeText(this, "Votre commentaire est vide", Toast.LENGTH_SHORT).show();
        }else if (imageCompressed_uri == null){ //upload comment without image
            String comment_time = String.valueOf(System.currentTimeMillis());
            ModelComment modelComment = new ModelComment(comment_time, post_comment,post_id, myUid, myName_temp, myPseudo, myProfile_image, "noImage");
            //store data to database
            DocumentReference documentReference_comment = FirebaseFirestore.getInstance().collection("Comments").document(comment_time);//comment_time == the id of current currentUser's comment

            //put data to database
            documentReference_comment.set(modelComment, SetOptions.merge())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(PostDetailsActivity.this, "Commentaire envoyer", Toast.LENGTH_SHORT).show();

                            //clear the views when comment sent
                            commnet_addComment.setText("");
                            //to clear the focus on edit text of the layout adding comment when comment sended
                            commnet_addComment.clearFocus();
                            //to hide soft keyboard when comment sended
                            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                            inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);

                            updateCommentCount();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(PostDetailsActivity.this, "impossible d'envoyer votre commentaire\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }else {
            final String timestamp = String.valueOf(System.currentTimeMillis());
            String filePathAndName = storagePdPPath + timestamp + "_comment_image" + "_" + firebaseUser.getUid(); //nom de l'image

            //storing imagge to Firabase Storage
            StorageReference storageReference1 = storageReference.child(filePathAndName);
            storageReference1.putFile(imageCompressed_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @SuppressLint("LongLogTag")
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            String comment_time = String.valueOf(System.currentTimeMillis());
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful()){} //loop until task is complete
                            String comment_image = uriTask.getResult().toString();
                            ModelComment modelComment = new ModelComment(comment_time, post_comment,post_id, myUid, myName_temp, myPseudo, myProfile_image, comment_image);
                            //store data to database
                            DocumentReference documentReference_comment = FirebaseFirestore.getInstance().collection("Comments").document(comment_time);//comment_time == the id of current currentUser's comment

                            //put data to database
                            documentReference_comment.set(modelComment, SetOptions.merge())
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(PostDetailsActivity.this, "Commentaire envoyer", Toast.LENGTH_SHORT).show();

                                            //clear the views when comment sent
                                            commnet_addComment.setText("");
                                            imageCompressed_uri = null;
                                            imageAddedIntoComment.setImageURI(imageCompressed_uri);
                                            imageAddedIntoComment.setVisibility(View.GONE);

                                            //to clear the focus on edit text of the layout adding comment when comment sent
                                            commnet_addComment.clearFocus();
                                            //to hide soft keyboard when comment sent
                                            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                                            inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);

                                            updateCommentCount();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(PostDetailsActivity.this, "impossible d'envoyer votre commentaire\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("message important", "******************************" +e.getMessage());
                    Toast.makeText(PostDetailsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateCommentCount() {
        mProcessComment = true;
        final DocumentReference documentReference_post = FirebaseFirestore.getInstance().collection("Publications").document(post_id);
        documentReference_post.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            if (mProcessComment) {
                                comment_count = "" + documentSnapshot.getString("comment_count");
                                commentCount = Integer.parseInt(comment_count) + 1;
                                Map<String, Object> numberOfComment = new HashMap<>();
                                numberOfComment.put("comment_count", String.valueOf(commentCount));
                                documentReference_post.set(numberOfComment, SetOptions.mergeFields("comment_count"));
                                loadComments(post_id);
                                mProcessComment = false;
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

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
                imageAddedIntoComment.setVisibility(View.VISIBLE);
                imageAddedIntoComment.setImageURI(imageCompressed_uri);
            }
            if (requestCode == IMAGE_PICK_CAMERA_REQUEST_CODE){
                imageAddedIntoComment.setVisibility(View.VISIBLE);
                imageAddedIntoComment.setImageURI(imageCompressed_uri);
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

    private void loadUserInfo() {
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //get current currentUser info
        final DocumentReference documentReference_currentUser = FirebaseFirestore.getInstance().collection("Users").document(myUid);
        documentReference_currentUser.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()){
                            documentReference_currentUser.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                @Override
                                public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                                    if (value != null) {
                                        myName = "" + value.getString("name");
                                        myPseudo = "" + value.getString("pseudo");
                                        myProfile_image = "" + value.getString("profile_image");

                                        myName_temp = myName;
                                        //set image to comment view
                                        try {
                                            Picasso.get().load(myProfile_image).placeholder(R.drawable.ic_image_profile_icon_dark).into(comment_userProfileImage);
                                        }catch (Exception e){

                                        }
                                    }
                                }
                            });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(PostDetailsActivity.this, ""+e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void loadPostDetails() {
        //get post details by th post id from database
        final DocumentReference documentReference_postDetails = FirebaseFirestore.getInstance().collection("Publications").document(post_id);
        documentReference_postDetails.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            documentReference_postDetails.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                @SuppressLint("SetTextI18n")
                                @Override
                                public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                                    //get data
                                    if (value !=null) {
                                        postDescription = "" + value.getString("post_description");
                                        post_kiff = "" + value.getString("post_kiff");
                                        String postTime = "" + value.getString("post_time");
                                        postImage1 = "" + value.getString("post_image1");
                                        postImage2 = "" + value.getString("post_image2");
                                        postImage3 = "" + value.getString("post_image3");
                                        hisProfile_image = "" + value.getString("profile_image");
                                        String user_id = "" + value.getString("user_id");
                                        hisPseudo = "" + value.getString("pseudo");
                                        hidName= "" + value.getString("name");
                                        comment_count = "" + value.getString("comment_count");
                                        HashMap<String, Object> myLocation = (HashMap<String, Object>) value.getData();

                                        //convert time
                                        Calendar calendar = Calendar.getInstance(Locale.getDefault());
                                        try {
                                            calendar.setTimeInMillis(Long.parseLong(postTime));
                                        }catch (Exception e){
                                        }
                                        String pTemps = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

                                        //set data to views
                                        if ((myLocation != null && myLocation.isEmpty()) || myLocation == null) {
                                            location_button.setVisibility(View.GONE);
                                        }else {
                                            location_button.setVisibility(View.VISIBLE);
                                            location_button.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    Toast.makeText(PostDetailsActivity.this, "Voir Lieu => Send to map", Toast.LENGTH_LONG).show();
                                                }
                                            });
                                        }
                                        post_description.setText(postDescription);
                                        //set kiff number
                                        if (post_kiff.equals("0")){
                                            textView_postKiff.setVisibility(View.GONE);
                                        }else if (post_kiff.equals("1")){
                                            textView_postKiff.setVisibility(View.VISIBLE);
                                            textView_postKiff.setText(post_kiff + " Kiff");// set kiff word to singular
                                        }else {
                                            textView_postKiff.setVisibility(View.VISIBLE);
                                            textView_postKiff.setText(post_kiff + " Kiffs");// set kiff word to plural
                                        }
                                        //set comment number
                                        if (comment_count.equals("0")){
                                            textView_nbrPosComment.setVisibility(View.GONE);
                                        }else if (comment_count.equals("1")){
                                            textView_nbrPosComment.setVisibility(View.VISIBLE);
                                            textView_nbrPosComment.setText(comment_count + " commentaire");// set commentaire word to singular
                                        }else {
                                            textView_nbrPosComment.setVisibility(View.VISIBLE);
                                            textView_nbrPosComment.setText(comment_count + " commentaires");// set commentaire word to plural
                                        }
                                        post_time.setText(pTemps);
                                        user_name.setText(hidName);
                                        if (user_id.contains("resto")) {
                                            textView_pseudo.setVisibility(View.GONE);
                                            ratingBar.setVisibility(View.VISIBLE);
                                            ratingBar.setRating(Float.parseFloat(hisPseudo));
                                        }else {
                                            textView_pseudo.setVisibility(View.VISIBLE);
                                            ratingBar.setVisibility(View.GONE);
                                            textView_pseudo.setText(hisPseudo);
                                        }
                                        //set image1
                                        if (postImage1.equals("noImage")) {
                                            post_image1.setVisibility(View.GONE);
                                        }else {
                                            post_image1.setVisibility(View.VISIBLE);
                                            try {
                                                Picasso.get().load(postImage1).into(post_image1);
                                            } catch (Exception e) { }
                                        }
                                        //set image2
                                        if (postImage2.equals("noImage")) {
                                            post_image2.setVisibility(View.GONE);
                                        }else {
                                            post_image2.setVisibility(View.VISIBLE);
                                            try {
                                                Picasso.get().load(postImage2).into(post_image2);
                                            } catch (Exception e) { }
                                        }
                                        //set image3
                                        if (postImage3.equals("noImage")) {
                                            post_image3.setVisibility(View.GONE);
                                        }else {
                                            post_image3.setVisibility(View.VISIBLE);
                                            try {
                                                Picasso.get().load(postImage3).into(post_image3);
                                            } catch (Exception e) { }
                                        }
                                        if (postImage2.equals("noImage") && postImage3.equals("noImage")) {
                                            linearLayout_image23.setVisibility(View.GONE);
                                        }

                                        //set his profile image
                                        try{
                                            Picasso.get().load(hisProfile_image).placeholder(R.drawable.ic_image_profile_icon_dark).into(user_profileImage);
                                        }catch (Exception e){ }

                                        //set toolBar title
                                        PostDetailsActivity.this.setTitle(hidName);

                                        //load post comment when post details loaded
                                        loadComments(post_id);

                                        progressDialog_loadComment.dismiss();

                                        //button kiff clicked
                                        kiff_button.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                kiffsGestion(post_kiff, post_id);
                                            }
                                        });

                                        //post image clicked, go to ShowImageActivity
                                        post_image1.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent intent2 = new Intent(PostDetailsActivity.this, ShowImageActivity.class);
                                                intent2.putExtra("showImage", postImage1);
                                                startActivity(intent2);
                                            }
                                        });
                                        post_image2.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent intent2 = new Intent(PostDetailsActivity.this, ShowImageActivity.class);
                                                intent2.putExtra("showImage", postImage2);
                                                startActivity(intent2);
                                            }
                                        });
                                        post_image3.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent intent2 = new Intent(PostDetailsActivity.this, ShowImageActivity.class);
                                                intent2.putExtra("showImage", postImage3);
                                                startActivity(intent2);
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(PostDetailsActivity.this, ""+e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void kiffsGestion(final String kiffNumber, final String post_id) {
        //get total number of kiffs for the post
        final int postKiff = Integer.parseInt(kiffNumber);
        mPressKiff = true;
        //get id of the post clicked
        final String postId = post_id;
        collectionReference_kiffs.document(postId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (mPressKiff){
                    String kiffs;
                    CollectionReference collectionReference_post = FirebaseFirestore.getInstance().collection("Publications");
                    if (value.get(myUid) != null) {
                        //already kiffed, so remove kiff
                        Map<String, Object> kiffCounted = new HashMap<>();
                        kiffs = String.valueOf(postKiff - 1);
                        kiffCounted.put("post_kiff", kiffs);
                        collectionReference_post.document(postId).update(kiffCounted);
                        collectionReference_kiffs.document(postId).update(myUid, FieldValue.delete());
                        mPressKiff = false;
                    } else {
                        //not kiff, kiff it
                        Map<String, Object> kiffNbr = new HashMap<>();
                        kiffs = String.valueOf(postKiff + 1);
                        kiffNbr.put("post_kiff", kiffs);
                        Map<String, Object> userWhoKiffs = new HashMap<>();
                        userWhoKiffs.put(myUid, "je kiff");
                        collectionReference_post.document(postId).update(kiffNbr);
                        collectionReference_kiffs.document(postId).set(userWhoKiffs, SetOptions.merge());
                        mPressKiff = false;
                    }
                }
            }
        });
    }

    private void setKiffs(final String post_id) {
        collectionReference_kiffs.document(post_id).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                assert value != null;
                if (value.get(myUid) != null){
                    //currentUser has kiffed the post
                    //change button icon
                    kiff_button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_kiff_icon_dark,0,0,0);
                }else {
                    //currentUser has not kiff this post
                    kiff_button.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_jkiff_icon_dark,0,0,0);
                }
            }
        });
    }
}
