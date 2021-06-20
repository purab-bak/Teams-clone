package com.lite.housepartynew.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lite.housepartynew.Models.User;
import com.lite.housepartynew.R;

public class SignUpActivity extends AppCompatActivity {

    EditText emailET, passwordET, fullNameET;
    Button confirmBtn;
    TextView gotoLoginTV;
    FirebaseAuth mAuth;
    FirebaseUser mUser;

    DatabaseReference userRef;

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

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()){
                                    Toast.makeText(SignUpActivity.this, task.getResult().toString(), Toast.LENGTH_SHORT).show();

                                    User user = new User(mAuth.getUid(), fullNameET.getText().toString(), email);
                                    userRef.child(mAuth.getUid().toString()).setValue(user)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    Toast.makeText(SignUpActivity.this, "Added to firebase", Toast.LENGTH_LONG).show();
                                                }
                                            });

                                    startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                                    finish();

                                }
                                else {
                                    Toast.makeText(SignUpActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                }

                            }
                        });
            }
        });




    }

    private void init() {

        emailET = findViewById(R.id.emailET);
        passwordET = findViewById(R.id.passwordET);
        fullNameET = findViewById(R.id.fullNameET);
        confirmBtn = findViewById(R.id.confirmSignUpBtn);
        gotoLoginTV = findViewById(R.id.gotoLoginTV);

        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("users");

    }
}