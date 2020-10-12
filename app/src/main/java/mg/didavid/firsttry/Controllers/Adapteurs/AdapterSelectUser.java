package mg.didavid.firsttry.Controllers.Adapteurs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.PaintDrawable;
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

public class AdapterSelectUser extends RecyclerView.Adapter<AdapterSelectUser.MyHolder>{

    final private String TAG = "AdapterSelectUser";

    Context context;
    List<User> userList;
    String mCurrentUserId;
    CollectionReference collectionReference_users;
    Long selectedPosition;

    private OnSelectUserListner onSelectUserListner;

    public AdapterSelectUser(Context context, List<User> userList, OnSelectUserListner onSelectUserListner) {
            this.context = context;
            this.userList = userList;
            this.onSelectUserListner = onSelectUserListner;

            mCurrentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            collectionReference_users = FirebaseFirestore.getInstance().collection("Users");
            setHasStableIds(true);
            }

    public AdapterSelectUser() {
            }

    @NonNull
    @Override
    public AdapterSelectUser.MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            //inflate layout row_post.xml
            View view = LayoutInflater.from(context).inflate(R.layout.row_select_user, parent, false);
            //return new AdapteurMessage.MyMessageHolder(view);
            return new AdapterSelectUser.MyHolder(view, onSelectUserListner);
            }


    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final AdapterSelectUser.MyHolder holder, final int position) {
            //get data
            String userName = userList.get(position).getName();
            String userPicture = userList.get(position).getProfile_image();

            try {
            //set data
            holder.textView_userName.setText(userName);
            Picasso.get().load(userPicture).resize(100, 100).transform(new CropCircleTransformation()).into(holder.imageView_userPicture);

//            holder.checkbox_selected.setChecked(!holder.checkbox_selected.isChecked());


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
        CheckBox checkbox_selected;
        AdapterSelectUser.OnSelectUserListner onSelectUserListner;

        public MyHolder(@NonNull View itemView, OnSelectUserListner onSelectUserListner) {
            super(itemView);

            this.onSelectUserListner = onSelectUserListner;

            //init views
            textView_userName = itemView.findViewById(R.id.textView_userName);
            imageView_userPicture = itemView.findViewById(R.id.imageView_userPicture);
            checkbox_selected = itemView.findViewById(R.id.checkbox_selected);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onSelectUserListner.onSelectUserClick(getAdapterPosition());
//            selectedPosition = getAdapterPosition();
//            notifyItemChanged(selectedPosition);

//            Paint viewPaint = ((PaintDrawable) v.getBackground()).getPaint();
//            int colorARGB = viewPaint.getColor();
//            if(colorARGB == Color.parseColor("0xFF00FF00")){
//                v.setBackgroundColor(0xFFFFFFFF);
//            }else{
//                v.setBackgroundColor(0xFF00FF00);
//            }

            checkbox_selected.setChecked(!checkbox_selected.isChecked());
        }
    }

    public interface OnSelectUserListner{
        void onSelectUserClick(int position);
    }
}
