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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.internal.$Gson$Preconditions;

import org.w3c.dom.Comment;

import java.util.ArrayList;
import java.util.List;

import mg.didavid.firsttry.Controllers.Activities.ChatActivity;
import mg.didavid.firsttry.Controllers.Activities.LoginActivity;
import mg.didavid.firsttry.Controllers.Activities.ProfileUserActivity;
import mg.didavid.firsttry.Controllers.Activities.UserListActivity;
import mg.didavid.firsttry.Controllers.Adapteurs.AdapteurMessage;
import mg.didavid.firsttry.Models.Message;
import mg.didavid.firsttry.Models.ModeleChatroom;
import mg.didavid.firsttry.Models.User;
import mg.didavid.firsttry.Models.UserSingleton;
import mg.didavid.firsttry.R;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class MessageFragment extends Fragment implements AdapteurMessage.OnChatRoomListner {

    RecyclerView recyclerView;
    List<ModeleChatroom> chatroomList;
    AdapteurMessage adapteursMessage;
    TextView textView_noMessage;

    User currentUser;

    ProgressDialog progressDialog;

    private DatabaseReference mChatroomReference = FirebaseDatabase.getInstance().getReference().child("chatroom");
    private Context mContext;

    public static MessageFragment newInstance() {
        return (new MessageFragment());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message, container, false);

        textView_noMessage = view.findViewById(R.id.textView_noMessage);

        adapteursMessage = new AdapteurMessage();

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

        (view.findViewById(R.id.floatingbtn_message)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), UserListActivity.class));
            }
        });

        return view;
    }

    //get and show the chatrooms of the current currentUser
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
    }

    private void configureAdapter(){
        if(chatroomList.isEmpty()){
            textView_noMessage.setVisibility(View.VISIBLE);
        }else{
            textView_noMessage.setVisibility(View.GONE);
        }

        //adapter
        adapteursMessage = new AdapteurMessage(mContext, chatroomList, this);
        //set adapter to recyclerView
        recyclerView.setAdapter(adapteursMessage);

        progressDialog.dismiss();
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
        if (item.getItemId() == R.id.menu_activity_main_profile) {
            startActivity(new Intent(getContext(), ProfileUserActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mContext = getActivity();
        currentUser = ((UserSingleton) mContext.getApplicationContext()).getUser();

        progressDialog = new ProgressDialog(mContext);
        progressDialog.setMessage("Chargement ...");
        progressDialog.show();

        //start the listner of new message
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                // A new comment has been added, add it to the displayed list
                ModeleChatroom chatroom = dataSnapshot.getValue(ModeleChatroom.class);

                loadChatrooms();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());

                // A comment has changed, use the key to determine if we are displaying this
                // comment and if so displayed the changed comment.
                ModeleChatroom chatroom = dataSnapshot.getValue(ModeleChatroom.class);
                loadChatrooms();

                // ...
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

                // A comment has changed, use the key to determine if we are displaying this
                // comment and if so remove it.
                String commentKey = dataSnapshot.getKey();

                // ...
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());

                // A comment has changed position, use the key to determine if we are
                // displaying this comment and if so move it.
                Comment movedComment = dataSnapshot.getValue(Comment.class);
                String commentKey = dataSnapshot.getKey();

                // ...
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "postComments:onCancelled", databaseError.toException());
            }
        };

        mChatroomReference.child(currentUser.getUser_id()).addChildEventListener(childEventListener);
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
        intent.putExtra("other_user_name", chatroomList.get(position).getOther_user_name());
        startActivity(intent);
    }
}
