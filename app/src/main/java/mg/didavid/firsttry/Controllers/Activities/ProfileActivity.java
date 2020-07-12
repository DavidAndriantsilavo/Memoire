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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import mg.didavid.firsttry.R;

public class ProfileActivity extends AppCompatActivity {

    TextView textView_displayName, textView_email;
    Button button_delete, button_logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

       textView_displayName = findViewById(R.id.nom_profile);
        textView_email = findViewById(R.id.email_profile);
        button_delete = findViewById(R.id.bouton_delete_profile);
        button_logout = findViewById(R.id.bouton_logout);

        //DISPLAY THE USER INFORMATIONS IN THE TEXTVIEW
        displayInformations();

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
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if(user!=null)
                {
                    //BUILD ALERT DIALOG TO CONFIRM THE SUPPRESSION
                    AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                    builder.setMessage("Etes-vous sûr de vouloir supprimer votre compte?");
                    builder.setCancelable(true);

                    builder.setPositiveButton(
                            "SUPPRIMER",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    user.delete()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(getApplicationContext(), "Votre compte a été supprimé avec SUCCES ... BYE-BYE", Toast.LENGTH_LONG).show();

                                                        logOut();

                                                        Intent logOut =  new Intent(getApplicationContext(), LoginActivity.class);
                                                        startActivity(logOut);
                                                    }
                                                }
                                            });
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

                else{
                    Toast.makeText(getApplicationContext(), "Y a rien à supprimer vous n'existez pas !!!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void logOut() {
        FirebaseAuth.getInstance().signOut();
        GoogleSignIn.getClient(
                this,
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        ).signOut();

        Intent logOut =  new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(logOut);

        this.finish();
    }

    private void displayInformations() {
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if(signInAccount != null)
        {
            textView_displayName.setText(signInAccount.getDisplayName());
            textView_email.setText(signInAccount.getEmail());
        }
        else
        {
            textView_displayName.setText("NULL");
            textView_email.setText("NULL");
        }
    }
}
