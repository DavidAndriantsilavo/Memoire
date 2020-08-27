package mg.didavid.firsttry.Controllers.Adapteurs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.model.DocumentCollections;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import mg.didavid.firsttry.Controllers.Activities.ListMenuRestoActivity;
import mg.didavid.firsttry.Controllers.Activities.OtherRestoProfileActivity;
import mg.didavid.firsttry.Controllers.Activities.OtherUsersProfileActivity;
import mg.didavid.firsttry.Controllers.Activities.PostDetailsActivity;
import mg.didavid.firsttry.Controllers.Activities.ProfileRestoActivity;
import mg.didavid.firsttry.Controllers.Activities.ProfileUserActivity;
import mg.didavid.firsttry.Controllers.Activities.ShowImageActivity;
import mg.didavid.firsttry.Controllers.Activities.ShowWhoKiffAvtivity;
import mg.didavid.firsttry.Models.ModelePost;
import mg.didavid.firsttry.R;

public class AdapteursPost extends RecyclerView.Adapter<AdapteursPost.MyHolder>{

    private Context context;
    private List<ModelePost> postList;

    private CollectionReference collectionReference_hasRatingResto = FirebaseFirestore.getInstance().collection("HasRatingResto");

    private String mCurrentUserId;
    private int postKiff;
    private CollectionReference collectionReference_post;
    private CollectionReference collectionReference_kiffs;
    private boolean mPressKiff = false, kiffFirstPressed = false;


    public AdapteursPost(Context context, List<ModelePost> postList) {
        this.context = context;
        this.postList = postList;
        mCurrentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        collectionReference_post = FirebaseFirestore.getInstance().collection("Publications");
        collectionReference_kiffs = FirebaseFirestore.getInstance().collection("Kiffs");
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout row_post.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_post, parent, false);
        return new MyHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, final int position) {
        //get data
        final String user_id = postList.get(position).getUser_id();
        final String name = postList.get(position).getName();
        final String pseudo = postList.get(position).getPseudo();
        final String profile_image = postList.get(position).getProfile_image();
        final String post_id = postList.get(position).getPost_id();
        final String post_description = postList.get(position).getPost_description();
        final String post_image1 = postList.get(position).getPost_image1();
        final String post_image2 = postList.get(position).getPost_image2();
        final String post_image3 = postList.get(position).getPost_image3();
        String post_timeStamp = postList.get(position).getPost_time();
        final String nbrPostKiffs = postList.get(position).getPost_kiff();
        final String nbrPostComments = postList.get(position).getComment_count();

        //convert timeStamp to dd/mm/yyyy hh:mm am/pm
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        try {
            calendar.setTimeInMillis(Long.parseLong(post_timeStamp));
        }catch (Exception e){
        }
        final String pTemps = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        DocumentReference documentReference_post = FirebaseFirestore.getInstance().collection("Publications").document(post_id);
        documentReference_post.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                String  name_ds = "",
                        profile_image_ds = "",
                        post_description_ds = "",
                        post_kiff_ds = "",
                        comment_count_ds = "";
                float pseudo_ds = 0;
                if (value != null) {
                    name_ds = value.getString("name");
                    profile_image_ds = value.getString("profile_image");
                    post_description_ds = value.getString("post_description");
                    post_kiff_ds = value.getString("post_kiff");
                    comment_count_ds = value.getString("comment_count");
                    if (user_id.contains("resto")) {
                        pseudo_ds = Float.parseFloat(value.getString("pseudo"));
                    }
                }
                //set data
                try {
                    holder.uNameTv.setText(name_ds);
                    holder.pDescriptionTv.setText(post_description_ds);

                    if (user_id.contains("resto")) {
                        holder.pseudo.setVisibility(View.GONE);
                        holder.ratingBar.setVisibility(View.VISIBLE);
                        holder.ratingBar.setRating(pseudo_ds);
                    }

                    //set user profile image
                    try{
                        Picasso.get().load(profile_image_ds).placeholder(R.drawable.ic_image_profile_icon_dark).into(holder.uPictureIv);
                    }catch (Exception e){

                    }
                    //manage kiff text view
                    if (post_kiff_ds.equals("0")) {
                        holder.pKiffTv.setVisibility(View.GONE);
                    } else if (post_kiff_ds.equals("1")) {
                        holder.pKiffTv.setVisibility(View.VISIBLE);
                        holder.pKiffTv.setText(post_kiff_ds + " Kiff");// set kiff word to singular
                    } else {
                        holder.pKiffTv.setVisibility(View.VISIBLE);
                        holder.pKiffTv.setText(post_kiff_ds + " Kiffs");// set kiff word to plural
                    }

                    //manage comment text view
                    if (comment_count_ds.equals("0")) {
                        holder.pComment.setVisibility(View.GONE);
                    } else if (comment_count_ds.equals("1")) {
                        holder.pComment.setVisibility(View.VISIBLE);
                        holder.pComment.setText(comment_count_ds + " commentaire");// set commentaire word to singular
                    } else {
                        holder.pComment.setVisibility(View.VISIBLE);
                        holder.pComment.setText(comment_count_ds + " commentaires");// set commentaire word to plural
                    }

                }catch (Exception e){
                }

                final String finalPost_kiff_ds = post_kiff_ds;
                holder.kiffBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!kiffFirstPressed) {
                            kiffsGestion(post_id, nbrPostKiffs);
                        }else {
                            kiffsGestion(post_id, finalPost_kiff_ds);
                        }
                    }
                });
            }
        });

        //set data
        try {
            if (user_id.contains("resto")){
                holder.pseudo.setVisibility(View.GONE);
                holder.ratingBar.setVisibility(View.VISIBLE);
                holder.ratingBar.setRating(Float.parseFloat(pseudo));
            }else {
                holder.ratingBar.setVisibility(View.GONE);
                holder.pseudo.setVisibility(View.VISIBLE);
                holder.pseudo.setText(pseudo);
            }
            holder.pTimeTv.setText(pTemps);
            holder.uNameTv.setText(name);
            holder.pDescriptionTv.setText(post_description);

        }catch (Exception e){
        }
        //set user profile image
        try{
            Picasso.get().load(profile_image).placeholder(R.drawable.ic_image_profile_icon_dark).into(holder.uPictureIv);
        }catch (Exception e){

        }
        //set post image
        if (post_image1.equals("noImage")) {
            holder.pImageIv1.setVisibility(View.GONE);
        }else {
            holder.pImageIv1.setVisibility(View.VISIBLE);
            try {
                Picasso.get().load(post_image1).into(holder.pImageIv1);
            } catch (Exception e) {

            }
        }
        if (post_image2.equals("noImage")) {
            holder.pImageIv2.setVisibility(View.GONE);
        }else {
            holder.pImageIv2.setVisibility(View.VISIBLE);
            try {
                Picasso.get().load(post_image2).into(holder.pImageIv2);
            } catch (Exception e) {

            }
        }
        if (post_image3.equals("noImage")) {
            holder.pImageIv3.setVisibility(View.GONE);
        }else {
            holder.pImageIv3.setVisibility(View.VISIBLE);
            try {
                Picasso.get().load(post_image3).into(holder.pImageIv3);
            } catch (Exception e) {

            }
        }
        if (post_image2.equals("noImage") && post_image3.equals("noImage")) {
            holder.pImageIv1.setAdjustViewBounds(true);
        }

        //image post clicked
        holder.pImageIv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!post_image1.equals("noImage")) {
                    Intent intent = new Intent(context, ShowImageActivity.class);
                    intent.putExtra("showImage", post_image1);
                    context.startActivity(intent);
                }
            }
        });
        holder.pImageIv2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!post_image2.equals("noImage")) {
                    Intent intent = new Intent(context, ShowImageActivity.class);
                    intent.putExtra("showImage", post_image2);
                    context.startActivity(intent);
                }
            }
        });
        holder.pImageIv3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!post_image3.equals("noImage")) {
                    Intent intent = new Intent(context, ShowImageActivity.class);
                    intent.putExtra("showImage", post_image3);
                    context.startActivity(intent);
                }
            }
        });

        //handle click
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions(holder.moreBtn, user_id, mCurrentUserId, post_id, post_image1, post_image2, post_image3, post_description);
            }
        });

        holder.commenterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start PostDetailsActivity
                Intent intent = new Intent(context, PostDetailsActivity.class);
                intent.putExtra("post_id", post_id);
                intent.putExtra("user_id", user_id);
                context.startActivity(intent);
            }
        });
        holder.partagerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Partager...\nwill implement later", Toast.LENGTH_SHORT).show();
            }
        });

        //user name clicked
        holder.uNameTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user_id.equals(mCurrentUserId)) { //set user to his profile
                    if (!context.getClass().equals(ProfileUserActivity.class)) {
                        Intent intent = new Intent(context, ProfileUserActivity.class);
                        context.startActivity(intent);
                    }
                }else if (user_id.equals("resto_" + mCurrentUserId)) { //send user to hid resto profile
                    if (!context.getClass().equals(ProfileRestoActivity.class)) {
                        Intent intent = new Intent(context, ProfileRestoActivity.class);
                        intent.putExtra("user_id", user_id);
                        context.startActivity(intent);
                    }
                }else if (user_id.contains("resto") && !user_id.equals("resto_" + mCurrentUserId)) { //send user to other resto profile
                    if (!context.getClass().equals(OtherRestoProfileActivity.class)) {
                        Intent intent = new Intent(context, OtherRestoProfileActivity.class);
                        intent.putExtra("id_resto", user_id);
                        context.startActivity(intent);
                    }
                }else { //send user to other user profile
                    if (!context.getClass().equals(OtherUsersProfileActivity.class)) {
                        Intent intent = new Intent(context, OtherUsersProfileActivity.class);
                        intent.putExtra("user_id", user_id);
                        context.startActivity(intent);
                    }
                }
            }
        });

        //image profile clicked
        holder.uPictureIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user_id.equals(mCurrentUserId)) { //set user to his profile
                    if (!context.getClass().equals(ProfileUserActivity.class)) {
                        Intent intent = new Intent(context, ProfileUserActivity.class);
                        context.startActivity(intent);
                    }
                }else if (user_id.equals("resto_" + mCurrentUserId)) { //send user to hid resto profile
                    if (!context.getClass().equals(ProfileRestoActivity.class)) {
                        Intent intent = new Intent(context, ProfileRestoActivity.class);
                        intent.putExtra("user_id", user_id);
                        context.startActivity(intent);
                    }
                }else if (user_id.contains("resto") && !user_id.equals("resto_" + mCurrentUserId)) { //send user to other resto profile
                    if (!context.getClass().equals(OtherRestoProfileActivity.class)) {
                        Intent intent = new Intent(context, OtherRestoProfileActivity.class);
                        intent.putExtra("id_resto", user_id);
                        context.startActivity(intent);
                    }
                }else { //send user to other user profile
                    if (!context.getClass().equals(OtherUsersProfileActivity.class)) {
                        Intent intent = new Intent(context, OtherUsersProfileActivity.class);
                        intent.putExtra("user_id", user_id);
                        context.startActivity(intent);
                    }
                }
            }
        });

        //linear layout of user details clicked, go to PostDetailsActivity
        holder.pDescriptionTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PostDetailsActivity.class);
                intent.putExtra("post_id", post_id);
                context.startActivity(intent);
            }
        });

        //click kiff count to show who kiffs the post
        holder.pKiffTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ShowWhoKiffAvtivity.class);
                intent.putExtra("key", post_id);
                context.startActivity(intent);
            }
        });

        //set kiff for its post
        setKiffs(holder, post_id);
    }

    private void kiffsGestion(final String post_id, final String nbrKiffs) {
        mPressKiff = true;
        kiffFirstPressed = true;
        final String postId = post_id;
        collectionReference_kiffs.document(postId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (mPressKiff){
                    //get id of the post clicked
                    //get total number of kiffs for the post
                    postKiff = Integer.parseInt(nbrKiffs);
                    String kiffs;
                    if (value.get(mCurrentUserId) != null) {
                        //already kiffed, so remove kiff
                        Map<String, Object> kiffCounted = new HashMap<>();
                        kiffs = String.valueOf(postKiff - 1);
                        kiffCounted.put("post_kiff", kiffs);
                        collectionReference_post.document(postId).update(kiffCounted);
                        collectionReference_kiffs.document(postId).update(mCurrentUserId, FieldValue.delete());
                        mPressKiff = false;
                    } else {
                        //not kiff, kiff it
                        Map<String, Object> kiffNbr = new HashMap<>();
                        kiffs = String.valueOf(postKiff + 1);
                        kiffNbr.put("post_kiff", kiffs);
                        Map<String, Object> userWhoKiffs = new HashMap<>();
                        userWhoKiffs.put(mCurrentUserId, "je kiff");
                        collectionReference_post.document(postId).update(kiffNbr);
                        collectionReference_kiffs.document(postId).set(userWhoKiffs, SetOptions.merge());
                        mPressKiff = false;
                    }
                }
            }
        });
    }

    private void setKiffs(final MyHolder holder, final String post_id) {
        collectionReference_kiffs.document(post_id).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                assert value != null;
                if (value.get(mCurrentUserId) != null){
                    //user has kiffed the post
                    //change button icon
                    holder.kiffBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_kiff_icon_dark,0,0,0);
                }else {
                    //user has not kiff this post
                    holder.kiffBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_jkiff_icon_dark,0,0,0);
                }
            }
        });
    }

    private void showMoreOptions(ImageButton moreBtn, final String user_id, final String mCurrentUserId, final String post_id, final String post_image1, final String post_image2, final String post_image3, final String post_description) {
        //create popup menu
        PopupMenu popupMenu = new PopupMenu(context, moreBtn, Gravity.END);
        //show popup menu in only posts of currently singed-in user
        if (user_id.contains(mCurrentUserId)){
            //add item in menu
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Supprimer la publication");
            popupMenu.getMenu().add(Menu.NONE, 1, 1, "Modifier la publication");
            popupMenu.getMenu().add(Menu.NONE, 5, 5, "Voir tous les menus");
        }else {
            popupMenu.getMenu().add(Menu.NONE, 4, 4, "Envoyer un message");
        }
        popupMenu.getMenu().add(Menu.NONE, 2, 2, "Commenter la publication");
        popupMenu.getMenu().add(Menu.NONE, 3, 3, "Voir le profile");
        if (user_id.contains("resto") && !user_id.equals("resto_" + mCurrentUserId)) {
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
                    //option comment is checked
                    Intent intent = new Intent(context, PostDetailsActivity.class);
                    intent.putExtra("post_id", post_id);
                    context.startActivity(intent);
                }else if (item_id == 3) {
                    //option show profile is checked
                    if (user_id.equals(mCurrentUserId)) { //set user to his profile
                        if (!context.getClass().equals(ProfileUserActivity.class)) {
                            Intent intent = new Intent(context, ProfileUserActivity.class);
                            context.startActivity(intent);
                        }
                    }else if (user_id.equals("resto_" + mCurrentUserId)) { //send user to hid resto profile
                        if (!context.getClass().equals(ProfileRestoActivity.class)) {
                            Intent intent = new Intent(context, ProfileRestoActivity.class);
                            intent.putExtra("user_id", user_id);
                            context.startActivity(intent);
                        }
                    }else if (user_id.contains("resto") && !user_id.equals("resto_" + mCurrentUserId)) { //send user to other resto profile
                        if (!context.getClass().equals(OtherRestoProfileActivity.class)) {
                            Intent intent = new Intent(context, OtherRestoProfileActivity.class);
                            intent.putExtra("id_resto", user_id);
                            context.startActivity(intent);
                        }
                    }else { //send user to other user profile
                        if (!context.getClass().equals(OtherUsersProfileActivity.class)) {
                            Intent intent = new Intent(context, OtherUsersProfileActivity.class);
                            intent.putExtra("user_id", user_id);
                            context.startActivity(intent);
                        }
                    }
                }else if (item_id == 4) {
                    //option send message is checked
                    Toast.makeText(context, "send message...\nwill implement later", Toast.LENGTH_LONG).show();
                }else if (item_id == 5) {
                    //voir tous les menus
                    Intent intent = new Intent(context, ListMenuRestoActivity.class);
                    intent.putExtra("key", user_id);
                    context.startActivity(intent);
                }else if (item_id == 6) {
                    //raitng selected
                    //check if user has rating resto yet, if not show rating dialog
                    collectionReference_hasRatingResto.document(user_id).get() // here user_id == id_resto
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if (documentSnapshot.exists()) {
                                        if (documentSnapshot.get(mCurrentUserId) != null) {
                                            Toast.makeText(context, "Vous avez déjà noté ce restaurant", Toast.LENGTH_LONG).show();
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

    private void showRatingDialog(final String id_resto) {
        //create dialog
        final Dialog ratingDialog = new Dialog(context);
        ratingDialog.setContentView(R.layout.rating_dialog);
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

                                //set user as having rate this restaurant
                                HashMap<String, Object> userRating = new HashMap<>();
                                userRating.put(mCurrentUserId, "rating");
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
                                                Toast.makeText(context, "Merci pour votre appreciation", Toast.LENGTH_SHORT).show();
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
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.edit_post_description);
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
                    final DocumentReference documentReferencePost = FirebaseFirestore.getInstance().collection("Publications").document(post_id);
                    documentReferencePost.update(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(context, "Statut mis à jour", Toast.LENGTH_LONG).show();
                                    dialog.dismiss();
                                }
                            });
            }
        });
        dialog.show();
    }

    private void avertissement(final String post_id, final String post_image1, final String post_image2, final String post_image3) {
        //BUILD ALERT DIALOG TO CONFIRM THE SUPPRESSION
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
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
        if (!post_image.equals("noImage")) {
            final ProgressDialog progressDialog_delete = new ProgressDialog(context);
            progressDialog_delete.setMessage("Suppressoin de le publication en cours...");
            progressDialog_delete.show();
            //we must delete image stored in Firebase storage
            //after that deleting post from Firestore
            StorageReference storagePickReference = FirebaseStorage.getInstance().getReferenceFromUrl(post_image);
            storagePickReference.delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressDialog_delete.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context, "Impossible de supprimer la publication", Toast.LENGTH_SHORT).show();
                    progressDialog_delete.dismiss();
                }
            });
        }
    }

    private void deletePost(final String post_id) {
        final ProgressDialog progressDialog_delete = new ProgressDialog(context);
        progressDialog_delete.setMessage("Suppressoin de le publication en cours...");
        progressDialog_delete.show();
        //delete data from Firestore
        DocumentReference documentReference = collectionReference_post.document(post_id);
        documentReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context, "Publication supprimer avec succès", Toast.LENGTH_SHORT).show();
                //delete post's comment
                final CollectionReference documentReference1 = FirebaseFirestore.getInstance().collection("Comments");
                documentReference1.get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                if (!queryDocumentSnapshots.isEmpty()){
                                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()){
                                        if (queryDocumentSnapshots.getDocuments().contains(mCurrentUserId)){
                                            String comment_id = documentSnapshot.getString("comment_time");
                                            documentReference1.document(comment_id).delete();
                                            progressDialog_delete.dismiss();
                                        }
                                    }
                                }
                            }
                        });

                //delete post kiffs
                collectionReference_kiffs.document(post_id).delete();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Impossible de supprimer la publication !", Toast.LENGTH_SHORT).show();
                progressDialog_delete.dismiss();
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    //view holder class
    class MyHolder extends RecyclerView.ViewHolder{

        //views from row_post.xml
        ImageView uPictureIv, pImageIv1, pImageIv2, pImageIv3;
        TextView uNameTv, pTimeTv, pDescriptionTv, pKiffTv, pComment, pseudo;
        ImageButton moreBtn;
        Button kiffBtn, commenterBtn, partagerBtn;
        RatingBar ratingBar;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init views
            uPictureIv = itemView.findViewById(R.id.imageView_photoDeProfile_actu);
            pImageIv1 = itemView.findViewById(R.id.imageView_imagePost1_actu);
            pImageIv2 = itemView.findViewById(R.id.imageView_imagePost2_actu);
            pImageIv3 = itemView.findViewById(R.id.imageView_imagePost3_actu);
            uNameTv = itemView.findViewById(R.id.textView_nomUser_actu);
            pTimeTv = itemView.findViewById(R.id.textView_temps_actu);
            pseudo = itemView.findViewById(R.id.texteView_pseudo);
            pDescriptionTv = itemView.findViewById(R.id.textView_descriptionPost_actu);
            pKiffTv = itemView.findViewById(R.id.texteView_kiffs_actu);
            pComment = itemView.findViewById(R.id.texteView_comment_actu);
            moreBtn = itemView.findViewById(R.id.button_moreAction_actu);
            kiffBtn = itemView.findViewById(R.id.button_kiff_actu);
            commenterBtn = itemView.findViewById(R.id.button_commenter_actu);
            partagerBtn = itemView.findViewById(R.id.button_partager_actu);
            ratingBar = itemView.findViewById(R.id.ratingBar_newPost);
        }
    }
}
