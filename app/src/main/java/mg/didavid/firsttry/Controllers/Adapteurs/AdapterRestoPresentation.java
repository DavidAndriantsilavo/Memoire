package mg.didavid.firsttry.Controllers.Adapteurs;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
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
import android.widget.PopupMenu;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

import mg.didavid.firsttry.Controllers.Activities.ListMenuRestoActivity;
import mg.didavid.firsttry.Controllers.Activities.OtherRestoProfileActivity;
import mg.didavid.firsttry.Controllers.Activities.ProfileRestoActivity;
import mg.didavid.firsttry.Models.ModelResto;
import mg.didavid.firsttry.Models.ModelRestoSampleMenu;
import mg.didavid.firsttry.Models.ModelePost;
import mg.didavid.firsttry.R;

public class AdapterRestoPresentation extends RecyclerView.Adapter<AdapterRestoPresentation.MyHolder> {

    private Context context;
    private List<ModelResto> modelRestoList;

    private AdapterSampleMenu adapterSampleMenu;
    private List<ModelRestoSampleMenu> modelRestoSampleMenus;

    private String mCurrentUser_id;
    private String mCurrentResto_id;

    private CollectionReference collectionReference_hasRatingResto = FirebaseFirestore.getInstance().collection("HasRatingResto");

    public AdapterRestoPresentation(Context context, List<ModelResto> modelRestoList) {
        this.context = context;
        this.modelRestoList = modelRestoList;
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            mCurrentUser_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            mCurrentResto_id = "resto_" + mCurrentUser_id;
        }
    }


    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //bind the row_presentation_resto.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_presentation_resto, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, int position) {
        //we just take resto id, resto name, resto culinary speciality, the rating of the resto and its logo
        ModelResto modelResto = modelRestoList.get(position);
        String restoName = modelResto.getName_resto();
        final String restoId = modelResto.getId_resto();
        String restoSpeciality = modelResto.getSpeciality_resto();
        float restoRating = Float.parseFloat(modelResto.getRating_resto());
        String restoNbrRatign = modelResto.getNbrRating_resto();
        String restoLogo = modelResto.getLogo_resto();

        //sample menu definition
        modelRestoSampleMenus = modelResto.getSampleMenuList();
        adapterSampleMenu = new AdapterSampleMenu(context, modelRestoSampleMenus);
        holder.recyclerView_sampleMenu.setAdapter(adapterSampleMenu);

        //set data to views
        holder.textView_restoName.setText(restoName);
        holder.textView_restSpeciality.setText(restoSpeciality);
        holder.ratingBar_restoRating.setRating(restoRating);
        holder.textView_ratingResto.setText(String.valueOf((float) Math.round(restoRating * 10) / 10));
        if (restoNbrRatign.equals("0")) {
            holder.textView_restoRatingNumber.setVisibility(View.GONE);
        }else if (restoNbrRatign.equals("1")){
            holder.textView_restoRatingNumber.setVisibility(View.VISIBLE);
            holder.textView_restoRatingNumber.setText(restoNbrRatign + " vote");
        } else {
            holder.textView_restoRatingNumber.setVisibility(View.VISIBLE);
            holder.textView_restoRatingNumber.setText(restoNbrRatign + " votes");
        }
        try {
            Picasso.get().load(restoLogo).placeholder(R.drawable.ic_image_profile_icon_dark).into(holder.imageView_restoLogo);
        }catch (Exception e) {
            Picasso.get().load(R.drawable.ic_image_profile_icon_dark).into(holder.imageView_restoLogo);
        }

        holder.imageButton_moreAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreActions(holder.imageButton_moreAction, restoId);
            }
        });

        holder.imageView_restoLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToRestoProfile(restoId);
            }
        });
        holder.textView_restoName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToRestoProfile(restoId);
            }
        });

    }

    private void showMoreActions(ImageButton imageButton_moreAction, final String restoId) {
        //create popup menu
        PopupMenu popupMenu = new PopupMenu(context,imageButton_moreAction, Gravity.END);
        //add items im menu
        popupMenu.getMenu().add(Menu.NONE, 0, 0, "Voir tous les menus");
        popupMenu.getMenu().add(Menu.NONE, 1, 1, "Voir profile");
        popupMenu.getMenu().add(Menu.NONE, 3, 3, "Voir lieu");
        if (!restoId.contains(mCurrentResto_id)) {
            popupMenu.getMenu().add(Menu.NONE, 4, 4, "Noter ce restaurant");
            popupMenu.getMenu().add(Menu.NONE, 2, 2, "Passer une commande");
        }

        //button clicked
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int item_id = item.getItemId();
                if (item_id == 0) {
                    //voir tous les menus
                    Intent intent = new Intent(context, ListMenuRestoActivity.class);
                    intent.putExtra("key", restoId);
                    context.startActivity(intent);
                }else if (item_id == 1) {
                    //voir profil
                    sendUserToRestoProfile(restoId);
                }else if (item_id == 2) {
                    //passer une commande
                    showChoiceDialog(restoId);
                }else if (item_id == 3) {
                    //voir lieu
                    Toast.makeText(context, "voir lieu", Toast.LENGTH_SHORT).show();
                }else if (item_id == 4) {
                    //raitng selected
                    //check if user has rating resto yet, if not show rating dialog
                    collectionReference_hasRatingResto.document(restoId).get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if (documentSnapshot.exists()) {
                                        if (documentSnapshot.get(mCurrentUser_id) != null) {
                                            Toast.makeText(context, "Vous avez déjà noté ce restaurant", Toast.LENGTH_LONG).show();
                                        }else {
                                            showRatingDialog(restoId);
                                        }
                                    }else {
                                        showRatingDialog(restoId);
                                    }
                                }
                            });
                }
                return false;
            }
        });
        //show popup menu
        popupMenu.show();
    }

    private void showChoiceDialog(final String id_resto) {
        String[] options = {"Appeler", "Envoyer un email"};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        callRestaurant(id_resto);
                        break;
                    case 1:
                        sendEmailToRestaurant(id_resto);
                        break;
                }
            }
        });
        builder.create().show();
    }

    private void callRestaurant(String id_resto) {
        FirebaseFirestore.getInstance().collection("Resto").document(id_resto).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Intent callResto = new Intent(Intent.ACTION_CALL);
                        callResto.setData(Uri.parse("tel:" + documentSnapshot.getString("phone_resto")));
                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                            ActivityCompat.requestPermissions((Activity) context, new String[] {Manifest.permission.CALL_PHONE}, 1996);
                            return;
                        }
                        context.startActivity(callResto);
                    }
                });
    }

    private void sendEmailToRestaurant(String id_resto) {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_sending_email);


        //init views
        Button button_annler = dialog.findViewById(R.id.tv_annuler_emailDialog);
        final Button button_creer = dialog.findViewById(R.id.tv_valider_emailDialog);
        button_annler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        final EditText editTextSubject = dialog.findViewById(R.id.editText_subject_emailDialog);
        final EditText editTextMessage = dialog.findViewById(R.id.editText_messageContent_emailDialog);
        if (editTextSubject.getText().toString().isEmpty()) {
            editTextSubject.setError("Veillez entrer l'objet du mail !");
        }else if (editTextMessage.getText().toString().isEmpty()){
            editTextMessage.setError("Veillez entrée votre message !");
        }else {
            FirebaseFirestore.getInstance().collection("Resto").document(id_resto).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                final Intent sendEmail = new Intent(Intent.ACTION_SEND);
                                sendEmail.setType("message/rfc822");
                                sendEmail.putExtra(Intent.EXTRA_EMAIL, new String[]{documentSnapshot.getString("email_resto")});
                                sendEmail.putExtra(Intent.EXTRA_SUBJECT, editTextSubject.getText().toString());
                                sendEmail.putExtra(Intent.EXTRA_TEXT, editTextMessage.getText().toString());

                                button_creer.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        context.startActivity(Intent.createChooser(sendEmail, "Envoie de l'email..."));
                                    }
                                });
                            }
                        }
                    });
        }

        dialog.show();
    }

    private void showRatingDialog(final String id_resto) {
        //create dialog
        final Dialog ratingDialog = new Dialog(context);
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

                                //set user as having rate this restaurant
                                HashMap<String, Object> userRating = new HashMap<>();
                                userRating.put(mCurrentUser_id, "rating");
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

    private void sendUserToRestoProfile(String restoId) {
        if (restoId.equals(mCurrentResto_id)) {
            //send user to his resto profile
            context.startActivity(new Intent(context, ProfileRestoActivity.class));
        }else {
            //send user to other resto profile
            Intent intent = new Intent(context, OtherRestoProfileActivity.class);
            intent.putExtra("id_resto", restoId);
            context.startActivity(intent);
        }
    }


    @Override
    public int getItemCount() {
        return modelRestoList.size();
    }


    class MyHolder extends RecyclerView.ViewHolder {
        //declare views form row_resentation_resto.xml
        TextView textView_restoName, textView_restSpeciality, textView_restoRatingNumber, textView_ratingResto;
        ImageView imageView_restoLogo;
        RatingBar ratingBar_restoRating;
        ImageButton imageButton_moreAction;
        RecyclerView recyclerView_sampleMenu;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            //inti views
            textView_restoName = itemView.findViewById(R.id.textView_nameResto_restoFragment);
            textView_restSpeciality = itemView.findViewById(R.id.textView_culinarySpeciality_restoFragment);
            textView_restoRatingNumber = itemView.findViewById(R.id.textView_ratingNumber_restoFragment);
            textView_ratingResto = itemView.findViewById(R.id.textView_noteResto_restoFragment);
            imageView_restoLogo = itemView.findViewById(R.id.imageView_logoResto_restoFragment);
            ratingBar_restoRating = itemView.findViewById(R.id.ratingBar_restoFragment);
            imageButton_moreAction = itemView.findViewById(R.id.button_moreAction_restoFragment);
            recyclerView_sampleMenu = itemView.findViewById(R.id.recyclerView_restoPresentation_restoFragment);

            recyclerView_sampleMenu.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        }
    }
}
