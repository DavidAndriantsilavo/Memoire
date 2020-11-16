package mg.didavid.firsttry.Controllers.Adapteurs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import mg.didavid.firsttry.Models.ModelAppointment;
import mg.didavid.firsttry.Models.User;
import mg.didavid.firsttry.Models.UserSingleton;
import mg.didavid.firsttry.R;

public class AdapterAppointmentList extends RecyclerView.Adapter<AdapterAppointmentList.MyHolder>{

    final private String TAG = "adapteurAppointmentList";

    Context context;
    List<ModelAppointment> appointmentList;

    CollectionReference collectionReference_users = FirebaseFirestore.getInstance().collection("Users");
    private DatabaseReference mAppointmentNotificationReference = FirebaseDatabase.getInstance().getReference().child("notification").child("appointment");

    User currentUser;

    private OnAppointmentListner mOnAppointmentListner;

    public AdapterAppointmentList(Context context, List<ModelAppointment> appointmentList, AdapterAppointmentList.OnAppointmentListner onAppointmentListner) {
        this.context = context;
        this.appointmentList = appointmentList;
        this.mOnAppointmentListner = onAppointmentListner;

        currentUser = ((UserSingleton)context.getApplicationContext()).getUser();
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
        final ModelAppointment appointment = appointmentList.get(position);

        String restoName = appointment.getResto_name();
        String restoLogo = appointment.getResto_logo();
        final String date = appointment.getDate();
        int selectedCount = appointment.getSelectedUserId().size()-1;
        String description = appointment.getDescription();
        final String timestamp = appointment.getTimestamp();
        final HashMap<String, Boolean> confirmUser = appointment.getConfirmUser();
        final ArrayList<String> selectedUserId = appointment.getSelectedUserId();
//        final ArrayList<String> selectedUsername = appointment.getSelectedUserName();

        Log.d(TAG, "onBindViewHolder: ");
        try {
            //set data
            Picasso.get().load(restoLogo).resize(100, 100).transform(new CropCircleTransformation()).into(holder.imageView_logoResto);

            Log.d(TAG, "onBindViewHolder: try");
            holder.textView_restoName.setText(restoName);
            holder.textView_date.setText("Le " + date);
            holder.textView_selectedCount.setText("avec " + selectedCount + "personne(s)");
            holder.textView_description.setText("\"" + description + "\"");

            holder.button_accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    confirmUser.put(currentUser.getUser_id(), true);

                    collectionReference_users.document(selectedUserId.get(0)).
                            collection("AppointmentCollection").document(timestamp)
                            .update("confirmUser", confirmUser)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(context,"Rendez-vous enregistrer!", Toast.LENGTH_SHORT).show();

                                    holder.layout_responseButtons.setVisibility(View.GONE);

                                    notifyAppointmentChange(selectedUserId.get(0), "accepté", date);
                                }
                            });
                }
            });

            holder.button_decline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedUserId.remove(currentUser.getUser_id());

                    collectionReference_users.document(selectedUserId.get(0)).
                            collection("AppointmentCollection").document(timestamp)
                            .update("selectedUser", selectedUserId)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(context,"Rendez-vous annuler", Toast.LENGTH_SHORT).show();
                                    appointmentList.remove(appointment);
                                    holder.mainLayout.setVisibility(View.GONE);

                                    notifyAppointmentChange(selectedUserId.get(0), "refusé", date);
                                }
                            });
                }
            });

            if(confirmUser.get(currentUser.getUser_id())){
                holder.layout_responseButtons.setVisibility(View.GONE);
            }

        }catch (Exception e){
            Log.e(TAG, "AdapterAppointmentList: " + e.getMessage());
        }
    }

    private void notifyAppointmentChange(String id, String response, String date){
        mAppointmentNotificationReference.child(id).child("hasNews").setValue(true);

        //write down the notification message
        Date d = new Date();
        mAppointmentNotificationReference.child(id).child(String.valueOf(d.getTime()))
                .setValue(currentUser.getName() + " a "
                        + response + " votre rendez-vous du " + date);
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
        Button button_accept, button_decline;
        LinearLayout layout_responseButtons, mainLayout;
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
            button_accept = itemView.findViewById(R.id.button_accept);
            button_decline = itemView.findViewById(R.id.button_decline);
            layout_responseButtons = itemView.findViewById(R.id.layout_responseButtons);
            mainLayout = itemView.findViewById(R.id.mainLayout);

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
