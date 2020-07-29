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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import mg.didavid.firsttry.Controllers.Activities.LoginActivity;
import mg.didavid.firsttry.Controllers.Activities.MainActivity;
import mg.didavid.firsttry.Controllers.Activities.NewPostActivity;
import mg.didavid.firsttry.Controllers.Activities.ProfileUserActivity;
import mg.didavid.firsttry.R;

public class RestoFragment extends Fragment {

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    ProgressDialog progressDialog_logout;


    public static RestoFragment newInstance() {
        return (new RestoFragment());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        //init progressDialog
        progressDialog_logout = new ProgressDialog(getContext());
        progressDialog_logout.setMessage("Déconnexion...");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_page2, container, false);
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
