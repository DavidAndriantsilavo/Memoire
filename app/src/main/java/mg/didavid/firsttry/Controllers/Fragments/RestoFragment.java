package mg.didavid.firsttry.Controllers.Fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Collections;
import java.util.List;

import mg.didavid.firsttry.Controllers.Activities.LoginActivity;
import mg.didavid.firsttry.Controllers.Activities.NewPostActivity;
import mg.didavid.firsttry.Controllers.Activities.ProfileRestoActivity;
import mg.didavid.firsttry.Controllers.Activities.ProfileUserActivity;
import mg.didavid.firsttry.Controllers.Activities.RestoRegisterActivity;
import mg.didavid.firsttry.Models.ModelResto;
import mg.didavid.firsttry.R;

public class RestoFragment extends Fragment {

    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private ProgressDialog progressDialog_logout;


    public static RestoFragment newInstance() {
        return (new RestoFragment());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_resto, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true); // to show menu option in fragment
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_activity_main, menu);
        menu.findItem(R.id.menu_search_button).setVisible(false);
        menu.findItem(R.id.menu_logout_profil).setVisible(false);
        menu.findItem(R.id.menu_activity_main_addNewPost).setVisible(false);
        menu.findItem(R.id.menu_activity_main_profile).setVisible(false);
        final String user_id = user.getUid();
        CollectionReference collectionReference_resto = FirebaseFirestore.getInstance().collection("Resto");
        collectionReference_resto.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<ModelResto> modelRestos = queryDocumentSnapshots.toObjects(ModelResto.class);
                            int size = modelRestos.size();
                            for ( int i = 0; i < size; i++) {
                                if (modelRestos.get(i).getId_resto().contains(user_id)) {
                                    //the user have already one restaurant account so, hide menu add restaurant account and show menu view profile
                                    menu.findItem(R.id.menu_activity_main_profile).setVisible(true);
                                    break;
                                }else {
                                    menu.findItem(R.id.menu_activity_main_addNewPost).setVisible(true);
                                }
                            }
                        }else {
                            //the document is empty, so allow user to add restaurant account
                            menu.findItem(R.id.menu_activity_main_profile).setVisible(false);
                            menu.findItem(R.id.menu_activity_main_addNewPost).setVisible(true);
                        }
                    }
                });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //3 - Handle actions on menu items
        switch (item.getItemId()) {
            case R.id.menu_activity_main_profile:
                startActivity(new Intent(getContext(), ProfileRestoActivity.class));
                return true;
            case R.id.menu_activity_main_addNewPost:
                startActivity(new Intent(getContext(), RestoRegisterActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
