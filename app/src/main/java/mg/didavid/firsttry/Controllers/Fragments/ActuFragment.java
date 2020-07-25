package mg.didavid.firsttry.Controllers.Fragments;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuCompat;
import androidx.core.view.MenuItemCompat;
import androidx.core.widget.CompoundButtonCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.collect.ImmutableList;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import mg.didavid.firsttry.Controllers.Activities.MainActivity;
import mg.didavid.firsttry.Controllers.Adapteurs.AdapteursPost;
import mg.didavid.firsttry.Controllers.Modeles.ModelePost;
import mg.didavid.firsttry.R;

public class ActuFragment extends Fragment {

    RecyclerView recyclerView;
    List<ModelePost> modelePostList;
    AdapteursPost adapteursPost;

    public static ActuFragment newInstance() {
        return (new ActuFragment());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_actu, container, false);

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
        MenuItem item = menu.findItem(R.id.menu_search_button);
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

    public void searchPost(final String query) {
        //path of all post
        CollectionReference collectionUsers = FirebaseFirestore.getInstance().collection("Publications");
        //get all data from this reference
        collectionUsers.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                modelePostList.clear();
                for (DocumentSnapshot documentSnapshot: queryDocumentSnapshots.getDocuments()){
                    List<ModelePost> modelePost = queryDocumentSnapshots.toObjects(ModelePost.class);

                    if (modelePost.get(5).getpTitre().toLowerCase().contains(query.toLowerCase()) ||
                        modelePost.get(6).getpDescription().toLowerCase().contains(query.toLowerCase())) {
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

    public void loadPosts() {
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
                        int size = modelePost.size();
                        Log.d("nombre d'element", "*************************************************************************************" + size);

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
}
