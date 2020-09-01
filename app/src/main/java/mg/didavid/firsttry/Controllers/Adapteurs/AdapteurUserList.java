package mg.didavid.firsttry.Controllers.Adapteurs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class AdapteurUserList extends RecyclerView.Adapter<AdapteurUserList.MyHolder>{

    final private String TAG = "adapteurUSerList";

    Context context;
    List<User> userList;

    String mCurrentUserId;
    CollectionReference collectionReference_users;

    private OnUserListner mOnUserListner;

    public AdapteurUserList(Context context, List<User> userList, OnUserListner onUserListner) {
        this.context = context;
        this.userList = userList;
        this.mOnUserListner = onUserListner;

        mCurrentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        collectionReference_users = FirebaseFirestore.getInstance().collection("Users");
    }

    public AdapteurUserList() {
    }

    @NonNull
    @Override
    public AdapteurUserList.MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout row_post.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_userlist, parent, false);
        //return new AdapteurMessage.MyMessageHolder(view);
        return new AdapteurUserList.MyHolder(view, mOnUserListner);
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final AdapteurUserList.MyHolder holder, final int position) {
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
        OnUserListner onUserListner;

        public MyHolder(@NonNull View itemView, OnUserListner onUserListner) {
            super(itemView);

            this.onUserListner = onUserListner;

            //init views
            textView_userName = itemView.findViewById(R.id.textView_userName);
            imageView_userPicture = itemView.findViewById(R.id.imageView_userPicture);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onUserListner.onUserClick(getAdapterPosition());
        }
    }

    public interface OnUserListner{
        void onUserClick(int position);
    }
}
