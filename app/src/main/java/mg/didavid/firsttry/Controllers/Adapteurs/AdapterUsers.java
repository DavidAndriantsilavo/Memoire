package mg.didavid.firsttry.Controllers.Adapteurs;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.List;

import mg.didavid.firsttry.Controllers.Activities.OtherUsersProfileActivity;
import mg.didavid.firsttry.Controllers.Activities.ProfileUserActivity;
import mg.didavid.firsttry.Models.User;
import mg.didavid.firsttry.R;

public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.Myholder> {

    private Context context;
    private List<User> modelUsesList;


    private String mCurrentUserId;

    public AdapterUsers(Context context, List<User> modelUsesList) {
        this.context = context;
        this.modelUsesList = modelUsesList;
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            mCurrentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
    }

    @NonNull
    @Override
    public Myholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //bind the row_users.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_users, parent, false);
        return new  Myholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Myholder holder, int position) {
        //we just need the user_id, name, pseudo and profile_image only
        final String user_id = modelUsesList.get(position).getUser_id();
        String name = modelUsesList.get(position).getName();
        String pseudo = modelUsesList.get(position).getPseudo();
        String profile_image = modelUsesList.get(position).getProfile_image();

        //set data to views
        holder.textView_name.setText(name);
        holder.textView_pseudo.setText(pseudo);
        if (user_id.equals(mCurrentUserId)) {
            holder.textView_whoKiff.setVisibility(View.VISIBLE);
        } else {
            holder.textView_whoKiff.setVisibility(View.GONE);
        }
        //set user image profile
        try {
            Picasso.get().load(profile_image).placeholder(R.drawable.ic_image_profile_icon_dark).into(holder.imageView_profileImage);
        }catch (Exception e){
            Picasso.get().load(R.drawable.ic_image_profile_icon_dark).into(holder.imageView_profileImage);
        }

        //layout clicked, go to user profile
        holder.relativeLayout_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user_id.equals(mCurrentUserId)) {
                        Intent intent = new Intent(context, ProfileUserActivity.class);
                        context.startActivity(intent);
                }else {
                        Intent intent = new Intent(context, OtherUsersProfileActivity.class);
                        intent.putExtra("user_id", user_id);
                        context.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return modelUsesList.size();
    }

    class Myholder extends RecyclerView.ViewHolder {
        //declare views from row_users.xml
        TextView textView_name, textView_pseudo, textView_whoKiff;
        ImageView imageView_profileImage;
        RelativeLayout relativeLayout_user;

        public Myholder(@NonNull View itemView) {
            super(itemView);

            //init views
            textView_name = itemView.findViewById(R.id.textView_userName_whoKiff);
            textView_pseudo = itemView.findViewById(R.id.texteView_pseudo_whoKiff);
            textView_whoKiff = itemView.findViewById(R.id.textView_you_whoKiff);
            imageView_profileImage = itemView.findViewById(R.id.imageView_photoDeProfile_whoKiff);
            relativeLayout_user = itemView.findViewById(R.id.relativeLayout_whokiff);
        }
    }
}
