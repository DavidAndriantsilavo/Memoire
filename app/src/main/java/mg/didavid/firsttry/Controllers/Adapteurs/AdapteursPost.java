package mg.didavid.firsttry.Controllers.Adapteurs;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import mg.didavid.firsttry.Controllers.Modeles.ModelePost;
import mg.didavid.firsttry.R;

public class AdapteursPost extends RecyclerView.Adapter<AdapteursPost.MyHolder>{

    Context context;
    List<ModelePost> postList;

    public AdapteursPost(Context context, List<ModelePost> postList) {
        this.context = context;
        this.postList = postList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout row_post.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_post, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        //get data
        String uid = postList.get(position).getUid();
        String nomEtPrenom = postList.get(position).getNomEtPrenom();
        String pseudo = postList.get(position).getPseudo();
        String photoDeProfile = postList.get(position).getPhotoDeProfile();
        String pId = postList.get(position).getpId();
        String pTitre = postList.get(position).getpTitre();
        String pDescription = postList.get(position).getpDescription();
        String pImage = postList.get(position).getpImage();
        String pTimeStamp = postList.get(position).getpTemps();

        //convert timeStamp to dd/mm/yyyy hh:mm am/pm
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
        String pTemps = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        //set data
        holder.uNameTv.setText(nomEtPrenom);
        holder.pseudo.setText(pseudo);
        holder.pTimeTv.setText(pTemps);
        holder.pTitleTv.setText(pTitre);
        holder.pDescriptionTv.setText(pDescription);
        //set user profile image
        try{
            Picasso.get().load(photoDeProfile).placeholder(R.drawable.ic_image_profile_icon_dark).into(holder.uPictureIv);
        }catch (Exception e){

        }
        //set post image
        if (!pImage.equals("sansImage")) {
            try {
                Picasso.get().load(pImage).into(holder.pImageIv);
            } catch (Exception e) {

            }
        }

        //handle click
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "More...\nwill implement later", Toast.LENGTH_SHORT).show();
            }
        });
        holder.kiffBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "J'kiff...\nwill implement later", Toast.LENGTH_SHORT).show();
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
        }
    }
}
