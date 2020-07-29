package mg.didavid.firsttry.Controllers.Activities;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import android.text.TextUtils;
import android.util.Log;
import android.os.Bundle;

import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import mg.didavid.firsttry.R;

public class RegisterActivity extends AppCompatActivity {

    private EditText nom_r, prenom_r, pseudo_r, motDePasse_r, confirmMotDePasse_r;
    private TextView nom_ctrl, prenom_ctrl, pseudo_ctrl, motDePasse_ctrl, confirmePwd_ctrl;
    private Button btnSend;
    private RadioButton sexe_male; // is Checked => male... else female
    private ProgressDialog loginProgress;


    boolean control_minuscule, control_majuscule, control_chiffre, control_comparePwd, control_nom, control_prenom, control_pseudo, control_pwdLength = true;


    FirebaseFirestore db;
    CollectionReference collectionReference;
    DocumentReference documentReference;

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //recuperation de la vue
        nom_r = findViewById(R.id.editText_nom);
        prenom_r = findViewById(R.id.editText_prenom);
        pseudo_r = findViewById(R.id.editText_pseudo);
        sexe_male = findViewById(R.id.radioButton_male);
        motDePasse_r = findViewById(R.id.editText_password);
        confirmMotDePasse_r = findViewById(R.id.editText_confirm_password);
        btnSend = findViewById(R.id.button_send);

        //init ProgressDialog
        loginProgress = new ProgressDialog(this);
        loginProgress.setMessage("Création de votre compte...");

        //recuperation des TextView de la vue
        nom_ctrl = findViewById(R.id.controle_nom_rg);
        prenom_ctrl = findViewById(R.id.controle_prenom_rg);
        pseudo_ctrl = findViewById(R.id.controle_pseudo_rg);
        motDePasse_ctrl = findViewById(R.id.controle_pwd_rg);
        confirmePwd_ctrl = findViewById(R.id.controle_confirmePwd_rg);

        //création de la collection User et du document Profil dans la base Firestore
        db = FirebaseFirestore.getInstance();
        collectionReference = db.collection("Users");

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();


        btnSend.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("WrongConstant")
            @Override
            public void onClick(View v) {
                //final String phone_num = phoneNo_r.getText().toString();
                //final String completePhoneNo = M_COUNTRY_CODE + phone_num;

                String uName = nom_r.getText().toString();
                String uLastname = prenom_r.getText().toString();
                String uPseudo = pseudo_r.getText().toString();
                String completPseudo = "";
                String uMotDePasse = motDePasse_r.getText().toString();
                String uConfirmePwd = confirmMotDePasse_r.getText().toString();


                // -- control input --

                //control Name
                if(TextUtils.isEmpty(uName)){
                    control_nom = false;
                    erreurName();
                }else {
                    control_nom = true;
                    nom_ctrl.setVisibility(View.INVISIBLE);
                }

                //control Lastname
                if (TextUtils.isEmpty(uLastname)){
                    control_prenom = false;
                    erreurLastName();
                }else {
                    control_prenom = true;
                    prenom_ctrl.setVisibility(View.INVISIBLE);
                }

                //control pseudo... il faut qu'il y ait le caractère @ à l'interieur
                char[] charPseudo = uPseudo.toCharArray();
                if (uPseudo.isEmpty()){
                    control_pseudo = false;
                    erreur_pseudoEmpty();
                }else{
                    for (int i=0; i<uPseudo.length(); i++){
                        if(!control_pseudo) {
                            if (charPseudo[i] != 0x40) {
                                control_pseudo = false;
                                erreur_PseudoInvalide();
                            } else if ((charPseudo[i] == 0x40) && ((i + 1) == uPseudo.length())) {
                                control_pseudo = false;
                                erreur_formatPseudoInvalide();
                            } else {
                                completPseudo = "" + uPseudo + ".mg";
                                control_pseudo = true;
                                pseudo_ctrl.setVisibility(View.INVISIBLE);
                            }
                        }
                    }
                }


                //control password
                int j;
                int pwdLength = uMotDePasse.length();
                if (pwdLength < 6){
                    control_pwdLength = false;
                    erreurPwdLength();
                }
                if (pwdLength >= 6){
                    control_minuscule = false;
                    control_majuscule = false;
                    control_chiffre = false;
                    control_comparePwd = false;
                    for (j = 0; j < pwdLength; j++) {
                        char[] ascciiCode_motDePasse = uMotDePasse.toCharArray();
                        //verification de presence d'une minuscule
                        if ((ascciiCode_motDePasse[j] > 0x40) && (ascciiCode_motDePasse[j] < 0x5B)) {
                            if (!control_minuscule) {
                                control_minuscule = true;
                            }
                        }
                        //verification de presence d'une mauscule
                        else if ((ascciiCode_motDePasse[j] > 0x60) && (ascciiCode_motDePasse[j] < 0x7B)){
                            if (!control_majuscule) {
                                control_majuscule = true;
                            }
                        }
                        //verification de presence d'un chiffre
                        else if ((ascciiCode_motDePasse[j] > 0x29) && (ascciiCode_motDePasse[j] < 0x3A)){
                            if (!control_chiffre) {
                                control_chiffre = true;
                            }
                        }
                    }
                    //verification presence de lettre dans le mot de passe
                    if (!control_minuscule && !control_majuscule){
                        erreurPwdLettre();
                    }
                    //verification presence de chiffre dans le mot de passe
                    else if (!control_chiffre){
                        erreurPwdChiffre();
                    }else{
                        motDePasse_ctrl.setVisibility(View.INVISIBLE);
                    }

                    //comparaison de mot de passe
                    if ((control_minuscule || control_majuscule) && control_chiffre) {
                        if (!uMotDePasse.equals(uConfirmePwd)) {
                            erreurPwdNotSame();
                        }else {
                            confirmePwd_ctrl.setVisibility(View.INVISIBLE);
                            control_comparePwd = true;
                        }
                    }
                }


                //evoie du code de vérification du numero de téléphone
                if ((control_minuscule || control_majuscule) && control_chiffre && control_comparePwd && control_nom && control_prenom && control_pseudo && control_pwdLength) {
                    loginProgress.show();
                    singinNewUser(completPseudo, uMotDePasse);
                }else{ //juste pour le test... à enlever plutard
                    Toast.makeText(RegisterActivity.this, "il y a une erreur de contrôle !", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void singinNewUser(String pseudo, String pwd) {
        mAuth.createUserWithEmailAndPassword(pseudo, pwd)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update user's information
                            mCurrentUser = mAuth.getCurrentUser();
                            String Uid = mCurrentUser.getUid();
                            addUser(Uid);
                            goToWelcomePage();
                        }
                    }
                });
    }

    private void goToWelcomePage() {
        startActivity(new Intent(RegisterActivity.this, WelcomeActivity.class));
    }

    private void sendUserHome() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void addUser(String Uid) {
        String uLastname = prenom_r.getText().toString();
        String uName = nom_r.getText().toString() + uLastname;
        String uPseudo = pseudo_r.getText().toString();
        String uMotDePasse = motDePasse_r.getText().toString();
        String uSexe;
        if (sexe_male.isChecked()){
            uSexe = "Homme";
        }else {
            uSexe = "Femme";
        }

        Map<String, String> profile = new HashMap<>();
        profile.put("Name", uName);
        profile.put("pseudo", uPseudo);
        profile.put("email", "");
        profile.put("password", uMotDePasse);
        profile.put("sex", uSexe);
        profile.put("user_id", Uid);
        profile.put("phone", "");
        profile.put("profile_image", "");

        documentReference = collectionReference.document(Uid);
        documentReference.set(profile);

        Toast.makeText(this, "Compte créé avec succès", Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("ResourceAsColor")
    private void erreur_formatPseudoInvalide() {
        pseudo_ctrl.setText("Veillez inserer un \"@\" à l'interieur de votre pseudo.\nExemple : Nasolo@JackSmv");
        pseudo_ctrl.setTextColor(R.color.red);
    }

    @SuppressLint("ResourceAsColor")
    private void erreur_PseudoInvalide() {
        pseudo_ctrl.setText("Veillez inserer un \"@\" dans votre pseudo.\nExemple : Nasolo@JackSmv");
        pseudo_ctrl.setTextColor(R.color.red);
    }

    @SuppressLint("ResourceAsColor")
    private void erreur_pseudoEmpty() {
        pseudo_ctrl.setText("Ajouter un pseudo. Exemple : Nasolo@JackSmv");
        pseudo_ctrl.setTextColor(R.color.red);
    }
    private void erreurPwdNotSame() {
        confirmePwd_ctrl.setVisibility(View.VISIBLE);
    }

    private void erreurPwdChiffre() {
        motDePasse_ctrl.setText("Doit contenir au moins un chiffre");
        motDePasse_ctrl.setVisibility(View.VISIBLE);
    }

    private void erreurPwdLettre() {
        motDePasse_ctrl.setText("Doit contenir au moins une lettre");
        motDePasse_ctrl.setVisibility(View.VISIBLE);
    }

    private void erreurPwdLength() {
        motDePasse_ctrl.setText("Doit contenir 6 caractères au moins");
        motDePasse_ctrl.setVisibility(View.VISIBLE);
    }

    private void erreurLastName() {
        prenom_ctrl.setVisibility(View.VISIBLE);
    }

    private void erreurName() {
        nom_ctrl.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkConnexion();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkConnexion();
        if(mCurrentUser != null){
            sendUserHome();
        }
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
                    assert imm != null;
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }
}