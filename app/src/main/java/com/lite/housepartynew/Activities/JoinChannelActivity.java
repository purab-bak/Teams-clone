package com.lite.housepartynew.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lite.housepartynew.R;

public class JoinChannelActivity extends AppCompatActivity {

    EditText channelNameEt;
    Button joinBtn, logoutBtn;

    FirebaseAuth mAuth;
    FirebaseUser mCurrentUser;

    DatabaseReference mRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_channel);

        init();

        joinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoMainActivity();
            }
        });


        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOutUser();
            }
        });



    }

    private void signOutUser() {
        mAuth.signOut();
        startActivity(new Intent(JoinChannelActivity.this, LoginActivity.class));
        finish();
    }

    private void gotoMainActivity() {

        String channelName = channelNameEt.getText().toString().trim();
        if (TextUtils.isEmpty(channelName)){
            Toast.makeText(JoinChannelActivity.this, "Cannot be empty", Toast.LENGTH_SHORT).show();
        }
        else{
            Intent intent = new Intent(JoinChannelActivity.this, MainActivity.class);
            intent.putExtra("channelName", channelName);
            startActivity(intent);
        }
    }

    private void init(){

        joinBtn = findViewById(R.id.joinBtn);
        channelNameEt = findViewById(R.id.channelNameEt);
        logoutBtn = findViewById(R.id.logoutBtn);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        mRef = FirebaseDatabase.getInstance().getReference();
    }
}