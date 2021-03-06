package com.lite.teamsclone.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lite.teamsclone.Models.User;
import com.lite.teamsclone.R;

import org.jetbrains.annotations.NotNull;

import static android.widget.Toast.LENGTH_SHORT;

/**
 * Activity to signup a user
 **/


public class SignUpActivity extends AppCompatActivity {

    EditText emailET, passwordET, fullNameET;
    Button confirmBtn;
    TextView gotoLoginTV;
    FirebaseAuth mAuth;
    FirebaseUser mUser;

    DatabaseReference userRef, mapRef;

    FirebaseFirestore firestoreDb;

    //google signup

    LinearLayout googleSignUpButton;
    //    SignInButton googleSignUpButton;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInOptions gso;
    private int RC_SIGN_IN = 1;
    private TextView errorTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
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

                if (isAllFilledAndVerified()) {
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
                Toast.makeText(getApplicationContext(), "Signing in...", LENGTH_SHORT).show();
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                //Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                //Log.w(TAG, "Google sign in failed", e);

                errorTv.setText("Sign in failed! " + e.getMessage());
                errorTv.startAnimation(shakeError());
                //Toast.makeText(getApplicationContext(), "Sign in failed! ", LENGTH_SHORT).show();
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

                            checkIfAlreadyRegistered(task.getResult().getUser());

                            startActivity(new Intent(SignUpActivity.this, DashboardActivity.class));
                            SignUpActivity.this.finish();

                        } else {
                            // If sign in fails, display a message to the user.
                            errorTv.setText(task.getException().getMessage());
                            errorTv.startAnimation(shakeError());
                        }

                    }
                });
    }

    private void checkIfAlreadyRegistered(FirebaseUser user) {
        userRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    //user already exists
                    showToast("Welcome back +" + user.getDisplayName());
                } else {
                    //register user
                    uploadDetailsDatabase(user.getEmail(), user.getDisplayName());

                    showToast("Hello new user");
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void showToast(String s) {
        Toast.makeText(SignUpActivity.this, s, LENGTH_SHORT).show();
    }


    private void createUser(String email, String password, String name) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {

                            uploadDetailsDatabase(email, name);
                            //Toast.makeText(SignUpActivity.this, task.getResult().toString(), Toast.LENGTH_SHORT).show();

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


                            startActivity(new Intent(SignUpActivity.this, DashboardActivity.class));
                            finish();

                        } else {
                            errorTv.setText(task.getException().getMessage());
                            errorTv.startAnimation(shakeError());
                            //Toast.makeText(SignUpActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_LONG).show();
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
                        //Toast.makeText(SignUpActivity.this, "Added to firebase", Toast.LENGTH_LONG).show();
                    }
                });

        firestoreDb.collection("users").document(email).set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //Toast.makeText(SignUpActivity.this, "Added to Firestore", Toast.LENGTH_LONG).show();

                    }
                });

        //mapRef.child(mAuth.getUid()).setValue(email);
    }

    private boolean isAllFilledAndVerified() {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        if (fullNameET.getText().toString().isEmpty()) {
            errorTv.setText("Enter your name");
            errorTv.startAnimation(shakeError());
            return false;
        } else if (emailET.getText().toString().isEmpty()) {
            errorTv.setText("Enter your email");
            errorTv.startAnimation(shakeError());
            return false;
        } else if (passwordET.getText().toString().isEmpty()) {
            errorTv.setText("Enter a password");
            errorTv.startAnimation(shakeError());
            return false;
        } else if (!emailET.getText().toString().trim().matches(emailPattern)) {
            errorTv.setText("Invalid email");
            errorTv.startAnimation(shakeError());
            return false;
        }
        return true;
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
        firestoreDb = FirebaseFirestore.getInstance();

        errorTv = findViewById(R.id.errorTV);

    }

    private TranslateAnimation shakeError() {
        TranslateAnimation shake = new TranslateAnimation(0, 10, 0, 0);
        shake.setDuration(500);
        shake.setInterpolator(new CycleInterpolator(7));
        return shake;
    }

    private AlphaAnimation animation() {
        AlphaAnimation greenAnim = new AlphaAnimation(0.3f, 1.0f);
        greenAnim.setDuration(500);
        return greenAnim;
    }

}