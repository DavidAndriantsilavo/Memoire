package mg.didavid.firsttry.Controllers.Fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import mg.didavid.firsttry.Controllers.Activities.LoginActivity;
import mg.didavid.firsttry.Controllers.Activities.NewPostActivity;
import mg.didavid.firsttry.Controllers.Activities.ProfileUserActivity;
import mg.didavid.firsttry.Controllers.Adapteurs.AdapteursPost;
import mg.didavid.firsttry.Models.ModelePost;
import mg.didavid.firsttry.R;

public class ActuFragment extends Fragment {

    RecyclerView recyclerView;
    List<ModelePost> modelePostList;
    AdapteursPost adapteursPost;

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    ProgressDialog progressDialog_logout;

    public static ActuFragment newInstance() {
        return (new ActuFragment());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_actu, container, false);

        //init progressDialog
        progressDialog_logout = new ProgressDialog(getContext());
        progressDialog_logout.setMessage("Déconnexion...");

        //recycler view and its proprieties
        recyclerView = view.findViewById(R.id.postRecyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());

        //show newest post first, for this load from last
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);

        //set Layout to recyclerView
        recyclerView.setLayoutManager(linearLayoutManager);

        //init post list
        modelePostList = new ArrayList<>();

        loadPosts();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPosts();
    }

    //inflate option menu
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        //inflate menu
        inflater.inflate(R.menu.menu_activity_main, menu);

        //searchView to seach post by title or description
        MenuItem item =  menu.findItem(R.id.menu_search_button);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //called when user press search button
                if (!TextUtils.isEmpty(query)){
                    searchPost(query);
                }else {
                    loadPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //called as and when user press any lettre
                if (!TextUtils.isEmpty(newText)){
                    searchPost(newText);
                }else {
                    loadPosts();
                }
                return false;
            }
        });

    }

    private void searchPost(final String query) {
        //path of all post
        CollectionReference collectionUsers = FirebaseFirestore.getInstance().collection("Publications");
        //get all data from this reference
        collectionUsers.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (DocumentSnapshot documentSnapshot: queryDocumentSnapshots.getDocuments()){
                    modelePostList.clear(); //for deleting auto redundancy
                    List<ModelePost> modelePost = queryDocumentSnapshots.toObjects(ModelePost.class);

                    //à reviser quand toutes les données sont en ordre
                    if (modelePost.get(5).getPost_title().toLowerCase().contains(query.toLowerCase()) ||
                        modelePost.get(6).getPost_description().toLowerCase().contains(query.toLowerCase())) {
                        modelePostList.addAll(modelePost);
                    }

                    //adapter
                    adapteursPost = new AdapteursPost(getContext(), modelePostList);
                    //set adapter to recyclerView
                    recyclerView.setAdapter(adapteursPost);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadPosts() {
        //path of all post
        CollectionReference collectionUsers = FirebaseFirestore.getInstance().collection("Publications");
        //get all data from this reference
        collectionUsers.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                        modelePostList.clear();
                        List<ModelePost> modelePost = queryDocumentSnapshots.toObjects(ModelePost.class);
                        modelePostList.addAll(modelePost);

                        //adapter
                        adapteursPost = new AdapteursPost(getContext(), modelePostList);
                        //set adapter to recyclerView
                        recyclerView.setAdapter(adapteursPost);
                    }
                }else{
                    //Toast.makeText(getActivity(), "Le document est vide ou il est il y a erreurs", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true); // to show menu option in fragment
        super.onCreate(savedInstanceState);
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
                startActivity(new Intent(getContext(), NewPostActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void avertissement() {
        if(user!=null)
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

}
