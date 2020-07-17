package mg.didavid.firsttry.Controllers.Activities;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;

import android.text.TextUtils;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import mg.didavid.firsttry.R;

public class RegisterActivity extends AppCompatActivity {

    private EditText nom_r, prenom_r, phoneNo_r, motDePasse_r, confirmMotDePasse_r;
    private TextView nom_ctrl, prenom_ctrl, phoneNO_ctrl, motDePasse_ctrl, confirmePwd_ctrl;
    private Button btnSend;
    private RadioButton male, female;
    private ProgressBar loginProgress;


    boolean control_minuscule, control_majuscule, control_chiffre, control_comparePwd, control_nom, control_prenom, control_phoneNo, control_pwdLength = true;


    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference documentReference;


    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallback;

    private final String M_COUNTRY_CODE = "+261";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        nom_r = findViewById(R.id.editText_nom);
        prenom_r = findViewById(R.id.editText_prenom);
        phoneNo_r = findViewById(R.id.editText_num);
        motDePasse_r = findViewById(R.id.editText_password);
        confirmMotDePasse_r = findViewById(R.id.editText_confirm_password);

        nom_ctrl = findViewById(R.id.controle_nom_rg);
        prenom_ctrl = findViewById(R.id.controle_prenom_rg);
        phoneNO_ctrl = findViewById(R.id.controle_phoneNo_rg);
        motDePasse_ctrl = findViewById(R.id.controle_pwd_rg);
        confirmePwd_ctrl = findViewById(R.id.controle_confirmePwd_rg);

        documentReference = db.collection("Users").document("Profile");

        btnSend = findViewById(R.id.button_send);

        loginProgress = findViewById(R.id.progressBar_horizontal);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();




        btnSend.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("WrongConstant")
            @Override
            public void onClick(View v) {
                String phone_num = phoneNo_r.getText().toString();
                String completePhoneNo = M_COUNTRY_CODE + phone_num;

                String uName = nom_r.getText().toString();
                String uLastname = prenom_r.getText().toString();
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

                //control phone number
                if (phone_num.isEmpty()){
                    control_phoneNo = false;
                    erreur_phoneNumber_length();
                }
                if (phone_num.length() < 9 || phone_num.length() > 10){
                    control_phoneNo = false;
                    erreurLengthPhoneNo();
                }else if (phone_num.length() == 10){
                    control_phoneNo = false;
                    erreurGivingZero_phoneNo();
                }else { //verification du numero s'il commance bien par 33, 34 ou 32
                    char[] ascciiCode_phoneNumber = phone_num.toCharArray();
                    if ((ascciiCode_phoneNumber[0] == 0x33) && ((ascciiCode_phoneNumber[1] == 0x34) || (ascciiCode_phoneNumber[1] == 0x32))) {
                        control_phoneNo = true;
                        phoneNO_ctrl.setVisibility(View.INVISIBLE);
                    }else {
                        control_phoneNo = false;
                        erreurBeging_phoneNo();
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
                if ((control_minuscule || control_majuscule) && control_chiffre && control_comparePwd && control_nom && control_prenom && control_phoneNo && control_pwdLength) {
                    loginProgress.setVisibility(View.VISIBLE);

                    //verification du numero de téléphone et encoie du code de confirmatsion
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            completePhoneNo,
                            60,
                            TimeUnit.SECONDS,
                            RegisterActivity.this,
                            mCallback
                    );
                }else{
                    Toast.makeText(RegisterActivity.this, "il y a une erreur de contrôle !", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //verification de l'existant du numero dans la base de données du provider
        mCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                Toast.makeText(getApplicationContext(), "Verification fait avec succès!", Toast.LENGTH_LONG).show();
                singinWithAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Toast.makeText(getApplicationContext(), "Verification échouéeeeeeeeeeeeeeee !", Toast.LENGTH_LONG).show();
                loginProgress.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
            }
        };
    }

    private void erreur_phoneNumber_length() {
        phoneNO_ctrl.setText("Veillez entrer un numero de téléphone");
        phoneNO_ctrl.setVisibility(View.VISIBLE);
    }

    private void erreurBeging_phoneNo() {
        phoneNO_ctrl.setText("le numero doit commancer par 32, 33 ou 34");
        phoneNO_ctrl.setVisibility(View.VISIBLE);
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


    private void erreurGivingZero_phoneNo() {
        phoneNO_ctrl.setText("Vous avez peut-être mis un \"0\" au début");
        phoneNO_ctrl.setVisibility(View.VISIBLE);
    }

    private void erreurLengthPhoneNo() {
        phoneNO_ctrl.setText("Ce n'est pas un numéro de téléphone");
        phoneNO_ctrl.setVisibility(View.VISIBLE);
    }

    private void erreurLastName() {
        prenom_ctrl.setVisibility(View.VISIBLE);
    }

    private void erreurName() {
        nom_ctrl.setVisibility(View.VISIBLE);
    }

    //s'authentifier avec le phone number
    private void singinWithAuthCredential(PhoneAuthCredential phoneAuthCredential) {
        mAuth.signInWithCredential(phoneAuthCredential)
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful()){
                            sendUserOnWelcomeActivity();
                        }else{
                            if(task.getException() instanceof FirebaseAuthInvalidCredentialsException){
                                Toast.makeText(getApplicationContext(), "Erreur de verification de l'otp !", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }

    //renvoie vers la page d'accueil
    private void sendUserOnWelcomeActivity() {
        String uName = nom_r.getText().toString();
        String uLastname = prenom_r.getText().toString();
        String uPhoneNo = "+261" + phoneNo_r.getText().toString();
        String uMotDePasse = motDePasse_r.getText().toString();

        Intent intent = new Intent(RegisterActivity.this, WelcomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("name", uName);
        intent.putExtra("last name", uLastname);
        intent.putExtra("phone number", uPhoneNo);
        intent.putExtra("mot de passe", uMotDePasse);
        startActivity(intent);
        finish();
    }


    //Ajout de l'user dans Firestore
    @SuppressLint("WrongConstant")
    private void addUser() {
        String uName = nom_r.getText().toString();
        String uLastname = prenom_r.getText().toString();
        String uPhoneNo = "+261" + phoneNo_r.getText().toString();
        String uMotDePasse = motDePasse_r.getText().toString();


            Map<String, Object> profile = new HashMap<>();
            profile.put("name", uName);
            profile.put("last name", uLastname);
            profile.put("phone number", uPhoneNo);
            profile.put("password", uMotDePasse);

            documentReference.set(profile)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            loginProgress.setVisibility(View.INVISIBLE);
                            Toast.makeText(RegisterActivity.this, "Profile créé", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(RegisterActivity.this, "Echèc !", Toast.LENGTH_SHORT).show();

                            loginProgress.setVisibility(View.INVISIBLE);
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
                    assert imm != null;
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }
}
