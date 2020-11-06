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
import mg.didavid.firsttry.Models.ModelAppointment;
import mg.didavid.firsttry.Models.User;
import mg.didavid.firsttry.R;

public class AdapterAppointmentList extends RecyclerView.Adapter<AdapterAppointmentList.MyHolder>{

    final private String TAG = "adapteurAppointmentList";

    Context context;
    List<ModelAppointment> appointmentList;

    String mCurrentUserId;
    CollectionReference collectionReference_users;

    private OnAppointmentListner mOnAppointmentListner;

    public AdapterAppointmentList(Context context, List<ModelAppointment> appointmentList, AdapterAppointmentList.OnAppointmentListner onAppointmentListner) {
        this.context = context;
        this.appointmentList = appointmentList;
        this.mOnAppointmentListner = onAppointmentListner;

        mCurrentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        collectionReference_users = FirebaseFirestore.getInstance().collection("Users");
    }

    public AdapterAppointmentList() {
    }

    @NonNull
    @Override
    public AdapterAppointmentList.MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout row_post.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_appointment_list, parent, false);
        //return new AdapteurMessage.MyMessageHolder(view);
        return new AdapterAppointmentList.MyHolder(view, mOnAppointmentListner);
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final AdapterAppointmentList.MyHolder holder, final int position) {
        //get data
        String restoName = appointmentList.get(position).getResto_name();
        String date = appointmentList.get(position).getDate();
        int selectedCount = appointmentList.get(position).getSelectedUser().size()-1;
        String descritpion = appointmentList.get(position).getDescription();

        Log.d(TAG, "onBindViewHolder: ");
        try {
            //set data
//            Picasso.get().load(userPicture).resize(100, 100).transform(new CropCircleTransformation()).into(holder.imageView_userPicture);

            Log.d(TAG, "onBindViewHolder: try");
            holder.textView_restoName.setText(restoName);
            holder.textView_date.setText("Le " + date);
            holder.textView_selectedCount.setText("avec " + selectedCount + "personne(s)");
            holder.textView_description.setText(descritpion);

        }catch (Exception e){

        }
    }

    @Override
    public int getItemCount() {
        return appointmentList.size();
    }

    //view holder class
    public class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        //views from row_userlist.xml
        ImageView imageView_logoResto;
        TextView textView_restoName, textView_date, textView_selectedCount, textView_description;
        OnAppointmentListner mOnAppointmentListner;

        public MyHolder(@NonNull View itemView, OnAppointmentListner onAppointmentListner) {
            super(itemView);

            this.mOnAppointmentListner = onAppointmentListner;

            //init views
            imageView_logoResto = itemView.findViewById(R.id.imageView_logoResto);
            textView_restoName = itemView.findViewById(R.id.textView_restoName);
            textView_date = itemView.findViewById(R.id.textView_date);
            textView_selectedCount = itemView.findViewById(R.id.textView_selectedCount);
            textView_description = itemView.findViewById(R.id.textView_description);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mOnAppointmentListner.onUserClick(getAdapterPosition());
        }
    }

    public interface OnAppointmentListner {
        void onUserClick(int position);
    }
}
