package com.lite.housepartynew.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lite.housepartynew.Models.Meeting;
import com.lite.housepartynew.R;

public class CreateInstantMeetingActivity extends AppCompatActivity {

    EditText meetingCodeEt;
    Button startBtn;

    FirebaseAuth mAuth;
    FirebaseUser mCurrentUser;

    String channelName;

    FirebaseFirestore firestoreDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_instant_meeting);

        initUi();

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                channelName = meetingCodeEt.getText().toString().trim();


                if (TextUtils.isEmpty(channelName)){
                    Toast.makeText(CreateInstantMeetingActivity.this, "Cannot be empty", Toast.LENGTH_SHORT).show();
                }
                else{
                    checkIfMeetingExists();
                }
            }
        });
    }

    private void checkIfMeetingExists() {
        firestoreDb.collection("meetings").document(channelName).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    showToast("Meeting ID already used");
                }
                else {
                    showToast("DNE");
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

                        showToast("Meeting created! Added to database");


                        Intent intent = new Intent(CreateInstantMeetingActivity.this, MainActivity.class);
                        intent.putExtra("channelName", channelName);
                        startActivity(intent);

                    }
                });
    }

    private void showToast(String s) {
        Toast.makeText(CreateInstantMeetingActivity.this, s, Toast.LENGTH_SHORT).show();
    }


    private void initUi() {
        meetingCodeEt = findViewById(R.id.meetingCode);
        startBtn = findViewById(R.id.startBtn);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        firestoreDb = FirebaseFirestore.getInstance();
    }

    public void onBackClicked(View view) {
        super.onBackPressed();
    }
}