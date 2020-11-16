package mg.didavid.firsttry.Controllers.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.util.ArrayList;

import mg.didavid.firsttry.Controllers.Adapteurs.AdapterAppointmentList;
import mg.didavid.firsttry.Controllers.Adapteurs.AdapteurUserList;
import mg.didavid.firsttry.Models.ModelAppointment;
import mg.didavid.firsttry.Models.User;
import mg.didavid.firsttry.Models.UserSingleton;
import mg.didavid.firsttry.R;

public class AppointmentListActivity extends AppCompatActivity implements AdapterAppointmentList.OnAppointmentListner{

    private User currentUser;
    private ArrayList<ModelAppointment> appointmentList;
    AdapterAppointmentList adapterAppointmentList;
    private RecyclerView recyclerView;
    private TextView textView_noAppointment;
    private String TAG = "AppointmentActivityList";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_list);

        currentUser = ((UserSingleton)getApplicationContext()).getUser();

        textView_noAppointment = findViewById(R.id.textView_noAppointment);
        //recycler view and its proprieties
        recyclerView = findViewById(R.id.recyclerView_appointmentList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        //show newest appointment first
        linearLayoutManager.setStackFromEnd(false);
        linearLayoutManager.setReverseLayout(false);
        recyclerView.setHasFixedSize(true);

        //set Layout to recyclerView
        recyclerView.setLayoutManager(linearLayoutManager);
        //init post list
        appointmentList = new ArrayList<>();
    }

    private void getAppointmentList(){
        Log.d(TAG, "getAppointmentList");

        FirebaseFirestore.getInstance()
                .collectionGroup("AppointmentCollection")
                .whereArrayContains("selectedUserId", currentUser.getUser_id())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            appointmentList.clear();
                            textView_noAppointment.setVisibility(TextView.GONE);
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                ModelAppointment appointment = documentSnapshot.toObject(ModelAppointment.class);
                                appointmentList.add(appointment);
                            }

                            Log.d(TAG, "appointmentList length : " + appointmentList.size());
                            configureAdapter(appointmentList);
                        }else{
                            textView_noAppointment.setVisibility(TextView.VISIBLE);
                            Log.d(TAG, "0 appointmentList : ");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: " + e.getMessage());
                    }
                });
    }

    private void configureAdapter(ArrayList list){
        //adapter
        adapterAppointmentList = new AdapterAppointmentList(this, list, this);
        //set adapter to recyclerView
        recyclerView.setAdapter(adapterAppointmentList);
    }


    @Override
    public void onUserClick(int position) {
        ModelAppointment appointment = appointmentList.get(position);
        ArrayList<String> selectedUserName = appointment.getSelectedUserName();

        Log.d(TAG, "username length: " + selectedUserName.size());

        selectedUserName.remove(currentUser.getName());
        String text = TextUtils.join("\n \n", selectedUserName);

        new AlertDialog.Builder(this)
                .setTitle("Liste des utilisateurs")
                .setMessage(text)
                .setPositiveButton("Fermer", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        getAppointmentList();
    }
}
