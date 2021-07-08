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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.lite.housepartynew.R;


public class HomeFragment extends Fragment {

    ImageView profileImage;

    FirebaseUser mCurrentUser;

    TextView userFullName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        profileImage = view.findViewById(R.id.userProfileIcon);
        userFullName = view.findViewById(R.id.user_full_name);

        if (mCurrentUser.getPhotoUrl() != null) {
            Glide.with(getContext()).load(mCurrentUser.getPhotoUrl()).into(profileImage);
        }
        userFullName.setText(mCurrentUser.getDisplayName());

        return view;
    }
}


