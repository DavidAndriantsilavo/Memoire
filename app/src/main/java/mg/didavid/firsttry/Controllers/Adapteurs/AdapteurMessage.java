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

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.List;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import mg.didavid.firsttry.Models.ModeleChatroom;
import mg.didavid.firsttry.Models.User;
import mg.didavid.firsttry.Models.UserSingleton;
import mg.didavid.firsttry.R;

public class AdapteurMessage extends RecyclerView.Adapter<AdapteurMessage.MyMessageHolder>{

    Context context;
    List<ModeleChatroom> chatroomList;

    CollectionReference collectionReference_chatroom;

    User currentUser;

    private OnChatRoomListner mOnChatRoomListner;

    public AdapteurMessage(Context context, List<ModeleChatroom> chatroomList, OnChatRoomListner onChatRoomListner) {
        this.context = context;
        this.chatroomList = chatroomList;
        this.mOnChatRoomListner = onChatRoomListner;

        collectionReference_chatroom = FirebaseFirestore.getInstance().collection("Chatrooms");
        currentUser = ((UserSingleton) context.getApplicationContext()).getUser();
    }

    public AdapteurMessage() {
    }

    @NonNull
    @Override
    public AdapteurMessage.MyMessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout row_post.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_message, parent, false);
        //return new AdapteurMessage.MyMessageHolder(view);
        return new AdapteurMessage.MyMessageHolder(view, mOnChatRoomListner);
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final MyMessageHolder holder, final int position) {
        //get data
        String other_user_name = chatroomList.get(position).getOther_user_name();
        String last_message = chatroomList.get(position).getLast_message();
        String profilePicture = chatroomList.get(position).getOther_user_profile_picture();

        try {
            holder.textView_receiverName.setText(other_user_name);
            holder.textView_lastMessage.setText(last_message);

            Picasso.get().load(profilePicture).resize(100, 100).transform(new CropCircleTransformation()).into(holder.imageView_profilePicture);

        }catch (Exception e){

        }
    }

    @Override
    public int getItemCount() {
        return chatroomList.size();
    }

    //view holder class
    class MyMessageHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        //views from message_post.xml
        TextView textView_receiverName, textView_lastMessage;
        ImageView imageView_profilePicture;
        OnChatRoomListner onChatRoomListner;

        public MyMessageHolder(@NonNull View itemView, OnChatRoomListner onChatRoomListner) {
            super(itemView);

            this.onChatRoomListner = onChatRoomListner;

            //init views
            textView_lastMessage = itemView.findViewById(R.id.textView_lastMessage);
            textView_receiverName = itemView.findViewById(R.id.textView_receiverName);
            imageView_profilePicture = itemView.findViewById(R.id.imageView_profilePicture);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mOnChatRoomListner.onChatRoomClick((getAdapterPosition()));
        }
    }

    public interface OnChatRoomListner{
        void onChatRoomClick(int position);
    }
}
