package mg.didavid.firsttry.Controllers.Adapteurs;

import android.app.Activity;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import mg.didavid.firsttry.Controllers.Activities.ProfileUserActivity;
import mg.didavid.firsttry.Controllers.Activities.ShowImageActivity;
import mg.didavid.firsttry.Models.ModelComment;
import mg.didavid.firsttry.R;

public class AdapteurComments extends RecyclerView.Adapter<AdapteurComments.MyHolder> {

    Context context;
    List<ModelComment> commentList;

    String mCurrentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    CollectionReference collectionReference_comment = FirebaseFirestore.getInstance().collection("Comments");

    public AdapteurComments(Context context, List<ModelComment> commentList) {
        this.context = context;
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //bind the row_comments.xml layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_comments, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, int position) {
        //get data
        final String comment_time = commentList.get(position).getComment_time();
        final String post_comment = commentList.get(position).getPost_comment();
        final String post_id = commentList.get(position).getPost_id();
        final String user_id = commentList.get(position).getUser_id();
        String name = commentList.get(position).getName();
        String pseudo = commentList.get(position).getPseudo();
        String profile_image = commentList.get(position).getProfile_image();
        final String comment_image = commentList.get(position).getComment_image();

        //convert timeStamp to dd/mm/yyyy hh:mm am/pm
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        try {
            calendar.setTimeInMillis(Long.parseLong(comment_time));
        }catch (Exception e){
        }
        String commentTemps = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        //set data
        //add document snapshot on view for updating data snapshotly
        // we just need to do this for the comment's content only because it can be changed
        DocumentReference documentReference_comment = FirebaseFirestore.getInstance().collection("Comments").document(comment_time);
        documentReference_comment
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (value != null){
                            String comment_content = value.getString("post_comment");
                            if (comment_content != null && !comment_content.isEmpty()) {
                                holder.commentContent_comment.setText(comment_content);
                            }
                        }
                    }
                });
        holder.userName_comment.setText(name);
        holder.userPseudo_comment.setText(pseudo);
        //holder.commentContent_comment.setText(post_comment);
        holder.commentTimeStamp_comment.setText(commentTemps);
        //set user image profile
        try {
            Picasso.get().load(profile_image).placeholder(R.drawable.ic_image_profile_icon_dark).into(holder.profileImage_comment);
        }catch (Exception e){ }
        //set comment image
        if (comment_image.equals("noImage")) {
            holder.image_comment.setVisibility(View.GONE);
        }else {
            holder.image_comment.setVisibility(View.VISIBLE);
            try {
                Picasso.get().load(comment_image).into(holder.image_comment);
            } catch (Exception e) { }
        }

        //Image profile of the user who comment clicked, then send to profile of this user
        holder.profileImage_comment.setOnClickListener(new View.OnClickListener() {
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

        //the name of the user who comment clicked, then send to profile of this user
        holder.userName_comment.setOnClickListener(new View.OnClickListener() {
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

        //image on comment clicked, send to ShowImageActivity
        holder.image_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ShowImageActivity.class);
                intent.putExtra("showImage", comment_image);
                context.startActivity(intent);
            }
        });

        //button more action in comment clicked
        holder.moreAction_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreActionsInComment(holder.moreAction_comment, user_id, post_comment, comment_time, comment_image, post_id);
            }
        });
    }

    private void showMoreActionsInComment(ImageButton moreAction_comment, String user_id, final String post_comment, final String comment_id, final String comment_image, final String post_id) {
        //create popup menu
        PopupMenu popupMenu = new PopupMenu(context, moreAction_comment, Gravity.END);
        //show popup menu in only posts of currently singed-in user
        if (user_id.equals(mCurrentUserId)){
            //add item in menu
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Modifier le commentaire");
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "Supprimer le commentaire");
        }

        //item click
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int item_id = item.getItemId();
                if (item_id == 0) {
                    //item "modifier le commentaire" clicked
                    editComment(comment_id, post_comment);
                }else if (item_id == 1) {
                    //item "supprimer le commentaire" clicked
                    warring(comment_id, comment_image, post_id);
                }
                return false;
            }
        });
        //show popup menu
        popupMenu.show();
    }

    private void warring(final String comment_id, final String comment_image, final String post_id) {
        //BUILD ALERT DIALOG TO CONFIRM THE SUPPRESSION
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Etes-vous sûr de vouloir supprimer cette publication ?");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "OUI",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        beginDelete(comment_id, comment_image, post_id);
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

    private void beginDelete(String comment_id, String comment_image, String post_id) {
        //decrease comment count
        final DocumentReference documentReference_post = FirebaseFirestore.getInstance().collection("Publications").document(post_id);
        final Map<String, Object> decreaseCommentCount = new HashMap<>();
        documentReference_post.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String comment_count = documentSnapshot.getString("comment_count");
                            int commentCount = Integer.parseInt(comment_count) - 1;
                            decreaseCommentCount.put("comment_count", String.valueOf(commentCount));
                            documentReference_post.set(decreaseCommentCount, SetOptions.merge());
                        }
                    }
                });
        if (comment_image.equals("noImage")){
            //delete post without image
            deletWithoutImage(comment_id);
        }else {
            //delete with image
            deleteWithImage(comment_id, comment_image);
        }
    }

    private void deleteWithImage(final String comment_id, String comment_image) {
        final ProgressDialog progressDialog_delete = new ProgressDialog(context);
        progressDialog_delete.setMessage("Suppressoin du commentaire en cours...");
        progressDialog_delete.show();

        //we must delete image stored in Firebase storage
        //after that deleting post from Firestore
        StorageReference storagePickReference = FirebaseStorage.getInstance().getReferenceFromUrl(comment_image);
        storagePickReference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //image deleted, now delete data on database
                        DocumentReference documentReference = collectionReference_comment.document(comment_id);
                        documentReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(context, "Commentaire supprimer avec succès", Toast.LENGTH_SHORT).show();
                                progressDialog_delete.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, "Erreur !!! Impossible de supprimer le commentaire", Toast.LENGTH_SHORT).show();
                                progressDialog_delete.dismiss();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Impossible de supprimer le commentaire", Toast.LENGTH_SHORT).show();
                progressDialog_delete.dismiss();
            }
        });
    }

    private void deletWithoutImage(String comment_id) {
        final ProgressDialog progressDialog_delete = new ProgressDialog(context);
        progressDialog_delete.setMessage("Suppressoin de le publication en cours...");
        progressDialog_delete.show();

        //delete data from Firestore
        DocumentReference documentReference = collectionReference_comment.document(comment_id);
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

    private void editComment(final String comment_id, String post_comment) {
        final ProgressDialog progressDialog_editCommentContent = new ProgressDialog(context);
        progressDialog_editCommentContent.setMessage("Chargement...");
        //custom dialog
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.edit_post_description);
        //set the custom dialog components
        TextView textView = dialog.findViewById(R.id.editText_title);
        textView.setText("MODIFICATION DU COMMENTAIRE");
        final EditText editText_comment = dialog.findViewById(R.id.et_postDescription);
        editText_comment.setText(post_comment);
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
                progressDialog_editCommentContent.show();
                dialog.dismiss();
                //to hide soft keyboard when comment sended
                InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                //get input text
                final String value = editText_comment.getText().toString();
                Map<String, Object> result = new HashMap<>();
                result.put("post_comment", value);
                //updata the value in database
                DocumentReference documentReferencePost = FirebaseFirestore.getInstance().collection("Comments").document(comment_id);
                documentReferencePost.update(result)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(context, "Commentaire mis à jour", Toast.LENGTH_LONG).show();
                                progressDialog_editCommentContent.dismiss();
                            }
                        });
            }
        });
        dialog.show();
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{
        //declare views from row_comments.xml
        ImageView profileImage_comment, image_comment;
        ImageButton moreAction_comment;
        TextView userName_comment, userPseudo_comment, commentContent_comment, commentTimeStamp_comment;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            //init views
            profileImage_comment = itemView.findViewById(R.id.imageView_photoDeProfile_commentPost);
            image_comment = itemView.findViewById(R.id.imageView_commentImage_comment);
            moreAction_comment = itemView.findViewById(R.id.moreActions_comment);
            userName_comment = itemView.findViewById(R.id.textView_userName_comment);
            userPseudo_comment = itemView.findViewById(R.id.texteView_pseudo_commentContent);
            commentContent_comment = itemView.findViewById(R.id.textView_commentContent_comment);
            commentTimeStamp_comment = itemView.findViewById(R.id.textView_commentTime_comment);
        }
    }
}
