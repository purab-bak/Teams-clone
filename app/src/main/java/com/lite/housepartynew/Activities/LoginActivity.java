package com.lite.housepartynew.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.lite.housepartynew.R;

import static android.widget.Toast.LENGTH_SHORT;

public class LoginActivity extends AppCompatActivity {

    EditText emailET, passwordET;
    Button confirmBtn;
    TextView gotoSignupTV;

    FirebaseAuth mAuth;
    FirebaseUser mUser;

    LinearLayout googleSignInButton;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInOptions gso;
    private int RC_SIGN_IN = 1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        init();

        gotoSignupTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            }
        });

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailET.getText().toString();
                String password = passwordET.getText().toString();

                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(LoginActivity.this, "Logged in", Toast.LENGTH_LONG).show();
                            
                            startActivity(new Intent(LoginActivity.this, JoinChannelActivity.class));
                        }
                        else {
                            Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();

                        }
                    }
                });
            }
        });

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("986346753834-tbtrh2vg5kftberhoihbt7rgc4v87j53.apps.googleusercontent.com")
                .requestEmail()
                .build();

        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
                if (account == null) {

                }
                signUpWithGoogle();
            }
        });
    }

    private void signUpWithGoogle() {

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mGoogleSignInClient.signOut();
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                Toast.makeText(getApplicationContext(),"Signing in...",LENGTH_SHORT).show();
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                //Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                //Log.w(TAG, "Google sign in failed", e);
                Toast.makeText(getApplicationContext(), "Sign in failed! ", LENGTH_SHORT).show();
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
                            //Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(LoginActivity.this, "Successfully signed in", LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, JoinChannelActivity.class));
                            finish();

                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, task.getException().getMessage(), LENGTH_SHORT).show();
                        }

                    }
                });
    }


    private void init() {

        emailET = findViewById(R.id.emailET);
        passwordET = findViewById(R.id.passwordET);
        confirmBtn = findViewById(R.id.confirmLoginBtn);
        gotoSignupTV = findViewById(R.id.gotoSignUpTV);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser()!=null){
            startActivity(new Intent(LoginActivity.this, JoinChannelActivity.class));
            finish();
        }

        googleSignInButton = findViewById(R.id.google_sign_in);

    }
}