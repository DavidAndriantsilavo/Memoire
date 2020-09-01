package mg.didavid.firsttry.Controllers.Adapteurs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;

import java.util.List;

import mg.didavid.firsttry.Models.Message;
import mg.didavid.firsttry.Models.User;
import mg.didavid.firsttry.Models.UserSingleton;
import mg.didavid.firsttry.R;

public class AdapteurChat extends RecyclerView.Adapter<AdapteurChat.MyHolder>{

            Context context;
            List<Message> messageList;
            User currentUser;
            CollectionReference collectionReference_chatroom;

    public AdapteurChat(Context context, List<Message> messageList) {
            this.context = context;
            this.messageList = messageList;

            currentUser = ((UserSingleton) context.getApplicationContext()).getUser();
            }

    public AdapteurChat() {
    }

    @NonNull
    @Override
    public AdapteurChat.MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            //inflate layout row_post.xml
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat, parent, false);
            //return new AdapteurMessage.MyMessageHolder(view);
            return new MyHolder(view);
            }


    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, final int position) {
            //get data
            String message = messageList.get(position).getContent();
            String sender_id = messageList.get(position).getSender_id();

            try {
                //set data
                holder.textView_message.setText(message);

                if(sender_id.equals(currentUser.getUser_id())){
                    holder.textView_message.setBackgroundResource(R.drawable.chat_sent);

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.gravity = Gravity.RIGHT;
                    params.topMargin = 10;
                    params.bottomMargin = 10;

                    holder.textView_message.setLayoutParams(params);
                }else{
                    holder.textView_message.setBackgroundResource(R.drawable.chat_receive);

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.gravity = Gravity.LEFT;
                    params.topMargin = 10;
                    params.bottomMargin = 10;

                    holder.textView_message.setLayoutParams(params);
                }

            }catch (Exception e){

            }
    }

    @Override
    public int getItemCount() {
            return messageList.size();
            }

    //view holder class
    class MyHolder extends RecyclerView.ViewHolder{

        //views from message_post.xml
        TextView textView_message;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init views
            textView_message= itemView.findViewById(R.id.textView_message);

        }
    }
}
