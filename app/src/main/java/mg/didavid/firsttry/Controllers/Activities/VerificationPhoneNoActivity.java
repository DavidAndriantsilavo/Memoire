package mg.didavid.firsttry.Controllers.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import mg.didavid.firsttry.R;

public class VerificationPhoneNoActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;

    private EditText mOtpText;
    private Button mConfirmer;
    private ProgressBar verifyProgressBer;

    private String mAuthVerificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification_phone_no);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        mOtpText = findViewById(R.id.editText_otp);
        mConfirmer = findViewById(R.id.button_confirmer);
        verifyProgressBer = findViewById(R.id.progressBar_horizontal_otp);

        mAuthVerificationId = getIntent().getStringExtra("AuthCredential");


        mConfirmer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String otp = mOtpText.getText().toString();

                if(otp.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Veillez entrer le code !", Toast.LENGTH_LONG).show();
                }else {
                    verifyProgressBer.setVisibility(View.VISIBLE);

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mAuthVerificationId, otp);
                    singinWithAuthCredential(credential);
                }
            }
        });
    }

    private void singinWithAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(VerificationPhoneNoActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            sendUserHome();
                        }else {
                            if(task.getException() instanceof FirebaseAuthInvalidCredentialsException){
                                Toast.makeText(getApplicationContext(), "Erreur de verification de l'otp !", Toast.LENGTH_LONG).show();
                            }
                        }
                        verifyProgressBer.setVisibility(View.INVISIBLE);
                    }
                });

    }

    private void sendUserHome() {
        Intent intent = new Intent(VerificationPhoneNoActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    /*@Override
    protected void onStart() {
        super.onStart();

        if(mCurrentUser != null){
            sendUserHome();
        }
    }*/
}
