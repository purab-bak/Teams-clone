package com.lite.housepartynew.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Paint;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lite.housepartynew.Models.Meeting;
import com.lite.housepartynew.R;

import org.jetbrains.annotations.NotNull;

public class JoinChannelActivity extends AppCompatActivity {

    EditText channelNameEt;
    Button joinBtn;

    FirebaseAuth mAuth;
    FirebaseUser mCurrentUser;

    String channelName;

    FirebaseFirestore firestoreDb;

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

            checkIfMeetingExists();
            
        }
    }

    private void checkIfMeetingExists() {

        firestoreDb.collection("meetings").document(channelName).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (!documentSnapshot.exists()){
                    showToast("No such meeting exists!");
                }
                else {
                    saveMeetingDetailsToFirestore();
                }
            }
        });
    }

    private void saveMeetingDetailsToFirestore() {

        long epoch = System.currentTimeMillis();

        Meeting meeting = new Meeting("instant","instant",channelName, null, String.valueOf(epoch), "instant");

        firestoreDb.collection(mCurrentUser.getEmail()).document(channelName).set(meeting);

        firestoreDb.collection("meetings").document(channelName).set(meeting)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        showToast("Joining meeting!");


                        Intent intent = new Intent(JoinChannelActivity.this, MainActivity.class);
                        intent.putExtra("channelName", channelName);
                        startActivity(intent);

                    }
                });
    }


    private void showToast(String s) {
        Toast.makeText(JoinChannelActivity.this, s, Toast.LENGTH_SHORT).show();
    }

    private void init(){

        joinBtn = findViewById(R.id.joinBtn);
        channelNameEt = findViewById(R.id.channelNameEt);
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        firestoreDb = FirebaseFirestore.getInstance();

    }

    public void onBackClicked(View view) {
        super.onBackPressed();
    }
}