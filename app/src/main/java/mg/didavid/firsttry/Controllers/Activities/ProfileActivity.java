package mg.didavid.firsttry.Controllers.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import mg.didavid.firsttry.Models.User;
import mg.didavid.firsttry.Models.UserSingleton;
import mg.didavid.firsttry.R;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class ProfileActivity extends AppCompatActivity {

    TextView display_name, displayFirstname, email, phone;
    ImageView imageView_pdp;
    Button button_delete, button_logout, button_image_profile;

    final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

    //FRIESTORE INSATANCE
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference userCollectionReference = db.collection("Users");

    //REALTIME DATABASE INSTANCE
    private FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mUserLocationReference = mFirebaseDatabase.getReference().child("userLocation");

    private final String TAG ="ProfileActivity";
    private boolean isUserAuthDeleted = false;
    private boolean isUserDataDeleted = false;
    private boolean isUserLocalisationDeleted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        display_name = findViewById(R.id.nom_profile);
        displayFirstname = findViewById(R.id.prenom_profile);
        email = findViewById(R.id.email_profile);
        phone = findViewById(R.id.phone_profile);

        button_delete = findViewById(R.id.bouton_delete_profile);
        button_logout = findViewById(R.id.bouton_logout);
        button_image_profile = findViewById(R.id.button_image_profile);

        button_image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(firebaseUser != null){
                    for (UserInfo profile : firebaseUser.getProviderData()) {
                        // Id of the provider (ex: google.com)

                        String provider = profile.getProviderId();

                        if (provider.contains("facebook")) {
                            Toast.makeText(getApplicationContext(), "FACEBOOK", Toast.LENGTH_LONG).show();
                        } else if (provider.contains("google")) {
                            Toast.makeText(getApplicationContext(), "GOOGLE", Toast.LENGTH_LONG).show();
                        } else{
                            Toast.makeText(getApplicationContext(), "OTHER", Toast.LENGTH_LONG).show();
                        }

                    }
                }
            }
        });

        //logout
        button_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logOut();
            }
        });

        //DELETE ACCOUNT
        button_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //BUILD ALERT DIALOG TO CONFIRM THE SUPPRESSION
                AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                builder.setMessage("Etes-vous sûr de vouloir supprimer votre compte?");
                builder.setCancelable(true);

                builder.setPositiveButton(
                        "SUPPRIMER",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                deleteUserLocalisation();
                            }
                        });

                builder.setNegativeButton(
                        "Annuler",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        //DISPLAY THE USER INFORMATIONS IN THE TEXTVIEW
        displayInformations();
    }

    //DELETE USER LOCALISATION IN REALTIME DATABASE
    private void deleteUserLocalisation()
    {
        final User user = ((UserSingleton) getApplicationContext()).getUser();

        if(user != null)
        {
            mUserLocationReference.child(user.getUser_id()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        //Task was successful, data written!
                        Log.d(TAG, "ProfileActivity : deleted user localisation!!");
                        isUserLocalisationDeleted = true;
                        deleteUserAuth();
                    }else{
                        //Task was not successful,
                        //Log the error message
                        Log.i(TAG, "onComplete: DB Request unsuccessful, error: " + task.getException().getLocalizedMessage() + "/" + user.getUser_id());
                    }
                }
            });
        }
    }

    //DELETE USER AUTH IN FIREBASE AUTH
    private void deleteUserAuth() {
        if(firebaseUser!=null)
        {
            firebaseUser.delete()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "ProfileActivity : deleted user auth!!");
                                isUserAuthDeleted = true;
                                deleteUserData();
                            }
                            else {
                                isUserAuthDeleted = false;
                            }
                        }
                    });
        }
        else{
            Toast.makeText(getApplicationContext(), "Y a rien à supprimer vous n'existez pas !!!", Toast.LENGTH_LONG).show();
        }
    }

    //DELETE USER DATA IN FIRESTORE
    private void deleteUserData() {
        User user = ((UserSingleton) getApplicationContext()).getUser();
        if(user != null) {
            final DocumentReference documentReference = userCollectionReference.document(user.getUser_id());

            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    //Checking request result
                    if (task.isSuccessful()) {
                        //Request was successful but it never means that data is found
                        DocumentSnapshot data = task.getResult();
                        if (data.exists()) {
                            documentReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "ProfileActivity : deleted user data!!");
                                        isUserDataDeleted = true;
                                        deletedUser();
                                    } else {
                                        Log.e(TAG, "onComplete: Error: " + task.getException().getLocalizedMessage());
                                        isUserDataDeleted = false;
                                    }
                                }
                            });
                        } else {
                            Log.e(TAG, "onComplete: Error: " + task.getException().getLocalizedMessage());
                        }

                    } else {
                        //Request was not successful
                        //Could be some rules or internet problem
                        Log.i(TAG, "onComplete: Request unsuccessful, error: " + task.getException().getLocalizedMessage());
                    }
                }
            });


        }
        else{
            Toast.makeText(getApplicationContext(), "Y a rien à supprimer vous n'existez pas !!!", Toast.LENGTH_LONG).show();
        }
    }

    //CHECK IF BOTH USER AUTH AND USER DATA ARE DELETED
    private void deletedUser()
    {
        if( isUserDataDeleted && isUserAuthDeleted && isUserLocalisationDeleted) {
            Toast.makeText(getApplicationContext(), "Votre compte a été supprimé avec SUCCES ... BYE-BYE", Toast.LENGTH_LONG).show();

            logOut();

            Intent logOut = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(logOut);
        }
        else {
            Toast.makeText(getApplicationContext(), "Erreur, votre compte n'a pas pu être supprimé!!!", Toast.LENGTH_LONG).show();
        }
    }

    private void logOut() {
        if(firebaseUser != null){
                for (UserInfo profile : firebaseUser.getProviderData()) {
                    // CHECK THE PROVIDER TO SIGN OUT FROM
                    if (profile.getProviderId().contains("google.com"))
                    {
                        GoogleSignIn.getClient(
                                this,
                                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
                        ).signOut();
                        Toast.makeText(getApplicationContext(), "GOOGLE SIGN OUT !!!", Toast.LENGTH_LONG).show();
                    }

                }
        }

        ((UserSingleton)getApplicationContext()).setUser(null);
        FirebaseAuth.getInstance().signOut();


        Intent logOut =  new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(logOut);

        MainActivity.stopActivity.finish();
        this.finish();
    }

    private void displayInformations() {
        User user = ((UserSingleton) getApplicationContext()).getUser();
        if(user != null)
        {
            //imageView_pdp.setText(signInAccount.getPhotoUrl());
            display_name.setText(user.getName());
            displayFirstname.setText(user.getUser_id());
            email.setText(user.getEmail());
            phone.setText((user.getPhone()));

        }
    }
}
