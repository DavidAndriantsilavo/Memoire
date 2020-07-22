package mg.didavid.firsttry.Controllers.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import mg.didavid.firsttry.R;

import static com.google.common.base.Ascii.toLowerCase;

public class LoginActivity extends AppCompatActivity {

    private Button button_register, button_connexion;
    private ImageButton button_google, button_facebook;

    private EditText mPseudo_lg, mMotDePasse_lg;
    private TextView erreurLoginTv, erreurPhoneNumber;

    private static final int RC_SIGN_IN = 1234;
    private static final String TAG = "RegisterActivity";
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    ProgressDialog progressDialog;

    @Override
    protected void onStart() {
        super.onStart();

        //verifier a connexion internet dès le démarrage de l'application
        checkConnexion();

        //getting current user who has authenticated
        FirebaseUser user = mAuth.getInstance().getCurrentUser();

        //id user still login, redirect to MainActivity
        if(user != null)
        {
            Intent intent =  new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //reuperation des vues
        mPseudo_lg = findViewById(R.id.editText_num_login);
        mMotDePasse_lg = findViewById(R.id.editText_password_login);
        button_register = findViewById(R.id.button_register);
        button_connexion = findViewById(R.id.button_connexion);
        button_google = findViewById(R.id.button_google);
        button_facebook = findViewById(R.id.button_facebook);
        erreurLoginTv = findViewById(R.id.textView_erreur_login);
        erreurPhoneNumber = findViewById(R.id.textView_erreurPhoneNumber_login);

        createRequest();

        mAuth= FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading ...");



        //redirect to register if user hasn't account

        button_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent register = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(register);
            }
        });


        //connexion with pseudo and password
        button_connexion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkConnexion()) {
                    String pseudo = mPseudo_lg.getText().toString();
                    String motDePasse = mMotDePasse_lg.getText().toString();
                    String uCompletPseudo = toLowerCase(pseudo) + ".mg";

                    if (pseudo.isEmpty() && motDePasse.isEmpty()){
                        erreurLoginTv.setText("Veillez completer tous les champs !");
                        erreurLoginTv.setVisibility(View.VISIBLE);
                    }else {
                        progressDialog.show();

                        mAuth.signInWithEmailAndPassword(uCompletPseudo, motDePasse)
                                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            //redirection vers la page d'accueil
                                            Intent connexion = new Intent(LoginActivity.this, MainActivity.class);
                                            startActivity(connexion);
                                            finish();
                                        } else {
                                            // If sign in fails, display a message to the user.
                                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                                    Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                erreurLoginTv.setText(e.getMessage());
                                erreurLoginTv.setVisibility(View.VISIBLE);

                            }
                        });
                    }
                }
            }
        });

        button_google = findViewById(R.id.button_google);
        button_facebook = findViewById(R.id.button_facebook);


        mAuth = FirebaseAuth.getInstance(); //get the instance of authentication

        createRequest();


        button_google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkConnexion()) {
                    signIn();
                }
            }
        });
    }

    ///////////////////////////////////////////////////
    /////////////////////////////////////////////////////
    //GOOGLE SIGN IN
    ////////////////////////////////////////////////////
    /////////////////////////////////////////////////////

    //REQUEST GOOGLE FOR SIGN IN
        private void createRequest() {
            // Configure Google Sign In
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.web_client_id))
                    .requestEmail()
                    .build();

            // Build a GoogleSignInClient with the options specified by gso.
            mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        }

        //GOOGLE POP-UP INTENT FOR CHOOSE GOOGLE ACCOUNT
        private void signIn() {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        }

        //GOOGLE REQUEST RESULT AFTER RECEIVING DATA FROM GOOGLE
        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
            if (requestCode == RC_SIGN_IN) {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                    firebaseAuthWithGoogle(account.getIdToken());
                } catch (ApiException e) {
                    // Google Sign In failed, update UI appropriately
                    Log.w(TAG, "Google sign in failed", e);
                    Toast.makeText(this, e.getMessage(),Toast.LENGTH_SHORT).show();
                    // ...
                }
            }
        }

        private void firebaseAuthWithGoogle(String idToken) {
            AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
            mAuth.signInWithCredential(credential)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithCredential:success");
                                FirebaseUser user = mAuth.getInstance().getCurrentUser();

                                Intent intent =  new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent);

                                finish();
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithCredential:failure", task.getException());
                                Toast.makeText(getApplicationContext(), "Désolé une erreur s'est produite", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

    ///////////////////////////////////////////////////
    /////////////////////////////////////////////////////
    //GOOGLE SIGN IN END
    ////////////////////////////////////////////////////
    /////////////////////////////////////////////////////

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

        if(!isConnected)
        {
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
            alert.show();
        }
        return isConnected;
    }
}



