package com.lite.teamsclone.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lite.teamsclone.Models.Meeting;
import com.lite.teamsclone.R;

import java.util.Random;

public class CreateInstantMeetingActivity extends AppCompatActivity {

    TextInputEditText meetingCodeEt;
    ExtendedFloatingActionButton startBtn;

    FirebaseAuth mAuth;
    FirebaseUser mCurrentUser;

    String channelName;

    FirebaseFirestore firestoreDb;

    TextInputEditText meetingTitleEt;
    TextView generateUniqueButton;

    String title = "";

    TextView errorTv;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_instant_meeting);

        initUi();

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                channelName = meetingCodeEt.getText().toString().trim();
                title = meetingTitleEt.getText().toString();


                if (TextUtils.isEmpty(title)){

                    errorTv.startAnimation(shakeError());
                    errorTv.setText("title cannot be empty");

                }
                else if (TextUtils.isEmpty(channelName)){
                    errorTv.startAnimation(shakeError());
                    errorTv.setText("ID cannot be empty");
                }
                else{
                    checkIfMeetingExists();
                }
            }
        });

        generateUniqueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String currentTimestamp = String.valueOf(System.currentTimeMillis());

                Random random = new Random();

                char randomizedCharacter1 = (char) (random.nextInt(26) + 'a');
                char randomizedCharacter2 = (char) (random.nextInt(26) + 'a');

                channelName =String.valueOf(randomizedCharacter1) + String.valueOf(randomizedCharacter2)+currentTimestamp.substring(currentTimestamp.length() - 5);
                meetingCodeEt.setText(channelName);
            }
        });
    }

    private void checkIfMeetingExists() {
        firestoreDb.collection("meetings").document(channelName).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){

                    errorTv.startAnimation(shakeError());
                    errorTv.setText("Meeting already exists");
                }
                else {
                    saveMeetingDetailsToFirestore();
                }
            }
        });

    }

    private void saveMeetingDetailsToFirestore() {

        long epoch = System.currentTimeMillis();

        Meeting meeting = new Meeting(mCurrentUser.getEmail(),title,channelName, null, String.valueOf(epoch), "instant");

        firestoreDb.collection(mCurrentUser.getEmail()).document(channelName).set(meeting);

        firestoreDb.collection("meetings").document(channelName).set(meeting)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        errorTv.setTextColor(Color.GREEN);
                        errorTv.startAnimation(animation());
                        errorTv.setText("Starting meeting!");

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

        meetingTitleEt = findViewById(R.id.meetingTitleET);
        generateUniqueButton = findViewById(R.id.uniqueIDTV);

        errorTv = findViewById(R.id.errorTV);
    }

    public void onBackClicked(View view) {
        super.onBackPressed();
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