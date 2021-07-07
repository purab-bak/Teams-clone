package com.lite.housepartynew.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.google.android.gms.dynamic.IFragmentWrapper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lite.housepartynew.Models.User;
import com.lite.housepartynew.R;

import static android.widget.Toast.LENGTH_SHORT;

public class SignUpActivity extends AppCompatActivity {

    EditText emailET, passwordET, fullNameET;
    Button confirmBtn;
    TextView gotoLoginTV;
    FirebaseAuth mAuth;
    FirebaseUser mUser;

    DatabaseReference userRef, mapRef;

    //google signup

    LinearLayout googleSignUpButton;
    //    SignInButton googleSignUpButton;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInOptions gso;
    private int RC_SIGN_IN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        init();

        gotoLoginTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            }
        });

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = emailET.getText().toString();
                String password = passwordET.getText().toString();
                String name = fullNameET.getText().toString();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(name)){
                    Toast.makeText(SignUpActivity.this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                }
                else {
                    createUser(email, password, name);
                }
            }
        });

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("986346753834-tbtrh2vg5kftberhoihbt7rgc4v87j53.apps.googleusercontent.com")
                .requestEmail()
                .build();

        googleSignUpButton.setOnClickListener(new View.OnClickListener() {
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
                            Toast.makeText(SignUpActivity.this, "Successfully signed in", LENGTH_SHORT).show();
                            startActivity(new Intent(SignUpActivity.this, JoinChannelActivity.class));
                            finish();

                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(SignUpActivity.this, task.getException().getMessage(), LENGTH_SHORT).show();
                        }

                    }
                });
    }



    private void createUser(String email, String password, String name) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()){

                            uploadDetailsDatabase(email, name);
                            Toast.makeText(SignUpActivity.this, task.getResult().toString(), Toast.LENGTH_SHORT).show();

                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();

                            task.getResult().getUser().updateProfile(profileUpdates)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Toast.makeText(SignUpActivity.this, "Welcome " + task.getResult().getUser().getDisplayName(), Toast.LENGTH_SHORT).show();

                                        }
                                    });

                        }
                        else {
                            Toast.makeText(SignUpActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        }

                    }
                });
    }

    private void uploadDetailsDatabase(String email, String name) {
        User user = new User(mAuth.getUid(), name, email);
        userRef.child(mAuth.getUid()).setValue(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(SignUpActivity.this, "Added to firebase", Toast.LENGTH_LONG).show();
                    }
                });

        //mapRef.child(mAuth.getUid()).setValue(email);

        startActivity(new Intent(SignUpActivity.this, JoinChannelActivity.class));
        finish();
    }

    private void init() {

        emailET = findViewById(R.id.emailET);
        passwordET = findViewById(R.id.passwordET);
        fullNameET = findViewById(R.id.fullNameET);
        confirmBtn = findViewById(R.id.confirmSignUpBtn);
        gotoLoginTV = findViewById(R.id.gotoLoginTV);

        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("users");
        //mapRef = FirebaseDatabase.getInstance().getReference().child("email-uid-map");

        googleSignUpButton = findViewById(R.id.google_sign_in);

    }
}