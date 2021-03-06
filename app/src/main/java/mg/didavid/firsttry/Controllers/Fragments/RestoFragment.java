package mg.didavid.firsttry.Controllers.Fragments;

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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import mg.didavid.firsttry.Controllers.Activities.ProfileRestoActivity;
import mg.didavid.firsttry.Controllers.Activities.RestoRegisterActivity;
import mg.didavid.firsttry.Controllers.Adapteurs.AdapterRestoPresentation;
import mg.didavid.firsttry.Models.ModelResto;
import mg.didavid.firsttry.Models.ModelRestoSampleMenu;
import mg.didavid.firsttry.R;

public class RestoFragment extends Fragment {

    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private RecyclerView recyclerView_restoFragment;
    private AdapterRestoPresentation adapterRestoPresentation;
    private List<ModelResto> modelRestoList;
    private TextView textView_aboutSinginResto;

    private String user_id;

    public static RestoFragment newInstance() {
        return (new RestoFragment());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_resto, container, false);
        modelRestoList = new ArrayList<>();

        //set recycler view
        textView_aboutSinginResto = view.findViewById(R.id.textView_aboutRestoRregister_restoFragment);
        recyclerView_restoFragment = view.findViewById(R.id.restoRecyclerview);
        recyclerView_restoFragment.setHasFixedSize(true);
        recyclerView_restoFragment.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        user_id = user.getUid();

        setData();

        return view;
    }

    private void setData() {
        final CollectionReference collectionReference_resto = FirebaseFirestore.getInstance().collection("Resto");
        collectionReference_resto.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                collectionReference_resto
                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                if (value != null && !value.isEmpty()) {
                                    modelRestoList.clear();
                                    for (DocumentSnapshot ds : value.getDocuments()) {
                                        //get resto informations

                                        final ModelResto modelResto = new ModelResto();

                                        modelResto.setName_resto(ds.getString("name_resto"));
                                        modelResto.setSpeciality_resto(ds.getString("speciality_resto"));
                                        modelResto.setRating_resto(ds.getString("rating_resto"));
                                        modelResto.setNbrRating_resto(ds.getString("nbrRating_resto"));
                                        modelResto.setLogo_resto(ds.getString("logo_resto"));
                                        modelResto.setLatitude(ds.getDouble("latitude"));
                                        modelResto.setLongitude(ds.getDouble("longitude"));
                                        final String id_resto = ds.getString("id_resto");
                                        modelResto.setId_resto(id_resto);
                                        final List<ModelRestoSampleMenu> modelRestoSampleMenuList = new ArrayList<>();

                                        final CollectionReference collectionReference_sampleMenu = FirebaseFirestore.getInstance().collection("Sample_menu");
                                        collectionReference_sampleMenu.get()
                                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                        collectionReference_sampleMenu.addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                                    @Override
                                                                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                                                        if (value != null && !value.isEmpty()) {
                                                                            modelRestoSampleMenuList.clear();
                                                                            List<ModelRestoSampleMenu> restoSampleMenus = value.toObjects(ModelRestoSampleMenu.class);
                                                                            int size = restoSampleMenus.size();
                                                                            for (int i = 0; i < size; i++) {
                                                                                if (restoSampleMenus.get(i).getId_resto().contains(id_resto)) {
                                                                                    modelRestoSampleMenuList.add(restoSampleMenus.get(i));
                                                                                }
                                                                            }
                                                                            modelResto.setSampleMenuList(modelRestoSampleMenuList);
                                                                            modelRestoList.add(modelResto);
                                                                            //set adapter to thi recycler view
                                                                            adapterRestoPresentation = new AdapterRestoPresentation(getContext(), modelRestoList, getFragmentManager());
                                                                            recyclerView_restoFragment.setAdapter(adapterRestoPresentation);

                                                                        } else {
                                                                            modelRestoList.add(modelResto);
                                                                            //set adapter to thi recycler view
                                                                            adapterRestoPresentation = new AdapterRestoPresentation(getContext(), modelRestoList, getFragmentManager());
                                                                            recyclerView_restoFragment.setAdapter(adapterRestoPresentation);
                                                                        }
                                                                    }
                                                                });
                                                    }
                                                });
                                    }
                                }
                            }
                        });
                    }
                });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true); // to show menu option in fragment
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull final Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_activity_main, menu);

        menu.findItem(R.id.menu_logout_profil).setVisible(false);
        menu.findItem(R.id.menu_activity_main_profile).setVisible(false);
        menu.findItem(R.id.menu_activity_main_addNewPost).setVisible(false);

        final boolean[] userHasRestoAccount = {false};

        CollectionReference collectionReference_resto = FirebaseFirestore.getInstance().collection("Resto");
        collectionReference_resto.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<ModelResto> modelRestos = queryDocumentSnapshots.toObjects(ModelResto.class);
                            int size = modelRestos.size();
                            for (int i = 0; i < size; i++) {
                                if (!userHasRestoAccount[0]) {
                                    if (modelRestos.get(i).getId_resto().contains(user_id)) {
                                        //the currentUser have already one restaurant account so, hide menu add restaurant account and show menu view profile
                                        menu.findItem(R.id.menu_activity_main_profile).setVisible(true);
                                        menu.findItem(R.id.menu_activity_main_addNewPost).setVisible(false);
                                        textView_aboutSinginResto.setVisibility(View.GONE);
                                        userHasRestoAccount[0] = true;
                                        break;
                                    }else {
                                        //current currentUser doesn't have resto account, allow him to add new resto account
                                        menu.findItem(R.id.menu_activity_main_addNewPost).setVisible(true);
                                    }
                                }
                            }
                        }else {
                            //current currentUser doesn't have resto account, allow him to add new resto account
                            menu.findItem(R.id.menu_activity_main_addNewPost).setVisible(true);
                        }
                    }
                });

        //searchView to seach post bydescription
        MenuItem item_search =  menu.findItem(R.id.menu_search_button);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item_search);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //called when currentUser press search button
                if (!TextUtils.isEmpty(query)){
                    searchResto(query);
                }else {
                    setData();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //called as and when currentUser press any lettre
                if (!TextUtils.isEmpty(newText)){
                    searchResto(newText);
                }else {
                    setData();
                }
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }


    private void searchResto(final String query) {
        //path of all Restaurants
        final CollectionReference collectionUsers = FirebaseFirestore.getInstance().collection("Resto");
        //get all data from this reference
        collectionUsers.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                modelRestoList.clear(); //for deleting auto redundancy
                List<ModelResto> modelRestos = queryDocumentSnapshots.toObjects(ModelResto.class);
                int size = modelRestos.size();
                for (int i = 0; i < size; i++) {
                    if (modelRestos.get(i).getName_resto().toLowerCase().contains(query.toLowerCase())
                    || modelRestos.get(i).getRating_resto().contains(query)) {
                        modelRestoList.add(modelRestos.get(i));
                    }
                }
                //adapter
                adapterRestoPresentation = new AdapterRestoPresentation(getContext(), modelRestoList, getFragmentManager());
                //set adapter to recyclerView
                recyclerView_restoFragment.setAdapter(adapterRestoPresentation);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
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
