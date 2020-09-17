package mg.didavid.firsttry.Controllers.Fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import mg.didavid.firsttry.Controllers.Activities.LoginActivity;
import mg.didavid.firsttry.Controllers.Activities.ProfileUserActivity;
import mg.didavid.firsttry.Controllers.Activities.UserListActivity;
import mg.didavid.firsttry.R;

public class ParametreFragment extends Fragment {
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private ProgressDialog progressDialog_logout;

    private LinearLayout defaultRay, changeLanguage, userListe, userOnline, myFavoritePlaces, about, help, developersContacts;
    private Button btn_logout;
    private Switch switchPosition, switchNightMode;

    public static int defaultRadius = 10;

    public static ParametreFragment newInstance() {
        return (new ParametreFragment());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_parametres, container, false);
        //init view
        defaultRay = view.findViewById(R.id.linearLayout_defaultRay_parametre);
        changeLanguage = view.findViewById(R.id.linearLayout_changeLanguage_parametre);
        userListe = view.findViewById(R.id.linearLayout_userList_parametre);
        userOnline = view.findViewById(R.id.linearLayout_userOnline_parametre);
        myFavoritePlaces = view.findViewById(R.id.linearLayout_myFavoritePlaces_parametre);
        about = view.findViewById(R.id.linearLayout_about_parametre);
        help = view.findViewById(R.id.linearLayout_help_parametre);
        developersContacts = view.findViewById(R.id.linearLayout_contact_parametre);
        btn_logout = view.findViewById(R.id.btn_logout_parametre);
        switchPosition = view.findViewById(R.id.switch_parametre);
        switchNightMode = view.findViewById(R.id.switch_theme_parametre);

        //init progressDialog
        progressDialog_logout = new ProgressDialog(getContext());
        progressDialog_logout.setMessage("Déconnexion...");

        //add on click listener to views
        defaultRay.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });
        switchPosition.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    FirebaseDatabase.getInstance().getReference().child("userLocation").child(user.getUid()).child("seeMyPosition").setValue(true);
                }else {
                    FirebaseDatabase.getInstance().getReference().child("userLocation").child(user.getUid()).child("seeMyPosition").setValue(false);
                }
            }
        });
        switchNightMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            }
        });
        changeLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        userListe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), UserListActivity.class));
                getActivity().finish();
            }
        });
        userOnline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        myFavoritePlaces.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        developersContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                avertissement();
            }
        });

        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("SetTextI18n")
    private void showDialog() {
        final Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.seek_bar_dialog);
        dialog.setCanceledOnTouchOutside(false);

        //get view
        final SeekBar seekBar = dialog.findViewById(R.id.seekBar_defaultRadius_parametre);
        final EditText editText_defaultRadius = dialog.findViewById(R.id.editText_defaultRadius_parametre);
        Button button_annuler = dialog.findViewById(R.id.btn_annuler_parametre);
        Button button_valider = dialog.findViewById(R.id.btn_valider_parametre);

        button_annuler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        button_valider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDefaultRadius(Integer.parseInt(editText_defaultRadius.getText().toString()));
                dialog.dismiss();
            }
        });

        seekBar.setProgress(defaultRadius);
        editText_defaultRadius.setText("" + (defaultRadius * 100));


        if (editText_defaultRadius.isInEditMode()) {
            editText_defaultRadius.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (!TextUtils.isEmpty(s.toString())) {
                        int i = Integer.parseInt(s.toString()) / 100;
                        seekBar.setProgress(i);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (!TextUtils.isEmpty(s.toString())) {
                        int i = Integer.parseInt(s.toString()) / 100;
                        seekBar.setProgress(i);
                    }
                }
            });
        }else {
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        editText_defaultRadius.setText("" + (progress * 100));
                }

                @SuppressLint("SetTextI18n")
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    editText_defaultRadius.setText("" + (seekBar.getProgress() * 100));
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    editText_defaultRadius.setText("" + (seekBar.getProgress() * 100));
                }
            });
        }

        dialog.show();
    }

    private void setDefaultRadius(int parseInt) {
        defaultRadius = parseInt / 100;
        Toast.makeText(getContext(), "" + defaultRadius, Toast.LENGTH_SHORT).show();
    }

    public static int getDefaultRadius() {
        return defaultRadius;
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
        menu.findItem(R.id.menu_activity_main_addNewPost).setVisible(false);
        menu.findItem(R.id.menu_logout_profil).setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //3 - Handle actions on menu items
        switch (item.getItemId()) {
            case R.id.menu_activity_main_profile:
                startActivity(new Intent(getContext(), ProfileUserActivity.class));
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
