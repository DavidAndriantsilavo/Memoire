package mg.didavid.firsttry.Controllers.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import mg.didavid.firsttry.Models.User;
import mg.didavid.firsttry.R;

import static com.google.common.base.Ascii.toLowerCase;

public class LoginActivity extends AppCompatActivity {

    private Button button_register, button_connexion;
    private ImageButton button_google;

    private EditText mPseudo_lg, mMotDePasse_lg;
    private TextView erreurLoginTv, erreurPhoneNumber;

    private static final int RC_SIGN_IN = 1234;
    private static final String TAG = "LoginActivity";
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    ProgressDialog progressDialog, progressDialog_register;

    String pseudo, password;

    @Override
    protected void onStart() {
        super.onStart();

        //verifier a connexion internet dès le démarrage de l'application
        checkConnexion();

        //getting current currentUser who has authenticated
        FirebaseUser user = mAuth.getInstance().getCurrentUser();

        //id currentUser still login, redirect to MainActivity
        if(user != null)
        {
            Intent intent =  new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        checkConnexion();
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
        erreurLoginTv = findViewById(R.id.textView_erreur_login);
        erreurPhoneNumber = findViewById(R.id.textView_erreurPhoneNumber_login);

        createRequest();

        mAuth= FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Chargement ...");
        progressDialog_register = new ProgressDialog(this);
        progressDialog_register.setMessage("Creation de votre compte...");



        //redirect to register if currentUser hasn't account

        button_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccountWithPseudoAndPassword();
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

                    if (pseudo.isEmpty() || motDePasse.isEmpty()){
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
                                            progressDialog.dismiss();
                                            finish();
                                        } else {
                                            // If sign in fails, display a message to the currentUser.
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

        mAuth = FirebaseAuth.getInstance(); //get the instance of authentication

        createRequest();


        button_google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkConnexion()) {
                    signIn();
                    progressDialog.show();
                }
            }
        });
    }

    private void createAccountWithPseudoAndPassword() {
        //custom dialog
        final Dialog dialog = new Dialog(LoginActivity.this);
        dialog.setContentView(R.layout.dialog_register);
        dialog.setTitle("Création de compte");

        //set the custom dialog components
        final EditText editText_pseudo = dialog.findViewById(R.id.pseudo_et);
        final EditText password_editText = dialog.findViewById(R.id.pwd_et);
        final EditText confirmPassword_editText = dialog.findViewById(R.id.confirmePwd_et);
        Button button_annler = dialog.findViewById(R.id.btn_annuler);
        Button button_creer = dialog.findViewById(R.id.btn_creer);

        button_annler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        button_creer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get the input text
                pseudo = editText_pseudo.getText().toString();
                password = password_editText.getText().toString();
                boolean control_pseudo = false;
                boolean control_minuscule = false;
                boolean control_majuscule = false;
                boolean control_chiffre = false;
                boolean control_comparePwd = false;
                boolean checked = false;
                String completPseudo = null;

                //control pseudo... il faut qu'il y ait le caractère @ à l'interieur
                char[] charPseudo = pseudo.toCharArray();
                if (pseudo.isEmpty()){
                    editText_pseudo.setError("Veillez entrer votre pseudo");
                }else{
                    for (int i=0; i<pseudo.length(); i++){
                        if(!control_pseudo){
                            if ((charPseudo[i] != 0x40) && !checked && ((i + 1) == pseudo.length())) { // 0x40 == ascii code of "@"
                                editText_pseudo.setError("Le pseudo doit contenir \"@\"");
                            } else if ((charPseudo[i] == 0x40) && ((i + 1) == pseudo.length())) {
                                checked = true;
                                editText_pseudo.setError("Veillez suivre l'exemple !");
                            } else if (charPseudo[i] == 0x40){
                                checked = true;
                                completPseudo = "" + pseudo + ".mg";
                                control_pseudo = true;
                            }
                        }
                    }
                }

                //control password
                int j;
                int pwdLength = password.length();
                if (pwdLength < 6){
                    password_editText.setError("Doit contenir au moins 6 caractères");
                }else {
                    for (j = 0; j < pwdLength; j++) {
                        char[] ascciiCode_motDePasse = password.toCharArray();
                        //verification de presence d'une minuscule
                        if ((ascciiCode_motDePasse[j] > 0x40) && (ascciiCode_motDePasse[j] < 0x5B)) {
                            if (!control_minuscule) {
                                control_minuscule = true;
                            }
                        }
                        //verification de presence d'une majuscule
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
                        password_editText.setError("Doit contenir au moins une lettre");
                    }
                    //verification presence de chiffre dans le mot de passe
                    else if (!control_chiffre){
                        password_editText.setError("Doit contenir au moins un Chiffre");
                    }

                    //comparaison de mot de passe
                    if ((control_minuscule || control_majuscule) && control_chiffre) {
                        if (!password.equals(confirmPassword_editText.getText().toString())) {
                            confirmPassword_editText.setError("Le mot de passe ne correspond pas");
                        }else {
                            control_comparePwd = true;
                        }
                    }
                }
                if(control_comparePwd && control_pseudo){
                    //dialog.dismiss();
                    progressDialog_register.show();
                    //authentication with Email and password
                    mAuth.createUserWithEmailAndPassword(completPseudo, password)
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success
                                        Intent intent = new Intent(LoginActivity.this, WelcomeActivity.class);
                                        intent.putExtra("key","Pseudo and Password");
                                        intent.putExtra("pseudo", pseudo);
                                        intent.putExtra("password", password);
                                        progressDialog_register.dismiss();
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog_register.dismiss();
                            Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }else {
                    Toast.makeText(LoginActivity.this, "il y a une erreur de contrôle", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog.show();
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
                                // Sign in success, update UI with the signed-in currentUser's information
                                Log.d(TAG, "signInWithCredential:success");
                                FirebaseUser currentUser = mAuth.getInstance().getCurrentUser();

                                checkUserExists(currentUser);
                            } else {
                                // If sign in fails, display a message to the currentUser.
                                Log.w(TAG, "signInWithCredential:failure", task.getException());
                                Toast.makeText(getApplicationContext(), "Désolé une erreur s'est produite", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

        private void checkUserExists(FirebaseUser user){
            DocumentReference documentReference = FirebaseFirestore.getInstance().collection("Users").document(user.getUid());

            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    //Checking request result
                    if (task.isSuccessful()) {
                        //Request was successful but it never means that data is found
                        DocumentSnapshot data = task.getResult();
                        if (data.exists()) {

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            progressDialog.dismiss();

                            finish();
                        } else {
                            Intent intent =  new Intent(LoginActivity.this, WelcomeActivity.class);
                            startActivity(intent);
                            progressDialog.dismiss();

                            finish();
                        }

                    } else {
                        //Request was not successful
                        //Could be some rules or internet problem
                        Log.i(TAG, "onComplete: Request unsuccessful, error: " + task.getException().getLocalizedMessage());
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



