package mg.didavid.firsttry.Controllers.Adapteurs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
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

public class AdapterAppointmentNotification extends RecyclerView.Adapter<AdapterAppointmentNotification.MyHolder>{

    final private String TAG = "AdapterSelectUser";

    Context context;
    List<String> notificationList;

    private AdapterAppointmentNotification.OnSelectNotificationListner onSelectNotificationListner;

    public AdapterAppointmentNotification(Context context, List<String> notificationList, AdapterAppointmentNotification.OnSelectNotificationListner onSelectNotificationListner) {
        this.context = context;
        this.notificationList = notificationList;
        this.onSelectNotificationListner = onSelectNotificationListner;
    }

    public AdapterAppointmentNotification(Context context, List<String> notificationList) {
        this.context = context;
        this.notificationList = notificationList;

        setHasStableIds(true);
    }

    public AdapterAppointmentNotification() {
    }

    @NonNull
    @Override
    public AdapterAppointmentNotification.MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout row_post.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_appointment_notification, parent, false);
        //return new AdapteurMessage.MyMessageHolder(view);
        return new AdapterAppointmentNotification.MyHolder(view, onSelectNotificationListner);
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final AdapterAppointmentNotification.MyHolder holder, final int position) {
        //get data
        String notification = notificationList.get(position);

        try {
            //set data
            holder.textView_notification.setText(notification);

        }catch (Exception e){

        }
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    //view holder class
    public class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        //views from row_userlist.xml
        TextView textView_notification;
        AdapterAppointmentNotification.OnSelectNotificationListner mOnSelectNotificationListner;

        public MyHolder(@NonNull View itemView, AdapterAppointmentNotification.OnSelectNotificationListner mOnSelectNotificationListner) {
            super(itemView);

            this.mOnSelectNotificationListner = mOnSelectNotificationListner;

            //init views
            textView_notification = itemView.findViewById(R.id.textView_notification);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mOnSelectNotificationListner.onSelectNotificationClick(getAdapterPosition());
        }
    }

    public interface OnSelectNotificationListner {
        void onSelectNotificationClick(int position);
    }
}