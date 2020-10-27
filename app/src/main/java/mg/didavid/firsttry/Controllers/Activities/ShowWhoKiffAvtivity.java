package mg.didavid.firsttry.Controllers.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mg.didavid.firsttry.Controllers.Adapteurs.AdapterUsers;
import mg.didavid.firsttry.Models.User;
import mg.didavid.firsttry.R;
import mg.didavid.firsttry.Views.AppMode;

public class ShowWhoKiffAvtivity extends AppMode {

    String post_id;
    int totalKiff;
    TextView textView_totalKiffs;

    private RecyclerView recyclerView_whoKiif;
    private List<User> modelUserList = new ArrayList<>();
    private AdapterUsers adapterUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_who_kiff_avtivity);

        recyclerView_whoKiif = findViewById(R.id.recyclerView_showWhoKiff);
        textView_totalKiffs = findViewById(R.id.textView_totelKiffs);

        //set title to ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar_userProile);
        if (toolbar != null){
            //set toolbar title
            // Sets the Toolbar
            setSupportActionBar(toolbar);
            this.setTitle("Qui ont kiffée");//set tool bar
        }else {
            Toast.makeText(this, "Tsy misy titre :-(", Toast.LENGTH_SHORT).show();
        }

        //get data from intent
        Intent intent = getIntent();
        post_id = intent.getStringExtra("key");

        //get list of users who kiff the post
        DocumentReference documentReference_kiff = FirebaseFirestore.getInstance().collection("Kiffs").document(post_id);
        documentReference_kiff.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value != null) {
                    List<String> stringList_otherUserId = new ArrayList<>();
                    Map<String, Object> otherUser_id = value.getData();
                    stringList_otherUserId.clear();
                    if (otherUser_id != null) {
                        for (Map.Entry<String, Object> entry :otherUser_id.entrySet()) {
                            stringList_otherUserId.add(entry.getKey());
                        }
                    }
                    totalKiff = stringList_otherUserId.size();
                        getUserInfo(stringList_otherUserId);
                }
            }
        });
    }

    private void getUserInfo(final List<String> otherUser_id) {
        //layout for recyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        //set layout into recyclerView
        recyclerView_whoKiif.setLayoutManager(linearLayoutManager);
        CollectionReference collectionReference_user = FirebaseFirestore.getInstance().collection("Users");
        collectionReference_user.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            //get data
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                if (documentSnapshot.exists()) {
                                    modelUserList.clear();
                                    List<User> modelUser = queryDocumentSnapshots.toObjects(User.class);
                                    int size = modelUser.size();
                                    for (String user_id : otherUser_id) {
                                        for (int i = 0; i < size; i++) {
                                            if (modelUser.get(i).getUser_id().contains(user_id)) {
                                                modelUserList.add(modelUser.get(i));
                                                Log.d("users name", modelUser.get(i).getName());
                                            }
                                        }
                                    }

                                    //set total kiffs
                                    textView_totalKiffs.setText("Total : " + totalKiff);
                                    //setup adapter
                                    adapterUsers = new AdapterUsers(ShowWhoKiffAvtivity.this, modelUserList);
                                    //set adapter to recyclerView
                                    recyclerView_whoKiif.setAdapter(adapterUsers);
                                }
                            }
                        }
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
