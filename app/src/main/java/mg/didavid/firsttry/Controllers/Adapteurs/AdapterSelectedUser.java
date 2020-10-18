package mg.didavid.firsttry.Controllers.Adapteurs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.List;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import mg.didavid.firsttry.Models.User;
import mg.didavid.firsttry.R;

public class AdapterSelectedUser extends  RecyclerView.Adapter<AdapterSelectedUser.MyHolder>{
    final private String TAG = "AdapterSelectUser";

    Context context;
    List<User> userList;
    String mCurrentUserId;
    CollectionReference collectionReference_users;

    private AdapterSelectedUser.OnSelectedUserListner onSelectedUserListner;

    public AdapterSelectedUser(Context context, List<User> userList, AdapterSelectedUser.OnSelectedUserListner onSelectedUserListner) {
        this.context = context;
        this.userList = userList;
        this.onSelectedUserListner = onSelectedUserListner;

        mCurrentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        collectionReference_users = FirebaseFirestore.getInstance().collection("Users");
        setHasStableIds(true);
    }

    public AdapterSelectedUser() {
    }

    @NonNull
    @Override
    public AdapterSelectedUser.MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout row_post.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_select_user, parent, false);
        //return new AdapteurMessage.MyMessageHolder(view);
        return new AdapterSelectedUser.MyHolder(view, onSelectedUserListner);
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final AdapterSelectedUser.MyHolder holder, final int position) {
        //get data
        String userName = userList.get(position).getName();
        String userPicture = userList.get(position).getProfile_image();

        try {
            //set data
            holder.textView_userName.setText(userName);
            Picasso.get().load(userPicture).resize(100, 100).transform(new CropCircleTransformation()).into(holder.imageView_userPicture);

        }catch (Exception e){

        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    //view holder class
    public class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        //views from row_userlist.xml
        TextView textView_userName;
        ImageView imageView_userPicture;
        AdapterSelectedUser.OnSelectedUserListner onSelectedUserListner;

        public MyHolder(@NonNull View itemView, AdapterSelectedUser.OnSelectedUserListner onSelectedUserListner) {
            super(itemView);

            this.onSelectedUserListner = onSelectedUserListner;

            //init views
            textView_userName = itemView.findViewById(R.id.textView_userName);
            imageView_userPicture = itemView.findViewById(R.id.imageView_userPicture);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onSelectedUserListner.onSelectedUserClick(getAdapterPosition());
        }
    }

    public interface OnSelectedUserListner{
        void onSelectedUserClick(int position);
    }

}
