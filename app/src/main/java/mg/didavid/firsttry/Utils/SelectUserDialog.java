package mg.didavid.firsttry.Utils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
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

import mg.didavid.firsttry.Controllers.Adapteurs.AdapterSelectUser;
import mg.didavid.firsttry.Controllers.Adapteurs.AdapterSelectedUser;
import mg.didavid.firsttry.Models.User;
import mg.didavid.firsttry.Models.UserSingleton;
import mg.didavid.firsttry.R;

import static com.google.firebase.inappmessaging.internal.Logging.TAG;

public class SelectUserDialog extends AppCompatDialogFragment implements  AdapterSelectUser.OnSelectUserListner, AdapterSelectedUser.OnSelectedUserListner {
    private ArrayList <User> selectedUserList;
    private ArrayList <User> userList;
    private RecyclerView recyclerView_userList;
    private RecyclerView recyclerView_selectedList;
    private View view_separator;
    private SearchView searchView;
    private SelectUserDialog.SelectUserDialogListner listner;

    private Context context;
    private LinearLayoutManager selectLayoutManager, selectedLayoutManager;
    private AdapterSelectUser adapterSelectUser;
    private AdapterSelectedUser adapterSelectedUser;
    private User currentUser;

    public SelectUserDialog() {
    }

    public SelectUserDialog(Context context, ArrayList<User> userList, ArrayList<User> selectedUserList){
        this.context = context;
        this.selectedUserList = selectedUserList;
        this.userList = userList;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.select_user_dialog_layout, null);

        currentUser = ((UserSingleton) getActivity().getApplicationContext()).getUser();

        recyclerView_userList = view.findViewById(R.id.recyclerView_userList);
        searchView = view.findViewById(R.id.searchView);
        recyclerView_userList = view.findViewById(R.id.recyclerView_userList);
        recyclerView_selectedList = view.findViewById(R.id.recyclerView_selectedList);
        view_separator = view.findViewById(R.id.view_separator);

        selectLayoutManager = new LinearLayoutManager(context);
        selectLayoutManager.setStackFromEnd(false);
        selectLayoutManager.setReverseLayout(false);

        selectedLayoutManager = new LinearLayoutManager(context);
        selectedLayoutManager.setStackFromEnd(false);
        selectedLayoutManager.setReverseLayout(false);

        recyclerView_userList.setHasFixedSize(true);
        recyclerView_userList.setLayoutManager(selectLayoutManager);

        recyclerView_selectedList.setHasFixedSize(true);
        recyclerView_selectedList.setLayoutManager(selectedLayoutManager);

        userList.removeAll(selectedUserList);

        adapterSelectUser = new AdapterSelectUser(context, userList, this);
        recyclerView_userList.setAdapter(adapterSelectUser);

        adapterSelectedUser = new AdapterSelectedUser(context, selectedUserList, this);
        recyclerView_selectedList.setAdapter(adapterSelectedUser);

//        showUserList();

        builder.setView(view)
                .setTitle("Selectionner des amis")
                .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("Valider", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                            listner.getSelectedUser(selectedUserList);
                    }
                });

        return builder.create();
    }

    //handle click on user list to select
    @Override
    public void onSelectUserClick(int position) {
        selectedUserList.add(userList.get(position));
//        adapterSelectedUser.notifyItemInserted(selectedUserList.size()-1);
        adapterSelectedUser = new AdapterSelectedUser(context, selectedUserList, this);
        recyclerView_selectedList.setAdapter(adapterSelectedUser);

        Log.d(TAG, "onSelectedUserClick: " + userList.get(position));

        userList.remove(position);
        adapterSelectUser.notifyItemRemoved(position);

        if(selectedUserList.size() > 3){
            recyclerView_selectedList.getLayoutParams().height = 360;
        }
    }

    //handle click in selected user list
    @Override
    public void onSelectedUserClick(int position) {
        userList.add(selectedUserList.get(position));
        adapterSelectUser.notifyItemInserted(userList.size()-1);

        Log.d(TAG, "onSelectUserClick: " + selectedUserList.get(position));

        selectedUserList.remove(position);
        adapterSelectedUser = new AdapterSelectedUser(context, selectedUserList, this);
        recyclerView_selectedList.setAdapter(adapterSelectedUser);

        if(selectedUserList.size() <= 3){
            recyclerView_selectedList.getLayoutParams().height = RecyclerView.LayoutParams.WRAP_CONTENT;
        }
    }

    //show all user stored in firebase
//    private void showUserList(){
//        //path of all post
//        final CollectionReference collectionUsers = FirebaseFirestore.getInstance().collection("Users");
//        //get all data from this reference
//        collectionUsers.whereNotEqualTo("name", currentUser.getName())
//                .orderBy("name", Query.Direction.ASCENDING)
//                .get()
//                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//            @Override
//            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                if (!queryDocumentSnapshots.isEmpty()) {
//                    userList.clear();
//                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
//                        User user = documentSnapshot.toObject(User.class);
//                        userList.add(user);
//                    }
//                    configureAdapter(userList);
//                }
//            }
//
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_LONG).show();
//            }
//        });
//    }

//    private void configureAdapter(ArrayList list){
//        if(!selectedUserList.isEmpty()){
//            for(int i = 0; i < selectedUserList.size(); i++){
//                Log.d(TAG, "selectedUserList: " + selectedUserList.get(i));
//            }
//            for(int i = 0; i < userList.size(); i++){
//                Log.d(TAG, "userList: " + userList.get(i));
//            }
//        }
//
//        adapterSelectUser = new AdapterSelectUser(context, list, this);
//        recyclerView_userList.setAdapter(adapterSelectUser);
//    }

    public interface SelectUserDialogListner{
        void getSelectedUser(ArrayList <User> selectedUser);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listner =  (SelectUserDialog.SelectUserDialogListner) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " implement listner first");
        }
    }
}
