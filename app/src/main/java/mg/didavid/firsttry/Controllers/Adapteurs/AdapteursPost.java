package mg.didavid.firsttry.Controllers.Adapteurs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import mg.didavid.firsttry.Controllers.Activities.NewPostActivity;
import mg.didavid.firsttry.Models.ModelePost;
import mg.didavid.firsttry.R;

public class AdapteursPost extends RecyclerView.Adapter<AdapteursPost.MyHolder>{

    Context context;
    List<ModelePost> postList;

    String mCurrentUserId;
    CollectionReference collectionReference_post;
    CollectionReference collectionReference_kiffs;
    Boolean mPressKiff = false;

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
        String name = postList.get(position).getName();
        String pseudo = postList.get(position).getPseudo();
        String profile_image = postList.get(position).getProfile_image();
        final String post_id = postList.get(position).getPost_id();
        String post_title = postList.get(position).getPost_title();
        String post_description = postList.get(position).getPost_description();
        final String post_image = postList.get(position).getPost_image();
        String post_timeStamp = postList.get(position).getPost_time();
        final String post_kiffs = postList.get(position).getPost_kiff(); // contains total number of kiffs for a post

        //convert timeStamp to dd/mm/yyyy hh:mm am/pm
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        try {
            calendar.setTimeInMillis(Long.parseLong(post_timeStamp));
        }catch (Exception e){
        }
        String pTemps = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        try {
            //set data
            holder.uNameTv.setText(name);
            holder.pseudo.setText(pseudo);
            holder.pTimeTv.setText(pTemps);
            holder.pTitleTv.setText(post_title);
            holder.pDescriptionTv.setText(post_description);
            holder.pKiffTv.setText(post_kiffs + " Kiffs");
            //set kiff for its post
            setKiffs(holder, post_id);
        }catch (Exception e){
        }
        //set user profile image
        try{
            Picasso.get().load(profile_image).placeholder(R.drawable.ic_image_profile_icon_dark).into(holder.uPictureIv);
        }catch (Exception e){

        }
        //set post image
        if (!post_image.equals("noImage")) {
            try {
                Picasso.get().load(post_image).into(holder.pImageIv);
            } catch (Exception e) {

            }
        }

        //handle click
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions(holder.moreBtn, user_id, mCurrentUserId, post_id, post_image);
            }
        });
        holder.kiffBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get total number of kiffs for the post
                final int postKiff = Integer.parseInt(postList.get(position).getPost_kiff());
                mPressKiff = true;
                //get id of the post clicked
                final String postId = postList.get(position).getPost_id();
                collectionReference_kiffs.addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (mPressKiff){
                            assert value != null;
                            if (value.getDocuments().get(position).get(mCurrentUserId) != null){
                                //already kiffed, so remove kiff
                                Map<String, Integer> kiffNumber = new HashMap<>();
                                kiffNumber.put("post_kiffs", postKiff - 1);
                                collectionReference_post.document(postId).set(kiffNumber);
                                collectionReference_kiffs.document(postId).delete();
                                mPressKiff = false;
                            }else {
                                //not kiff, kiff it
                                Map<String, Integer> kiffNumber = new HashMap<>();
                                kiffNumber.put("post_kiffs", postKiff + 1);
                                collectionReference_post.document(postId).set(kiffNumber);
                                collectionReference_kiffs.document(postId).set("kiff");
                                mPressKiff = false;
                            }
                        }
                    }
                });
            }
        });
        holder.commenterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Commenter...\nwill implement later", Toast.LENGTH_SHORT).show();
            }
        });
        holder.partagerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Partager...\nwill implement later", Toast.LENGTH_SHORT).show();
            }
        });
        holder.profile_linearlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void setKiffs(final MyHolder holder, String post_id) {
        collectionReference_kiffs.document(post_id).addSnapshotListener(new EventListener<DocumentSnapshot>() {
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

    private void showMoreOptions(ImageButton moreBtn, String user_id, String mCurrentUserId, final String post_id, final String post_image) {
        //create popup menu
        PopupMenu popupMenu = new PopupMenu(context, moreBtn, Gravity.END);
        //show popup menu in only posts of currently singed-in user
        if (user_id.equals(mCurrentUserId)){
            //add item in menu
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Supprimer la publication");
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Modifier la publication");
        }
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
                    //start NewPostActivity with key "edit post" and the id of the post clicked
                    Intent intent = new Intent(context, NewPostActivity.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("post_id", post_id);
                    context.startActivity(intent);
                }
                return false;
            }
        });
        //show menu
        popupMenu.show();
    }

    private void avertissement(final String post_id, final String post_image) {
        //BUILD ALERT DIALOG TO CONFIRM THE SUPPRESSION
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Etes-vous sûr de vouloir supprimer cette publication ?");
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
        progressDialog_delete.setMessage("Suppressoin de le publication en cours...");
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
        TextView uNameTv, pTimeTv, pTitleTv, pDescriptionTv, pKiffTv, pseudo;
        ImageButton moreBtn;
        Button kiffBtn, commenterBtn, partagerBtn;
        LinearLayout profile_linearlayout;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init views
            uPictureIv = itemView.findViewById(R.id.imageView_photoDeProfile_actu);
            pImageIv = itemView.findViewById(R.id.imageView_imagePost_actu);
            uNameTv = itemView.findViewById(R.id.textView_nomUser_actu);
            pTimeTv = itemView.findViewById(R.id.textView_temps_actu);
            pseudo = itemView.findViewById(R.id.texteView_pseudo);
            pTitleTv = itemView.findViewById(R.id.textView_titrePost_actu);
            pDescriptionTv = itemView.findViewById(R.id.textView_descriptionPost_actu);
            pKiffTv = itemView.findViewById(R.id.texteView_kiffs_actu);
            moreBtn = itemView.findViewById(R.id.button_moreAction_actu);
            kiffBtn = itemView.findViewById(R.id.button_kiff_actu);
            commenterBtn = itemView.findViewById(R.id.button_commenter_actu);
            partagerBtn = itemView.findViewById(R.id.button_partager_actu);
            profile_linearlayout = itemView.findViewById(R.id.linearLayout_profile_post);
        }
    }
}
