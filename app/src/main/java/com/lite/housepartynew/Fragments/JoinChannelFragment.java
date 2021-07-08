package com.lite.housepartynew.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.lite.housepartynew.Activities.JoinChannelActivity;
import com.lite.housepartynew.Activities.MainActivity;
import com.lite.housepartynew.R;


public class JoinChannelFragment extends Fragment {

    EditText channelNameEt;
    Button joinBtn, logoutBtn;

    FirebaseAuth mAuth;
    FirebaseUser mCurrentUser;

    String channelName;

    Button openDashboard;

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
            Intent intent = new Intent(getContext(), MainActivity.class);
            intent.putExtra("channelName", channelName);
            startActivity(intent);

        }
    }

    private void init(View view) {
        joinBtn = view.findViewById(R.id.joinBtn);
        channelNameEt = view.findViewById(R.id.channelNameEt);
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
    }
}


