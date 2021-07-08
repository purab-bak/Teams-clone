package com.lite.housepartynew.Fragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.lite.housepartynew.R;

import java.io.File;

public class EditProfileFragment extends Fragment {

    ImageView profilePictureEdit;
    EditText nameEt;
    Button editConfirmButton;

    String name, email, phone;

    private Uri productImageUri;
    Uri croppedImageUri;
    File compressedImage, croppedImage;

    private String downloadImageUrl;


    FirebaseUser mCurrentUser;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        nameEt = view.findViewById(R.id.accountNameEdit);
        profilePictureEdit = view.findViewById(R.id.accountPictureEdit);
        editConfirmButton = view.findViewById(R.id.accountEditConfirm);

        nameEt.setText(mCurrentUser.getDisplayName());


        if (mCurrentUser.getPhotoUrl() != null) {
            Glide.with(getContext()).load(mCurrentUser.getPhotoUrl()).into(profilePictureEdit);
        }

        profilePictureEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });



        return view;
    }

}
