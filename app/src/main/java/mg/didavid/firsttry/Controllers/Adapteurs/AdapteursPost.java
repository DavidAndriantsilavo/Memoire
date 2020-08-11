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
import android.widget.LinearLayout;
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
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import mg.didavid.firsttry.Controllers.Activities.OtherUsersProfileActivity;
import mg.didavid.firsttry.Controllers.Activities.PostDetailsActivity;
import mg.didavid.firsttry.Controllers.Activities.ProfileUserActivity;
import mg.didavid.firsttry.Controllers.Activities.ShowImageActivity;
import mg.didavid.firsttry.Models.ModelePost;
import mg.didavid.firsttry.R;

public class AdapteursPost extends RecyclerView.Adapter<AdapteursPost.MyHolder>{

    private Context context;
    private List<ModelePost> postList;

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
        final String post_image = postList.get(position).getPost_image();
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
                        post_time_ds = "",
                        post_kiff_ds = "",
                        comment_count_ds = "";
                if (value != null) {
                    name_ds = value.getString("name");
                    profile_image_ds = value.getString("profile_image");
                    post_description_ds = value.getString("post_description");
                    post_kiff_ds = value.getString("post_kiff");
                    comment_count_ds = value.getString("comment_count");
                }
                //convert timeStamp to dd/mm/yyyy hh:mm am/pm
                Calendar calendar = Calendar.getInstance(Locale.getDefault());
                try {
                    calendar.setTimeInMillis(Long.parseLong(post_time_ds));
                }catch (Exception e){
                }
                final String pTemps = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
                //set data
                try {
                    holder.uNameTv.setText(name_ds);
                    holder.pTimeTv.setText(pTemps);
                    holder.pDescriptionTv.setText(post_description_ds);

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
                //set user profile image
                try{
                    Picasso.get().load(profile_image_ds).placeholder(R.drawable.ic_image_profile_icon_dark).into(holder.uPictureIv);
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
            holder.pseudo.setText(pseudo);
            holder.pTimeTv.setText(pTemps);

        }catch (Exception e){
        }

        //set post image
        if (post_image.equals("noImage")) {
            holder.pImageIv.setVisibility(View.GONE);
        }else {
            holder.pImageIv.setVisibility(View.VISIBLE);
            try {
                Picasso.get().load(post_image).into(holder.pImageIv);
            } catch (Exception e) {

            }
        }

        //image post clicked
        holder.pImageIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!post_image.equals("noImage")) {
                    Intent intent = new Intent(context, ShowImageActivity.class);
                    intent.putExtra("showImage", post_image);
                    context.startActivity(intent);
                }
            }
        });

        //handle click
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions(holder.moreBtn, user_id, mCurrentUserId, post_id, post_image, post_description);
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
                if (user_id.equals(mCurrentUserId)) {
                    if (!context.getClass().equals(ProfileUserActivity.class)) {
                        Intent intent = new Intent(context, ProfileUserActivity.class);
                        context.startActivity(intent);
                    }
                }else {
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
                if (user_id.equals(mCurrentUserId)) {
                    if (!context.getClass().equals(ProfileUserActivity.class)) {
                        Intent intent = new Intent(context, ProfileUserActivity.class);
                        context.startActivity(intent);
                    }
                }else {
                    if (!context.getClass().equals(OtherUsersProfileActivity.class)) {
                        Intent intent = new Intent(context, OtherUsersProfileActivity.class);
                        intent.putExtra("user_id", user_id);
                        context.startActivity(intent);
                    }
                }
            }
        });

        //linear layout of user details clicked, go to PostDetailsActivity
        holder.user_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PostDetailsActivity.class);
                intent.putExtra("post_id", post_id);
                context.startActivity(intent);
            }
        });

        //set kiff for its post
        setKiffs(holder, post_id);
    }

    private void kiffsGestion(final String post_id, final String nbrKiffs) {
        mPressKiff = true;
        kiffFirstPressed = true;
        //get id of the post clicked
        final String postId = post_id;
        collectionReference_kiffs.document(postId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (mPressKiff){
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

    private void showMoreOptions(ImageButton moreBtn, final String user_id, final String mCurrentUserId, final String post_id, final String post_image, final String post_description) {
        //create popup menu
        PopupMenu popupMenu = new PopupMenu(context, moreBtn, Gravity.END);
        //show popup menu in only posts of currently singed-in user
        if (user_id.equals(mCurrentUserId)){
            //add item in menu
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Supprimer la publication");
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "Modifier la publication");
        }
        popupMenu.getMenu().add(Menu.NONE, 2, 0, "Commenter la publication");
        popupMenu.getMenu().add(Menu.NONE, 3, 0, "Voir le profile");
        popupMenu.getMenu().add(Menu.NONE, 4, 0, "Envoyer un message");

        //item click listener
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int item_id = item.getItemId();
                if (item_id == 0){
                    //option delete is checked
                    avertissement(post_id, post_image);
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
                    if (user_id.equals(mCurrentUserId)) {
                        if (!context.getClass().equals(ProfileUserActivity.class)) {
                            Intent intent = new Intent(context, ProfileUserActivity.class);
                            context.startActivity(intent);
                        }
                    }else {
                        if (!context.getClass().equals(OtherUsersProfileActivity.class)) {
                            Intent intent = new Intent(context, OtherUsersProfileActivity.class);
                            intent.putExtra("user_id", user_id);
                            context.startActivity(intent);
                        }
                    }
                }else if (item_id == 4) {
                    //option send message is checked
                    Toast.makeText(context, "send message...\nwill implement later", Toast.LENGTH_LONG).show();
                }
                return false;
            }
        });
        //show menu
        popupMenu.show();
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

    private void avertissement(final String post_id, final String post_image) {
        //BUILD ALERT DIALOG TO CONFIRM THE SUPPRESSION
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Etes-vous sûr de vouloir supprimer ce commentaire ?");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "OUI",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        beginDelete(post_id, post_image);
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

    private void beginDelete(String post_id, String post_image) {
        if (post_image.equals("noImage")){
            //delete post without image
            deletWithoutImage(post_id);
        }else {
            //delete with image
            deleteWithImage(post_id, post_image);
        }
    }

    private void deleteWithImage(final String post_id, String post_image) {
        final ProgressDialog progressDialog_delete = new ProgressDialog(context);
        progressDialog_delete.setMessage("Suppressoin de la publication en cours...");
        progressDialog_delete.show();

        //we must delete image stored in Firebase storage
        //after that deleting post from Firestore
        StorageReference storagePickReference = FirebaseStorage.getInstance().getReferenceFromUrl(post_image);
        storagePickReference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //image deleted, now delete data on database
                        DocumentReference documentReference = collectionReference_post.document(post_id);
                        documentReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(context, "Publication supprimer avec succès", Toast.LENGTH_SHORT).show();
                                progressDialog_delete.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, "Impossible de supprimer la publication !", Toast.LENGTH_SHORT).show();
                                progressDialog_delete.dismiss();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Impossible de supprimer la publication", Toast.LENGTH_SHORT).show();
                progressDialog_delete.dismiss();
            }
        });
    }

    private void deletWithoutImage(final String post_id) {
        final ProgressDialog progressDialog_delete = new ProgressDialog(context);
        progressDialog_delete.setMessage("Suppressoin de le publication en cours...");
        progressDialog_delete.show();

        //delete data from Firestore
        DocumentReference documentReference = collectionReference_post.document(post_id);
        documentReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context, "Publication supprimer avec succès", Toast.LENGTH_SHORT).show();
                progressDialog_delete.dismiss();
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
        ImageView uPictureIv, pImageIv;
        TextView uNameTv, pTimeTv, pDescriptionTv, pKiffTv, pComment, pseudo;
        ImageButton moreBtn;
        Button kiffBtn, commenterBtn, partagerBtn;
        LinearLayout user_details;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init views
            uPictureIv = itemView.findViewById(R.id.imageView_photoDeProfile_actu);
            pImageIv = itemView.findViewById(R.id.imageView_imagePost_actu);
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
            user_details = itemView.findViewById(R.id.linearLayout_userDetails);
        }
    }
}
