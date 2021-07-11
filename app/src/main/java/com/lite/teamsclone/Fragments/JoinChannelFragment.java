package com.lite.teamsclone.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lite.teamsclone.Activities.MainActivity;
import com.lite.teamsclone.Models.Meeting;
import com.lite.teamsclone.R;


public class JoinChannelFragment extends Fragment {

    EditText channelNameEt;
    Button joinBtn, logoutBtn;

    FirebaseAuth mAuth;
    FirebaseUser mCurrentUser;

    String channelName;

    Button openDashboard;

    FirebaseFirestore firestoreDb;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_join_channel, container, false);
        
        init(view);

        joinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoMainActivity();
            }
        });


        return view;
    }

    private void gotoMainActivity() {

        channelName = channelNameEt.getText().toString().trim();


        if (TextUtils.isEmpty(channelName)){
            Toast.makeText(getContext(), "Cannot be empty", Toast.LENGTH_SHORT).show();
        }
        else{

            checkIfMeetingExists();

        }
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


                        Intent intent = new Intent(getContext(), MainActivity.class);
                        intent.putExtra("channelName", channelName);
                        startActivity(intent);

                    }
                });
    }

    private void showToast(String s) {
        Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
    }

    private void init(View view) {
        joinBtn = view.findViewById(R.id.joinBtn);
        channelNameEt = view.findViewById(R.id.channelNameEt);
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        firestoreDb = FirebaseFirestore.getInstance();
    }
}


