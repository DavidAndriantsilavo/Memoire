package mg.didavid.firsttry.Controllers.Adapteurs;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.List;

import mg.didavid.firsttry.Controllers.Activities.ShowImageActivity;
import mg.didavid.firsttry.Models.ModelRestoSampleMenu;
import mg.didavid.firsttry.R;

public class AdapterListMenu extends RecyclerView.Adapter<AdapterListMenu.MyHolder> {

    private Context context;
    private List<ModelRestoSampleMenu> menuList;
    private CollectionReference collectionReference_sampleMenu = FirebaseFirestore.getInstance().collection("Sample_menu");

    public AdapterListMenu(Context context, List<ModelRestoSampleMenu> menuList) {
        this.context = context;
        this.menuList = menuList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //bind the row_sample_menu_resto.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_menu_list, parent, false);
        return new MyHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, int position) {
        final String menuName, menuPrice, menuIngredient, id_resto, id_menu, menuPhoto;
        //get data
        menuPhoto = menuList.get(position).getMenuPhoto();
        menuName = menuList.get(position).getMenuName();
        menuPrice = menuList.get(position).getMenuPrice();
        menuIngredient = menuList.get(position).getMenuIngredient();
        id_resto = menuList.get(position).getId_resto();
        id_menu = menuList.get(position).getId_menu();

        Log.d("menuName ", menuName);

        //set data
        holder.textView_menuName.setText(menuName);
        holder.textView_menuIngredient.setText(menuIngredient);
        holder.textView_menuPrice.setText(menuPrice + "\tAr");
        try {
            Picasso.get().load(menuPhoto).into(holder.imageView_menuImage);
        } catch (Exception e) {
        }

        //button more action clicked
        holder.imageButton_moreAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMoreActionMenu(holder.imageButton_moreAction, id_resto, id_menu, menuName, menuPrice, menuIngredient, menuPhoto);
            }
        });

        //menu image clicked
        holder.imageView_menuImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToShowImage(menuPhoto);
            }
        });
    }


    private void goToShowImage(String imageUri) {
        Intent intent = new Intent(context, ShowImageActivity.class);
        intent.putExtra("showImage", imageUri);
        Log.d("valeur de image uri", " :" + imageUri);
        context.startActivity(intent);
    }

    private void setMoreActionMenu(ImageButton imageButton_moreAction, final String id_resto, final String id_menu, final String menuName, final String menuPrice, final String menuIngredient, final String menuPhoto) {
        //create popup menu
        PopupMenu popupMenu = new PopupMenu(context, imageButton_moreAction, Gravity.END);
        if (!id_resto.equals("resto_" + FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Commander ce menu");
        } else {
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "Ajouter à l'échantillion");
            popupMenu.getMenu().add(Menu.NONE, 2, 0, "Modifier le menu");
            popupMenu.getMenu().add(Menu.NONE, 3, 0, "Supprimer le menu");
        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id_item = item.getItemId();
                if (id_item == 0) {
                    //"commander ce menu" cicked
                    showChoiceDialog(id_resto);
                }
                if (id_item == 1) {
                    //"Ajouter à l'échantillion" clicked
                    addMenuToSample(menuName, menuPrice, menuPhoto, id_menu, id_resto);
                }
                if (id_item == 2) {
                    //"modiier le menu" clicked
                    //Allow user to edit menu name, menu price and menu ingrendients
                    showEditMenuDialog(menuName, menuIngredient, menuPrice, menuPhoto, id_menu, id_resto);
                }
                if (id_item == 3) {
                    //"supprimer menu" clicked
                    warring(menuPhoto, id_menu);
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

    private void addMenuToSample(final String menuName, final String menuPrice, final String menuPhoto, final String id_menu, final String id_resto) {
        collectionReference_sampleMenu.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            int countSampleMenu = 0;
                            boolean isMenuExist = false;
                            for (DocumentSnapshot ds : queryDocumentSnapshots.getDocuments()){
                                String id_resto2 = ds.getString("id_resto");
                                String id_menu2 = ds.getString("id_menu");
                                if (id_resto2.equals(id_resto)) {
                                    countSampleMenu ++;
                                }
                                if (id_menu2.equals(id_menu)) {
                                    isMenuExist = true;
                                    Toast.makeText(context, "Ce menu est déjà dans l'échantillon.", Toast.LENGTH_LONG).show();
                                }
                            }
                            if (countSampleMenu < 6 && !isMenuExist) {
                                ModelRestoSampleMenu modelRestoSampleMenu = new ModelRestoSampleMenu(id_resto, id_menu, "", menuPhoto, menuName, menuPrice);
                                final String timestamp = String.valueOf(System.currentTimeMillis());
                                //store data
                                collectionReference_sampleMenu.document(id_menu).set(modelRestoSampleMenu)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(context, "Ecahntillon de menu ajouté avec succès", Toast.LENGTH_LONG).show();
                                            }
                                        });
                            }else {
                                //user has yet 5 sample menu set
                                showMessageDialog();
                            }
                        }else {
                            ModelRestoSampleMenu modelRestoSampleMenu = new ModelRestoSampleMenu(id_resto, id_menu, "", menuPhoto, menuName, menuPrice);
                            //store data
                            collectionReference_sampleMenu.document(id_menu).set(modelRestoSampleMenu)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(context, "Ecahntillon de menu ajouté avec succès", Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    }
                });
    }

    private void showMessageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Vous aver déjà 5 échantillons. Veillez vous rendre dans le profile de votre restaurant pour en supprimer au moins 1 pour pouvoir en ajouter");
        builder.setCancelable(false);

        builder.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        //create and show dialog
        builder.create().show();
    }

    private void warring(final String menuPhoto, final String id_menu) {
        //BUILD ALERT DIALOG TO CONFIRM THE SUPPRESSION
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Etes-vous sûr de vouloir supprimer ce menu ?");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "OUI",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                       deleteMenuImage(menuPhoto, id_menu);
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

    private void deletMenu(String id_menu) {
        FirebaseFirestore.getInstance().collection("Menu_list").document(id_menu).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "Menu supprimer avec succès", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Impossible de supprimer le menu. Veillez réessayer !", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void deleteMenuImage(String menuPhoto, final String id_menu) {
        FirebaseStorage.getInstance().getReferenceFromUrl(menuPhoto).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        deletMenu(id_menu);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Une erreur s'est survenue. Veuillez réessayer!", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showEditMenuDialog(String menuName, String menuIngredient, String menuPrice, final String menuPhoto, final String id_menu, final String id_resto) {
        //create dialog
        final Dialog dialog = new Dialog(context);
        //set dialog content
        dialog.setContentView(R.layout.dialog_edit_menu_resto);
        dialog.setCanceledOnTouchOutside(false);
        //init content views
        final EditText editText_menuName = dialog.findViewById(R.id.et_menuName_editDialog);
        final EditText editText_menuIngredient = dialog.findViewById(R.id.et_menuIngredient_editDialog);
        final EditText editText_menuPrice = dialog.findViewById(R.id.et_menuPrice_editDialog);
        TextView textView_annuler = dialog.findViewById(R.id.tv_annuler_editDialog);
        TextView textView_valider = dialog.findViewById(R.id.tv_valider_editDialog);

        //set current value
        editText_menuName.setText(menuName);
        editText_menuPrice.setText(menuPrice);
        editText_menuIngredient.setText(menuIngredient);

        //"annuler" cicked
        textView_annuler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        //"valider" clicked
        textView_valider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String menuName1 = editText_menuName.getText().toString();
                String menuIngredient1 = editText_menuIngredient.getText().toString();
                String menuPrice1 = editText_menuPrice.getText().toString();

                ModelRestoSampleMenu modelRestoSampleMenu = new ModelRestoSampleMenu(id_resto, id_menu, menuIngredient1, menuPhoto, menuName1, menuPrice1);
                //update data
                FirebaseFirestore.getInstance().collection("Menu_list").document(id_menu).set(modelRestoSampleMenu, SetOptions.merge())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(context, "Menu modifié avec succès", Toast.LENGTH_SHORT).show();
                            }
                        });
                collectionReference_sampleMenu.document(id_menu).set(modelRestoSampleMenu, SetOptions.merge());

                dialog.dismiss();
            }
        });

        //show dialog
        dialog.show();
    }

    @Override
    public int getItemCount() {
        return menuList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {
        //declare views from row_sample_menu_resto
        TextView textView_menuName, textView_menuPrice, textView_menuIngredient;
        ImageView imageView_menuImage;
        ImageButton imageButton_moreAction;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            textView_menuName = itemView.findViewById(R.id.textView_menuName_listMenu);
            textView_menuPrice = itemView.findViewById(R.id.textView_price_listMenu);
            imageView_menuImage = itemView.findViewById(R.id.imageView_menuImage_listMenu);
            textView_menuIngredient = itemView.findViewById(R.id.textView_ingredient_lestMenu);
            imageButton_moreAction = itemView.findViewById(R.id.button_moreAction_actu);
        }
    }
}
