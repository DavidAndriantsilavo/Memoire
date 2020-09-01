package mg.didavid.firsttry.Controllers.Fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import mg.didavid.firsttry.Controllers.Activities.ChatActivity;
import mg.didavid.firsttry.Controllers.Activities.LoginActivity;
import mg.didavid.firsttry.Controllers.Activities.ProfileUserActivity;
import mg.didavid.firsttry.Controllers.Activities.UserListActivity;
import mg.didavid.firsttry.Controllers.Adapteurs.AdapteurMessage;
import mg.didavid.firsttry.Models.ModeleChatroom;
import mg.didavid.firsttry.Models.User;
import mg.didavid.firsttry.Models.UserSingleton;
import mg.didavid.firsttry.R;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class MessageFragment extends Fragment implements AdapteurMessage.OnChatRoomListner {

    RecyclerView recyclerView;
    List<ModeleChatroom> chatroomList;
    AdapteurMessage adapteursMessage;

    User currentUser;
    ProgressDialog progressDialog_logout;

    private DatabaseReference mChatroomReference = FirebaseDatabase.getInstance().getReference().child("chatroom");

    public static MessageFragment newInstance() {
        return (new MessageFragment());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message, container, false);

        currentUser = ((UserSingleton)getActivity().getApplicationContext()).getUser();

        adapteursMessage = new AdapteurMessage();

        //init progressDialog
        progressDialog_logout = new ProgressDialog(getContext());
        progressDialog_logout.setMessage("Déconnexion...");

        //recycler view and its proprieties
        recyclerView = view.findViewById(R.id.recyclerView_message);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());

        //show newest post first, for this load from last
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);

        //set Layout to recyclerView
        recyclerView.setLayoutManager(linearLayoutManager);
        //init post list
        chatroomList = new ArrayList<>();

        return view;
    }

    //get and show the chatrooms of the current user
    private void loadChatrooms() {
        mChatroomReference.child(currentUser.getUser_id())
                .orderByChild("last_message_timestamp")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        chatroomList.clear();
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            //Getting User object from dataSnapshot
                            if(data.exists()){
                                ModeleChatroom chatroom = data.getValue(ModeleChatroom.class);
                                chatroomList.add(chatroom);
                            }
                        }
                        configureAdapter();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "%s" + error);
                    }
                });

//        //path of all post
//        final CollectionReference collectionChatrooms = FirebaseFirestore.getInstance().collection("Chatrooms");
//        //get all data from this reference
//        collectionChatrooms.whereArrayContains("id_room", currentUser.getUser_id())
//                .orderBy("last_message_timestamp")
//                .get()
//                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//            @Override
//            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                if (!queryDocumentSnapshots.isEmpty()) {
//                    chatroomList.clear();
//                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
//                        ModeleChatroom modeleChatroom = documentSnapshot.toObject(ModeleChatroom.class);
//                        chatroomList.add(modeleChatroom);
//                    }
//
//                    configureAdapter();
//                }
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_LONG).show();
//
//                Log.e(TAG, "onFailure: " + e.getMessage());
//            }
//        });
    }

    private void configureAdapter(){
        //adapter
        adapteursMessage = new AdapteurMessage(getActivity(), chatroomList, this);
        //set adapter to recyclerView
        recyclerView.setAdapter(adapteursMessage);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true); // to show menu option in fragment
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_activity_main, menu);
        menu.findItem(R.id.menu_search_button).setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //3 - Handle actions on menu items
        switch (item.getItemId()) {
            case R.id.menu_logout_profil:
                avertissement();
                return true;
            case R.id.menu_activity_main_profile:
                startActivity(new Intent(getContext(), ProfileUserActivity.class));
                return true;
            case R.id.menu_activity_main_addNewPost:
                startActivity(new Intent(getContext(), UserListActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void avertissement() {
        if(currentUser!=null)
        {
            //BUILD ALERT DIALOG TO CONFIRM THE SUPPRESSION
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage("Vous voulez vous déconnecter?");
            builder.setCancelable(true);

            builder.setPositiveButton(
                    "OUI",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            progressDialog_logout.show();
                            logOut();
                            dialog.cancel();
                        }
                    });

            builder.setNegativeButton(
                    "NON",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            progressDialog_logout.dismiss();

            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private void logOut() {
        progressDialog_logout.show();
        FirebaseAuth.getInstance().signOut();
        GoogleSignIn.getClient(
                getContext(),
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        ).signOut();

        Intent logOut =  new Intent(getContext(), LoginActivity.class);
        startActivity(logOut);

        getActivity().finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadChatrooms();
    }

    //click event listner on a chatroom
    @Override
    public void onChatRoomClick(int position) {
        Intent intent = new Intent (getContext(), ChatActivity.class);
        intent.putExtra("chatroom_id", chatroomList.get(position).getRoom_id());
        intent.putExtra("other_user_id", chatroomList.get(position).getOther_user_id());
        startActivity(intent);
    }
}
