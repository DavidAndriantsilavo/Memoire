package mg.didavid.firsttry.Controllers.Adapteurs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import mg.didavid.firsttry.Models.ModelRestoSampleMenu;
import mg.didavid.firsttry.R;

public class AdapterSampleMenu extends RecyclerView.Adapter<AdapterSampleMenu.MyHolder> {

    private Context context;
    private List<ModelRestoSampleMenu> modelRestoSampleMenuList;

    public AdapterSampleMenu(Context context, List<ModelRestoSampleMenu> modelRestoSampleMenuList) {
        this.context = context;
        this.modelRestoSampleMenuList = modelRestoSampleMenuList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //bind the row_sample_menu_resto.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_sample_menu_resto, parent, false);
        return new MyHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        //get data
        final String menuPhoto = modelRestoSampleMenuList.get(position).getMenuPhoto();
        String menuName = modelRestoSampleMenuList.get(position).getMenuName();
        String menuPrice = modelRestoSampleMenuList.get(position).getMenuPrice();
        final String id_menu = modelRestoSampleMenuList.get(position).getId_menu();
        String id_resto = modelRestoSampleMenuList.get(position).getId_resto();

        String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Log.d("menuName ", menuName);

        //set data
        holder.textView_nameSampleMenu.setText(menuName);
        holder.textView_priceSampleMenu.setText(menuPrice + "\tAr");
        try {
            Picasso.get().load(menuPhoto).into(holder.imageView_sampleMenu);
        }catch (Exception e) { }

        //delete sample menu
        if (id_resto.equals("resto_" + user_id)) {
            holder.imageButton_deletSampleMenu.setVisibility(View.VISIBLE);
            holder.imageButton_deletSampleMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   deleteDocumentSampleMenu(id_menu);
                }
            });
        }
    }


    private void deleteDocumentSampleMenu(String id_menu) {
        //delete document that contain id_menu
        FirebaseFirestore.getInstance().collection("Sample_menu").document(id_menu).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "Suppression de l'échantillon avec succès", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @SuppressLint("LongLogTag")
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Il y a eu une erreur. Veillez réessayer", Toast.LENGTH_SHORT).show();
                        Log.d("erreur : delete sample menu data", "" + e.getMessage());
                    }
                });
    }

    @Override
    public int getItemCount() {
        int size = 0;
        if (modelRestoSampleMenuList != null && !modelRestoSampleMenuList.isEmpty()) {
            size = modelRestoSampleMenuList.size();
        }
        return size;
    }

    class MyHolder extends RecyclerView.ViewHolder {
        //declare views from row_sample_menu_resto
        TextView textView_nameSampleMenu, textView_priceSampleMenu;
        ImageView imageView_sampleMenu;
        ImageButton imageButton_deletSampleMenu;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            textView_nameSampleMenu = itemView.findViewById(R.id.textView_nameSampleMenu);
            textView_priceSampleMenu = itemView.findViewById(R.id.textView_priceSampeMenu);
            imageView_sampleMenu = itemView.findViewById(R.id.imageView_sampleMenu);
            imageButton_deletSampleMenu = itemView.findViewById(R.id.btn_delete_sampleMenu);
        }
    }
}
