package com.lite.housepartynew.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.lite.housepartynew.R;

import org.jetbrains.annotations.NotNull;

public class JoinChannelActivity extends AppCompatActivity {

    EditText channelNameEt;
    Button joinBtn;

    FirebaseAuth mAuth;
    FirebaseUser mCurrentUser;

    String channelName;

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

    }

    private void gotoMainActivity() {

        channelName = channelNameEt.getText().toString().trim();


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
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

    }

    public void onBackClicked(View view) {
        super.onBackPressed();
    }
}