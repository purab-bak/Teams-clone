package com.lite.housepartynew.Fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.lite.housepartynew.Activities.CreateInstantMeetingActivity;
import com.lite.housepartynew.Activities.JoinChannelActivity;
import com.lite.housepartynew.Activities.ReminderActivity;
import com.lite.housepartynew.Activities.ScheduledMeetingsActivity;
import com.lite.housepartynew.R;


public class HomeFragment extends Fragment {

    ImageView profileImage;

    FirebaseUser mCurrentUser;

    TextView userFullName;

    CardView remindMeCV, scheduledMeetingsCV, startInstantMeetingCV, joinMeetingCV, myNotesCV, previousMeetingsCV;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        profileImage = view.findViewById(R.id.userProfileIcon);
        userFullName = view.findViewById(R.id.user_full_name);
        remindMeCV = view.findViewById(R.id.remindMeCardView);
        scheduledMeetingsCV = view.findViewById(R.id.scheduledMeetingsCV);
        startInstantMeetingCV = view.findViewById(R.id.instantMeetingCV);
        joinMeetingCV = view.findViewById(R.id.joinMeetingCV);
        myNotesCV = view.findViewById(R.id.myNotesCV);
        previousMeetingsCV = view.findViewById(R.id.meetingsHistoryCV);

        try {
            if (mCurrentUser.getPhotoUrl() != null) {
                Glide.with(getContext()).load(mCurrentUser.getPhotoUrl()).into(profileImage);
            }
            userFullName.setText(mCurrentUser.getDisplayName());

        } catch (Exception e) {
            e.printStackTrace();

        }

        remindMeCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), ReminderActivity.class));
            }
        });

        scheduledMeetingsCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), ScheduledMeetingsActivity.class));
            }
        });

        joinMeetingCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), JoinChannelActivity.class));
            }
        });

        startInstantMeetingCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), CreateInstantMeetingActivity.class));
            }
        });

        return view;
    }
}


