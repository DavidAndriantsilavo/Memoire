package mg.didavid.firsttry.Controllers.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import mg.didavid.firsttry.Controllers.Adapteurs.AdapteurUserList;
import mg.didavid.firsttry.Models.User;
import mg.didavid.firsttry.Models.UserSingleton;
import mg.didavid.firsttry.R;
import mg.didavid.firsttry.Views.AppMode;

public class UserListActivity extends AppMode implements AdapteurUserList.OnUserListner {

    Context context = this;

    RecyclerView recyclerView;
    List<User> userList;
    AdapteurUserList adapteurUserList;

    User currentUser;

    final private String TAG = "userList";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        currentUser = ((UserSingleton)getApplicationContext()).getUser();

        //recycler view and its proprieties
        recyclerView = findViewById(R.id.recyclerView_userList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);

        //show newest post first, for this load from last
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setHasFixedSize(true);

        //set Layout to recyclerView
        recyclerView.setLayoutManager(linearLayoutManager);
        //init post list
        userList = new ArrayList<>();
    }

    //show all user stored in firebase
    private void showUserList(){
        //path of all post
        final CollectionReference collectionUsers = FirebaseFirestore.getInstance().collection("Users");
        //get all data from this reference
        collectionUsers.orderBy("name", Query.Direction.DESCENDING).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    userList.clear();
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                        User user = documentSnapshot.toObject(User.class);

                        if(!user.getUser_id().equals(currentUser.getUser_id())){
                            userList.add(user);
                        }
                    }

                    Log.d(TAG, "userList length : " + userList.size());
                    configureAdapter();
                }
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), ""+e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void configureAdapter(){
        //adapter
        adapteurUserList = new AdapteurUserList(context, userList, this);
        //set adapter to recyclerView
        recyclerView.setAdapter(adapteurUserList);
    }

    @Override
    public void onResume() {
        super.onResume();
        showUserList();
    }

    @Override
    public void onUserClick(int position) {
        Log.d(TAG, "onUserClick: " + userList.get(position).getName());
        Intent intent = new Intent (getApplicationContext(), ChatActivity.class);
        intent.putExtra("other_user_id", userList.get(position).getUser_id());
        intent.putExtra("other_user_name", userList.get(position).getName());
        startActivity(intent);
    }
}
