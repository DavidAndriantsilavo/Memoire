package mg.didavid.firsttry.Controllers.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import mg.didavid.firsttry.Models.User;
import mg.didavid.firsttry.Models.UserSingleton;
import mg.didavid.firsttry.R;

public class WelcomeActivity extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference userCollectionReference = db.collection("Users");

    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

    private String user_id = "NULL";
    private String lastname = "NULL";
    private String firstname = "NULL";
    private String display_name = "NULL";
    private String pseudo = "NULL";
    private String sexe = "NULL";
    private String phone = "NULL";
    private String email = "NULL";
    private final String TAG= "MainActivity";

    private String[] separated_name;

    private EditText editText_nom, editText_prenom, editText_pseudo, editText_phone, editText_email;
    private RadioGroup radioGroup_sexe;
    private RadioButton radioButton_male, radioButton_female, radioButton_selected;
    private Button button_send;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        editText_nom = findViewById(R.id.editText_nom);
        editText_prenom = findViewById(R.id.editText_prenom);
        editText_pseudo = findViewById(R.id.editText_pseudo);
        editText_phone = findViewById(R.id.editText_phone);
        editText_email = findViewById(R.id.editText_email);
        radioGroup_sexe = findViewById(R.id.radioGroup_sexe);
        radioButton_male = findViewById(R.id.radioButton_male);
        radioButton_female = findViewById(R.id.radioButton_female);
        button_send = findViewById(R.id.button_send);

        //always check if we are connected into the internet
        checkConnexion();

        Intent intent = getIntent();
        String singinPseudo = "" + intent.getStringExtra("key");
        String register_pseudo = "" + intent.getStringExtra("pseudo");
        String register_pwd = "" + intent.getStringExtra("password");
        String password = null;
        final String finalPassword = password;
        button_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(TextUtils.isEmpty(editText_nom.getText().toString().trim())) {
                    editText_nom.setError("Veuillez rensigner cette information");
                    return;
                }
                else if(TextUtils.isEmpty(editText_prenom.getText().toString().trim())){
                    editText_prenom.setError("Veuillez rensigner cette information");
                    return;
                }
                else if(TextUtils.isEmpty(editText_pseudo.getText().toString().trim())){
                    editText_pseudo.setError("Veuillez rensigner cette information");
                    return;
                }
                else{
                    radioButton_selected =findViewById(radioGroup_sexe.getCheckedRadioButtonId());

                    user_id = firebaseUser.getUid();
                    lastname = editText_nom.getText().toString();
                    firstname = editText_prenom.getText().toString();
                    display_name = firstname + " " + lastname;
                    pseudo = editText_pseudo.getText().toString();
                    sexe = radioButton_selected.getText().toString();
                    phone = editText_phone.getText().toString();
                    email = editText_email.getText().toString();

                    final User user = new User(user_id, display_name, pseudo, sexe, email, phone, finalPassword);
                    storeUserData(user);
                }
            }
        });

        //IF USER IS AUTH TO FIREBASE AND NO SINGLETON SET
        if (singinPseudo.equals("Pseudo and Password")) {
            editText_pseudo.setEnabled(false);
            editText_pseudo.setText(register_pseudo);
            password = register_pwd;
        }else {
                configureUser();
        }
    }

    private void configureUser()
    {
        DocumentReference documentReference = userCollectionReference.document(firebaseUser.getUid());

        //CHECK IF THE USER IS ALREADY STORED IN THE DATABASE OR NOT
        //IF NOT THEN CREATE A NEW DOCUMENT WITH THE UID
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                //Checking request result
                if (task.isSuccessful()) {
                    //Request was successful but it never means that data is found
                    DocumentSnapshot data = task.getResult();
                    if (data.exists()) {
                        Toast.makeText(WelcomeActivity.this, "YOU ARE ALREADY SAVED IN THE DATABASE !!!", Toast.LENGTH_SHORT).show();

                        //USER INSTANCE TO STORE THE USER FROM FIRESTORE IF THE DOCUMENT ALREADY EXISTS
                        User user = data.toObject(User.class);

                        Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                        startActivity(intent);

                        finish();
                    } else {
                        email = firebaseUser.getEmail();
                        user_id = firebaseUser.getUid();
                        display_name = firebaseUser.getDisplayName();
                        phone = firebaseUser.getPhoneNumber();

                        if(email != null){
                            editText_email.setText(email);
                            editText_email.setEnabled(false);
                        }

                        if(display_name != null){
                            separated_name = display_name.split(" ");
                            firstname = separated_name[0];
                            lastname = separated_name[1];

                            editText_prenom.setText(firstname);
                            editText_nom.setText(lastname);
                        }

                        if(phone != null){
                            editText_phone.setText(phone);
                        }
                    }

                } else {
                    //Request was not successful
                    //Could be some rules or internet problem
                    Log.i(TAG, "onComplete: Request unsuccessful, error: " + task.getException().getLocalizedMessage());
                }
            }
        });
    }
    //CREATE NEW USER IN FIRESTORE AND STORE DATAS
    private void storeUserData(final User user){
        DocumentReference documentReference = userCollectionReference.document(user_id);

        //Writing data and using call-back functions
        documentReference.set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(WelcomeActivity.this);
                    builder.setMessage("Votre compte a été créé avec succes");
                    builder.setCancelable(false);

                    builder.setPositiveButton(
                            "Continuer",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                                    startActivity(intent);

                                    finish();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }else{
                    //Something went wrong
                    Log.e(TAG, "onComplete: Error: " + task.getException().getLocalizedMessage() );
                }
            }
        });
    }

    //THE FOLLOWING METHOD IS USED TO DETACH EDIT_TEXT FOCUS WHEN WE CLICK OUTSIDE OF IT
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }

    // CHECK IF INTERNET CONNEXION IS AVAILABLE
    public boolean checkConnexion(){
        ConnectivityManager cm =
                (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Veuillez vous connecter à internet!");
        builder.setCancelable(false);

        builder.setPositiveButton(
                "retour",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        if(!isConnected)
        {
            alert.show();
        }else {
            alert.dismiss();
        }
        return isConnected;
    }

}
