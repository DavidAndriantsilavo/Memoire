package mg.didavid.firsttry.Utils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import mg.didavid.firsttry.Controllers.Adapteurs.AdapterSelectUser;
import mg.didavid.firsttry.Controllers.Adapteurs.AdapteurUserList;
import mg.didavid.firsttry.Models.User;
import mg.didavid.firsttry.R;

import static com.google.firebase.inappmessaging.internal.Logging.TAG;

public class SelectUserDialog extends AppCompatDialogFragment implements  AdapterSelectUser.OnSelectUserListner{
    private ArrayList <User> selectedUserList;
    private ArrayList <User> userList;
    private RecyclerView recyclerView_userList;
    private SearchView searchView;
    private SelectUserDialog.SelectUserDialogListner listner;

    private Context context;
    private LinearLayoutManager layoutManager;
    private AdapterSelectUser adapterSelectUser;

    public SelectUserDialog() {
    }

    public SelectUserDialog(Context context){
        this.context = context;

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.select_user_layout, null);

        recyclerView_userList = view.findViewById(R.id.recyclerView_userList);
        searchView = view.findViewById(R.id.searchView);
        recyclerView_userList = view.findViewById(R.id.recyclerView_userList);
        userList = new ArrayList<>();
        selectedUserList = new ArrayList<>();

        layoutManager = new LinearLayoutManager(context);
        //show newest post first, for this load from last
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        recyclerView_userList.setHasFixedSize(true);

        //set Layout to recyclerView
        recyclerView_userList.setLayoutManager(layoutManager);

        showUserList();

        builder.setView(view)
                .setTitle("Selectionner")
                .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("Valider", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        if(!selectedUser.isEmpty()){
//                            listner.getSelectedUser(selectedUser);
//                        }
                    }
                });

        return builder.create();
    }

    @Override
    public void onSelectUserClick(int position) {
        Log.d(TAG, "item click : " + position);
    }

    public interface SelectUserDialogListner{
        void getSelectedUser(ArrayList <User> selectedUser);
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
                        userList.add(user);
                    }

                    Log.d(TAG, "userList length : " + userList.size());
                    configureAdapter(userList);
                }
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void configureAdapter(ArrayList list){
        //adapter
        adapterSelectUser = new AdapterSelectUser(context, userList, this);
        //set adapter to recyclerView
        recyclerView_userList.setAdapter(adapterSelectUser);
    }
}
