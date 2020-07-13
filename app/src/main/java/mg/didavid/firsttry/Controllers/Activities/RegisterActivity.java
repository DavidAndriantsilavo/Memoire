package mg.didavid.firsttry.Controllers.Activities;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;

import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;

import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import mg.didavid.firsttry.Models.InfosUser;
import mg.didavid.firsttry.R;

public class RegisterActivity extends AppCompatActivity {

    private EditText nom_r, prenom_r, phoneNo_r, motDePasse_r, confirmMotDePasse_r;
    private Button btnSend;
    private RadioButton male, female;
    private ProgressBar loginProgress;


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

        documentReference = db.collection("Users").document("Profile");

        btnSend = findViewById(R.id.button_send);

        loginProgress = findViewById(R.id.progressBar_horizontal);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();


        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone_num = phoneNo_r.getText().toString();
                String completePhoneNo = M_COUNTRY_CODE + phone_num;

                if(phone_num.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Entrer numéro !", Toast.LENGTH_LONG).show();
                }else {
                    loginProgress.setVisibility(View.VISIBLE);

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            completePhoneNo,
                            60,
                            TimeUnit.SECONDS,
                            RegisterActivity.this,
                            mCallback
                    );
                }
            }
        });


        mCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                singinWithAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Toast.makeText(getApplicationContext(), "Verification échouée !", Toast.LENGTH_LONG).show();
                loginProgress.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                Intent intent = new Intent(RegisterActivity.this, VerificationPhoneNoActivity.class);
                intent.putExtra("AuthCredential", s);
                addUser();
                startActivity(intent);
            }
        };
    }



    private void singinWithAuthCredential(PhoneAuthCredential phoneAuthCredential) {
        mAuth.signInWithCredential(phoneAuthCredential)
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful()){
                            sendUserHone();
                        }else{
                            if(task.getException() instanceof FirebaseAuthInvalidCredentialsException){
                                Toast.makeText(getApplicationContext(), "Erreur de verification de l'otp !", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }

    private void sendUserHone() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void addUser() {
        String uName = nom_r.getText().toString();
        String uLastname = prenom_r.getText().toString();
        String uPhoneNo = "+261" + phoneNo_r.getText().toString();
        String uMotDePasse = motDePasse_r.getText().toString();

        if(!TextUtils.isEmpty(uName)  || !TextUtils.isEmpty(uLastname)  || !TextUtils.isEmpty(uMotDePasse)){
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

        }else {
            Toast.makeText(this,"veillez completer tous les champs!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(mCurrentUser != null){
            sendUserHone();
        }
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
}
