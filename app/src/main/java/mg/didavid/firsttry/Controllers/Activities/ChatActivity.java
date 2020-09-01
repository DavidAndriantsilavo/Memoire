package mg.didavid.firsttry.Controllers.Activities;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mg.didavid.firsttry.Controllers.Adapteurs.AdapteurChat;
import mg.didavid.firsttry.Models.Message;
import mg.didavid.firsttry.Models.ModeleChatroom;
import mg.didavid.firsttry.Models.User;
import mg.didavid.firsttry.Models.UserSingleton;
import mg.didavid.firsttry.R;

public class ChatActivity extends AppCompatActivity {

    Context context = this;

    RecyclerView recyclerView;
    List<Message> messageList;
    AdapteurChat adapateurChat;

    User currentUser;

    private DatabaseReference mMessageReference = FirebaseDatabase.getInstance().getReference().child("chats");
    private DatabaseReference mChatroomReference = FirebaseDatabase.getInstance().getReference().child("chatroom");

    EditText editText_messageContent;
    ImageButton imageButton_send;

    final private String TAG = "userList";
    private String otherUserId, otherUserName, chatroomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        currentUser = ((UserSingleton)getApplicationContext()).getUser();

        imageButton_send = findViewById(R.id.imageButton_send);
        editText_messageContent = findViewById(R.id.editText_messageContent);

        //recycler view and its proprieties
        recyclerView = findViewById(R.id.recyclerView_chat);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);

        //show newest post first, for this load from last
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(false);

        //set Layout to recyclerView
        recyclerView.setLayoutManager(linearLayoutManager);
        //init post list
        messageList = new ArrayList<>();

        checkIntentExtras();

        imageButton_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    //send wrote message and store in DB
    private void sendMessage(){
        if(!TextUtils.isEmpty(editText_messageContent.getText().toString().trim())) {
            final Message message = new Message();
            final String timestamp = String.valueOf(System.currentTimeMillis());

            message.setSender_id(currentUser.getUser_id());
            message.setContent(editText_messageContent.getText().toString());
            message.setTimestamp(timestamp);

            mMessageReference.child(chatroomId).push().setValue(message).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        //Task was successful, data written!
                        Log.d(TAG, "chatroom : message sent");

                        editText_messageContent.setText("");

                        messageList.add( message);

                        adapateurChat.notifyItemInserted(messageList.size());

                        recyclerView.smoothScrollToPosition(messageList.size()-1);

                        updateLastMessage(message);
                    }else

                        //Log the error message
                        Log.e(TAG, "onComplete: ERROR: " + task.getException().getLocalizedMessage() );
                }
            });

            return;
        }
    }


    //update the lastMmessage in the chatroom
    private void updateLastMessage(Message message){
//        mCollectionChatrooms.document(chatroomId).update("last_message", message.getContent());
//        mCollectionChatrooms.document(chatroomId).update("last_message_timestamp", message.getTimestamp());

        Map<String, Object> messageUpdate = new HashMap<>();
        messageUpdate.put("/last_message" , message.getContent());
        messageUpdate.put("/last_message_timestamp", message.getTimestamp());

        mChatroomReference.child(currentUser.getUser_id()).child(chatroomId).updateChildren(messageUpdate);
        mChatroomReference.child(otherUserId).child(chatroomId).updateChildren(messageUpdate);
    }


    //check for the sent extras from intent
    private void checkIntentExtras(){

        //if extras came from userListActivity

        if(getIntent().hasExtra("other_user_id") && getIntent().hasExtra("other_user_name")){
            //EXTRA FROM USERLISTACTIVITY
            otherUserId = getIntent().getStringExtra("other_user_id");
            otherUserName = getIntent().getStringExtra("other_user_name");

            Log.d(TAG, " chatroom : get intent extra user_name : " + otherUserName);

            mChatroomReference.child(currentUser.getUser_id())
                    .orderByChild("other_user_id")
                    .equalTo(otherUserId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.d(TAG, " chatroom : test");

                    if(dataSnapshot.exists()){
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            //Getting User object from dataSnapshot
                            if(data.exists()){
                                ModeleChatroom chatroom = data.getValue(ModeleChatroom.class);

                                chatroomId = chatroom.getRoom_id();

                                Log.d(TAG, " chatroom : get chatroom id : " + chatroomId);

                                showMessage(chatroomId);
                            }
                        }
                    }else{
                        Log.d(TAG, "chatroom : Creating new chatroom");

                        final String other_user_id = otherUserId;
                        final String other_user_name = otherUserName;
                        final String room_id = currentUser.getUser_id() + "_" + other_user_id;
                        final String last_message = "";
                        final String last_message_timestamp = String.valueOf(System.currentTimeMillis());

                        final ModeleChatroom chatroom = new ModeleChatroom(other_user_id, other_user_name, room_id, last_message, last_message_timestamp);

                        chatroomId = room_id;

                        mChatroomReference.child(currentUser.getUser_id()).child(chatroomId).setValue(chatroom).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    //Task was successful, data written!
                                    Log.d(TAG, "chatroom : chatroom created into currentUser");

                                    createRoomForOtherUser(chatroom);

                                }else
                                    //Log the error message
                                    Log.e(TAG, "onComplete: ERROR: " + task.getException().getLocalizedMessage() );
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "%s" + error);
                }
            });

        }
        //if extras came from MessageFramgment
        else if(getIntent().hasExtra("chatroom_id") && getIntent().hasExtra("other_user_id") && getIntent().hasExtra("other_user_name")){
            //EXTRA FROM MESSAGEFRAGMENT (CHATROOM LIST°
            chatroomId = getIntent().getStringExtra("chatroom_id");
            otherUserId = getIntent().getStringExtra("other_user_id");
            otherUserName = otherUserId = getIntent().getStringExtra("other_user_name");

            Log.d(TAG, " chatroom : get intent extra chatroom_id : " + chatroomId);

            showMessage(chatroomId);
        }
    }


    //duplicate the created new chatroom in the second user's field
    private void createRoomForOtherUser(ModeleChatroom chatroom){
        chatroom.setOther_user_id(currentUser.getUser_id());
        chatroom.setOther_user_name(currentUser.getName());
        mChatroomReference.child(otherUserId).child(chatroomId).setValue(chatroom).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    //Task was successful, data written!
                    Log.d(TAG, "chatroom : chatroom created into otherUser");

                    showMessage(chatroomId);

                }else
                    //Log the error message
                    Log.e(TAG, "onComplete: ERROR: " + task.getException().getLocalizedMessage() );
            }
        });
    }

    private void showMessage(String chatroomId){

        configureToolbar(otherUserName);
        mMessageReference.child(chatroomId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    //Getting User object from dataSnapshot
                    if(data.exists()){
                        Message message = data.getValue(Message.class);

                        messageList.add(message);
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
        //adapter
        adapateurChat = new AdapteurChat(context, messageList);
        //set adapter to recyclerView
        recyclerView.setAdapter(adapateurChat);
    }

    private void configureToolbar(String name){
        // Get the toolbar view inside the activity layout
        Log.d(TAG, "chatroom : configure toolbar" + name);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null){
            //set toolbar title
            // Sets the Toolbar
            setSupportActionBar(toolbar);
            this.setTitle(name);

            toolbar.setTitleTextAppearance(this, R.style.toolBarOtherUsers);

            Log.d(TAG, "chatroom : configure toolbar done");
        }else {
            Toast.makeText(this, "Tsy misy titre :-(", Toast.LENGTH_SHORT).show();

            Log.d(TAG, "chatroom : configure toolbar error");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    //THE FOLLOWING METHOD IS USED TO DETACH EDIT_TEXT FOCUS WHEN WE CLICK OUTSIDE OF IT
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }
}
