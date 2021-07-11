package com.lite.teamsclone.Activities;

import androidx.appcompat.app.AppCompatActivity;

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

public class JoinChannelActivity extends AppCompatActivity {

    TextInputEditText channelNameEt;
    ExtendedFloatingActionButton joinBtn;

    FirebaseAuth mAuth;
    FirebaseUser mCurrentUser;

    String channelName;

    FirebaseFirestore firestoreDb;

    TextView errorTv;

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
            errorTv.startAnimation(shakeError());
            errorTv.setText("Meeting ID cannot be empty");        }
        else{

            checkIfMeetingExists();
            
        }
    }

    private void checkIfMeetingExists() {

        firestoreDb.collection("meetings").document(channelName).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (!documentSnapshot.exists()){
                    errorTv.startAnimation(shakeError());
                    errorTv.setText("Uh oh! No such meeting exists");
                }
                else {
                    //copy meeting details

                    Meeting meeting = documentSnapshot.toObject(Meeting.class);
                    //showToast(meeting.getHostEmail());

                    firestoreDb.collection(mCurrentUser.getEmail()).document(channelName).set(meeting)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {

                                    errorTv.setTextColor(Color.GREEN);
                                    errorTv.startAnimation(animation());
                                    errorTv.setText("Joining meeting!");


                                    Intent intent = new Intent(JoinChannelActivity.this, MainActivity.class);
                                    intent.putExtra("channelName", channelName);
                                    startActivity(intent);

                                }
                            });
                }
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