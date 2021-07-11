package com.lite.teamsclone.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
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
    private DatabaseReference userRef;

    TextView forgotPasswordTV;

    TextInputLayout emailTil, passwordTil;

    TextView errorTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        setContentView(R.layout.activity_login);

        init();

        gotoSignupTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
                finish();
            }
        });


        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                errorTv.setText("");

                String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

                String email = emailET.getText().toString();
                String password = passwordET.getText().toString();

                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){

                    if (email.matches(emailPattern)){
                        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()){
                                    Toast.makeText(LoginActivity.this, "Logged in", Toast.LENGTH_LONG).show();

                                    startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                                }
                                else {


                                    errorTv.setText(task.getException().getMessage());
                                    errorTv.startAnimation(shakeError());

                                }
                            }
                        });
                    }
                    else {
                        errorTv.setText("Invalid email");
                        errorTv.startAnimation(shakeError());
                    }
                }
                else {

                    if (email.isEmpty()){
                        errorTv.setText("email cannot be empty");
                        errorTv.startAnimation(shakeError());
                    }
                    else if (password.isEmpty()){
                        errorTv.setText("password cannot be empty");
                        errorTv.startAnimation(shakeError());
                    }

                }

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

        forgotPasswordTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final EditText mResetEmail = new EditText(view.getContext());

                AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(view.getContext());

                passwordResetDialog.setTitle("Reset password");
                passwordResetDialog.setMessage("Enter your email to receive reset link");
                passwordResetDialog.setView(mResetEmail);

                passwordResetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //extract email and set reset link
                        String mail = mResetEmail.getText().toString();

                        if (!mail.isEmpty()){
                            mAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    errorTv.startAnimation(animation());
                                    errorTv.setText("Reset link has been sent to your mail");


                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    errorTv.setText("Error resetting the password " + e.getMessage());
                                    errorTv.startAnimation(shakeError());
                                }
                            });
                        }
                        else {
                            errorTv.setText("Email cannot be empty");
                            errorTv.startAnimation(shakeError());
                        }


                    }
                });

                passwordResetDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //close dialog
                    }
                });

                passwordResetDialog.create().show();

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
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately

                errorTv.setText("Sign in failed!" + e.getMessage());
                errorTv.startAnimation(shakeError());
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
                            Toast.makeText(LoginActivity.this, "Successfully signed in", LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                            finish();

                        } else {
                            // If sign in fails, display a message to the user.

                            errorTv.setText("Sign in failed!" + task.getException().getMessage());
                            errorTv.startAnimation(shakeError());
                            //Toast.makeText(LoginActivity.this, task.getException().getMessage(), LENGTH_SHORT).show();
                        }

                    }
                });
    }

    private void checkIfAlreadyRegistered(FirebaseUser user) {
        userRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    //user already exists
                    showToast("Welcome back! " + user.getDisplayName());
                }
                else {
                    //register user
                    uploadDetailsDatabase(user.getEmail(), user.getDisplayName());

                    showToast("Hello " + user.getDisplayName());
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void showToast(String s) {
        Toast.makeText(LoginActivity.this, s, LENGTH_SHORT).show();
    }
    private void uploadDetailsDatabase(String email, String name) {

        User user = new User(mAuth.getUid(), name, email);
        userRef.child(mAuth.getUid()).setValue(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(LoginActivity.this, "Added to firebase", Toast.LENGTH_LONG).show();
                    }
                });

        FirebaseFirestore.getInstance().collection("users").document(email).set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(LoginActivity.this, "Added to Firestore", Toast.LENGTH_LONG).show();

                    }
                });
    }

    private void init() {

        emailET = findViewById(R.id.emailET);
        passwordET = findViewById(R.id.passwordET);
        confirmBtn = findViewById(R.id.confirmLoginBtn);
        gotoSignupTV = findViewById(R.id.gotoSignUpTV);
        emailTil = findViewById(R.id.emailTIL);
        passwordTil = findViewById(R.id.passwordTIL);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser()!=null){
            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
            finish();
        }

        googleSignInButton = findViewById(R.id.google_sign_in);
        userRef = FirebaseDatabase.getInstance().getReference().child("users");
        forgotPasswordTV = findViewById(R.id.forgotPasswordTV);

        errorTv = findViewById(R.id.errorTV);

    }

    private TranslateAnimation shakeError() {
        TranslateAnimation shake = new TranslateAnimation(0, 10, 0, 0);
        shake.setDuration(500);
        shake.setInterpolator(new CycleInterpolator(7));
        return shake;
    }

    private AlphaAnimation animation(){
        AlphaAnimation greenAnim = new AlphaAnimation(0.3f, 1.0f);
        greenAnim.setDuration(500);
        return greenAnim;
    }
}